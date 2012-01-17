package jsettlers.mapcreator.main;

import jsettlers.common.CommonConstants;
import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.landscape.ELandscapeType;
import jsettlers.common.map.object.BuildingObject;
import jsettlers.common.map.object.MapObject;
import jsettlers.common.map.shapes.MapCircle;
import jsettlers.common.position.ISPosition2D;
import jsettlers.common.position.RelativePoint;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.mapcreator.data.LandscapeFader;
import jsettlers.mapcreator.data.MapData;

public class DataTester implements Runnable {

	public static final int MAX_HEIGHT_DIFF = 3;
	private boolean retest = true;
	private final MapData data;

	// onyl used from test thread
	private boolean successful;
	private String result;
	private ISPosition2D resultPosition;
	private final TestResultReceiver receiver;
	private final LandscapeFader fader = new LandscapeFader();

	public DataTester(MapData data, TestResultReceiver receiver) {
		this.data = data;
		this.receiver = receiver;
		new Thread(this, "data tester").start();
	}

	@Override
	public void run() {
		while (true) {
			synchronized (this) {
				while (!retest) {
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
				}
				retest = false;
			}
			doTest();
		}
	}

	private void doTest() {
		successful = true;
		result = "";
		resultPosition = new ShortPoint2D(0, 0);

		byte[][] players = new byte[data.getWidth()][data.getHeight()];
		for (int x = 0; x < data.getWidth(); x++) {
			for (int y = 0; y < data.getHeight(); y++) {
				players[x][y] = (byte) -1;
			}
		}
		for (int x = 0; x < data.getWidth(); x++) {
			for (int y = 0; y < data.getHeight(); y++) {
				MapObject mapObject = data.getMapObject(x, y);
				if (mapObject instanceof BuildingObject) {
					BuildingObject buildingObject = (BuildingObject) mapObject;
					drawBuildingCircle(players, x, y, buildingObject);
				}
			}
		}
		for (int x = 0; x < data.getWidth(); x++) {
			for (int y = 0; y < data.getHeight(); y++) {
				MapObject mapObject = data.getMapObject(x, y);
				if (mapObject instanceof BuildingObject) {
					ShortPoint2D start = new ShortPoint2D(x, y);
					BuildingObject buildingObject = (BuildingObject) mapObject;
					testBuilding(players, x, y, start, buildingObject);
				}
			}
		}
		
		boolean[][] borders = new boolean[data.getWidth()][data.getHeight()];

		for (int x = 0; x < data.getWidth() - 1; x++) {
			for (int y = 0; y < data.getHeight() - 1; y++) {
				test(x, y, x + 1, y, players, borders);
				test(x, y, x + 1, y + 1, players, borders);
				test(x, y, x, y + 1, players, borders);
			}
		}
		
		for (int player = 0; player < data.getPlayerCount(); player++) {
			ISPosition2D point = data.getStartPoint(player);
			if (players[point.getX()][point.getY()] != player) {
				testFailed("Player " + player + " has invalid start point", point);
			}
			//mark
			borders[point.getX()][point.getY()] = true;
		}
		
		data.setPlayers(players);
		data.setBorders(borders);
		receiver.testResult(result, successful, resultPosition);
	}

	private void testBuilding(byte[][] players, int x, int y,
	        ShortPoint2D start, BuildingObject buildingObject) {
		EBuildingType type = buildingObject.getType();
		for (RelativePoint p : type.getProtectedTiles()) {
			ISPosition2D pos = p.calculatePoint(start);
			if (!data.contains(pos.getX(), pos.getY())) {
				testFailed("Building " + type + " outside map", pos);
			} else if (!MapData.listAllowsLandscape(type.getGroundtypes(),
			        data.getLandscape(pos.getX(), pos.getY()))) {
				testFailed(
				        "Building " + type + " cannot be placed on "
				                + data.getLandscape(pos.getX(), pos.getY()),
				        pos);
			} else if (players[x][y] != buildingObject.getPlayer()) {
				testFailed(
				        "Building " + type + " of player "
				                + buildingObject.getPlayer() + ", but is on "
				                + players[x][y] + "'s land", pos);
			}
		}
	}

	private void drawBuildingCircle(byte[][] players, int x, int y,
	        BuildingObject buildingObject) {
		byte player = buildingObject.getPlayer();
		EBuildingType type = buildingObject.getType();
		if (type == EBuildingType.TOWER || type == EBuildingType.BIG_TOWER
		        || type == EBuildingType.CASTLE) {
			MapCircle circle = new MapCircle(x, y, CommonConstants.TOWERRADIUS);
			drawCircle(players, player, circle);
		}
	}

	private void drawCircle(byte[][] players, byte player, MapCircle circle) {
		for (ISPosition2D pos : circle) {
			if (data.contains(pos.getX(), pos.getY())
			        && players[pos.getX()][pos.getY()] == -1) {
				players[pos.getX()][pos.getY()] = player;
			}
		}
	}

	private void test(int x, int y, int x2, int y2, byte[][] players,
	        boolean[][] borders) {
		if (Math.abs(data.getLandscapeHeight(x2, y2)
		        - data.getLandscapeHeight(x, y)) > MAX_HEIGHT_DIFF) {
			successful = false;
			result = "Too high landscape diff";
			resultPosition = new ShortPoint2D(x, y);
		}
		ELandscapeType l2 = data.getLandscape(x2, y2);
		ELandscapeType l1 = data.getLandscape(x, y);
		if (!fader.canFadeTo(l2, l1)) {
			testFailed("Wrong landscape pair: " + l2 + ", " + l1,
			        new ShortPoint2D(x, y));
		}

		if (players[x][y] != players[x2][y2]) {
			if (players[x][y] != -1) {
				borders[x][y] = true;
			}
			if (players[x2][y2] != -1) {
				borders[x2][y2] = true;
			}
		}
	}

	private void testFailed(String string, ISPosition2D pos) {
		result = string;
		resultPosition = pos;
	}

	public synchronized void retest() {
		retest = true;
		this.notifyAll();
	}

	public interface TestResultReceiver {
		public void testResult(String name, boolean allowed,
		        ISPosition2D resultPosition);
	}

}
