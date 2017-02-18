package jsettlers.main.android.mainmenu.factories;

import java8.util.stream.StreamSupport;
import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.common.menu.IMapDefinition;
import jsettlers.common.utils.collections.ChangingList;
import jsettlers.graphics.startscreen.SettingsManager;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.main.android.core.GameStarter;
import jsettlers.main.android.mainmenu.navigation.MainMenuNavigator;
import jsettlers.main.android.mainmenu.presenters.picker.JoinMultiPlayerPickerPresenter;
import jsettlers.main.android.mainmenu.presenters.setup.JoinMultiPlayerSetupPresenter;
import jsettlers.main.android.mainmenu.presenters.setup.JoinMultiPlayerSetupPresenterImpl;
import jsettlers.main.android.mainmenu.presenters.setup.JoinMultiPlayerSetupPresenterPop;
import jsettlers.main.android.mainmenu.presenters.picker.LoadSinglePlayerPickerPresenter;
import jsettlers.main.android.mainmenu.presenters.picker.NewMultiPlayerPickerPresenter;
import jsettlers.main.android.mainmenu.presenters.setup.NewMultiPlayerSetupPresenter;
import jsettlers.main.android.mainmenu.presenters.setup.NewMultiPlayerSetupPresenterImpl;
import jsettlers.main.android.mainmenu.presenters.setup.NewMultiPlayerSetupPresenterPop;
import jsettlers.main.android.mainmenu.presenters.picker.NewSinglePlayerPickerPresenter;
import jsettlers.main.android.mainmenu.presenters.setup.NewSinglePlayerSetupPresenter;
import jsettlers.main.android.mainmenu.views.JoinMultiPlayerPickerView;
import jsettlers.main.android.mainmenu.views.JoinMultiPlayerSetupView;
import jsettlers.main.android.mainmenu.views.MapPickerView;
import jsettlers.main.android.mainmenu.views.NewMultiPlayerPickerView;
import jsettlers.main.android.mainmenu.views.NewMultiPlayerSetupView;
import jsettlers.main.android.mainmenu.views.NewSinglePlayerSetupView;

import android.app.Activity;

import java.util.List;

/**
 * Created by tompr on 03/02/2017.
 */

public class PresenterFactory {
    private static final MapList defaultMapList = MapList.getDefaultList();

    /**
     * Picker screen presenters
     */
    public static NewSinglePlayerPickerPresenter createNewSinglePlayerPickerPresenter(Activity activity, MapPickerView view) {
        MainMenuNavigator navigator = (MainMenuNavigator) activity;
        GameStarter gameStarter = (GameStarter) activity.getApplication();
        ChangingList<MapLoader> changingMaps = gameStarter.getMapList().getFreshMaps();
        //ChangingList<? extends IMapDefinition> changingMaps = gameStarter.getStartScreen().getSingleplayerMaps();

        return new NewSinglePlayerPickerPresenter(view, navigator, gameStarter, changingMaps);
    }

    public static LoadSinglePlayerPickerPresenter createLoadSinglePlayerPickerPresenter(Activity activity, MapPickerView view) {
        MainMenuNavigator navigator = (MainMenuNavigator) activity;
        GameStarter gameStarter = (GameStarter) activity.getApplication();
        ChangingList<? extends MapLoader> changingMaps = gameStarter.getMapList().getSavedMaps();
        //ChangingList<? extends IMapDefinition> changingMaps = gameStarter.getStartScreen().getStoredSingleplayerGames();

        return new LoadSinglePlayerPickerPresenter(view, navigator, gameStarter, changingMaps);
    }

    public static NewMultiPlayerPickerPresenter createNewMultiPlayerPickerPresenter(Activity activity, NewMultiPlayerPickerView view) {
        MainMenuNavigator navigator = (MainMenuNavigator) activity;
        GameStarter gameStarter = (GameStarter) activity.getApplication();
        ChangingList<MapLoader> changingMaps = gameStarter.getMapList().getFreshMaps();
        //ChangingList<? extends IMapDefinition> changingMaps = gameStarter.getStartScreen().getMultiplayerMaps();

        return new NewMultiPlayerPickerPresenter(view, navigator, gameStarter, changingMaps);
    }

    public static JoinMultiPlayerPickerPresenter createJoinMultiPlayerPickerPresenter(Activity activity, JoinMultiPlayerPickerView view) {
        MainMenuNavigator navigator = (MainMenuNavigator) activity;
        GameStarter gameStarter = (GameStarter) activity.getApplication();

        return new JoinMultiPlayerPickerPresenter(view, navigator, gameStarter);
    }

    /**
     * Setup screen presenters
     */
    public static NewSinglePlayerSetupPresenter createNewSinglePlayerSetupPresenter(Activity activity, NewSinglePlayerSetupView view, String mapId) {
        MainMenuNavigator navigator = (MainMenuNavigator) activity;
        GameStarter gameStarter = (GameStarter) activity.getApplication();

        List<MapLoader> maps = gameStarter.getMapList().getFreshMaps().getItems();
        MapLoader mapDefinition = StreamSupport.stream(maps)
                .filter(x -> mapId.equals(x.getMapId()))
                .findFirst()
                .get();

        return new NewSinglePlayerSetupPresenter(view, navigator, gameStarter, mapDefinition);
    }

    public static NewMultiPlayerSetupPresenter createNewMultiPlayerSetupPresenter(Activity activity, NewMultiPlayerSetupView view, String mapId) {
        MainMenuNavigator mainMenuNavigator = (MainMenuNavigator) activity;
        GameStarter gameStarter = (GameStarter) activity.getApplication();
        IJoinPhaseMultiplayerGameConnector joinPhaseMultiplayerGameConnector = gameStarter.getJoinPhaseMultiplayerConnector();

        List<MapLoader> maps = gameStarter.getMapList().getFreshMaps().getItems();
        MapLoader mapDefinition = StreamSupport.stream(maps)
                .filter(x -> mapId.equals(x.getMapId()))
                .findFirst()
                .get();

        if (joinPhaseMultiplayerGameConnector == null || mapDefinition == null) {
            return new NewMultiPlayerSetupPresenterPop(mainMenuNavigator);
        } else {
            return new NewMultiPlayerSetupPresenterImpl(view, mainMenuNavigator, gameStarter, joinPhaseMultiplayerGameConnector, SettingsManager.getInstance(), mapDefinition);
        }
    }

    public static JoinMultiPlayerSetupPresenter createJoinMultiPlayerSetupPresenter(Activity activity, JoinMultiPlayerSetupView view) {
        MainMenuNavigator navigator = (MainMenuNavigator) activity;
        GameStarter gameStarter = (GameStarter) activity.getApplication();
        IJoinPhaseMultiplayerGameConnector connector = gameStarter.getJoinPhaseMultiplayerConnector();

        if (connector == null/* || mapDefinition == null */) {
            return new JoinMultiPlayerSetupPresenterPop(navigator);
        } else {
            return new JoinMultiPlayerSetupPresenterImpl(view, navigator, gameStarter, connector);
        }
    }
}
