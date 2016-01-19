package cz.destil.moodsync.core;

import android.app.Application;

import com.crittercism.app.Crittercism;
import com.squareup.otto.Bus;

import cz.destil.moodsync.BuildConfig;
import cz.destil.moodsync.light.LightsController;
import cz.destil.moodsync.light.MirroringHelper;
import cz.destil.moodsync.light.hue.HueController;
import cz.destil.moodsync.light.lifx.LifxController;

/**
 * Main application object.
 *
 * @author David Vávra (david@vavra.me)
 */
public class App extends Application {

    static App sInstance;
    static Bus sBus;
    private MirroringHelper mMirroring;
    private LifxController mLifxLights;
    private HueController mHueLights;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Crittercism.initialize(this, "552c06ab7365f84f7d3d6da5"); //TODO outsource the id to a file
        }
        sInstance = this;
        sBus = new Bus();
        mMirroring = MirroringHelper.get();
        mLifxLights = LifxController.get();
        mHueLights = HueController.get();
        mMirroring.init();
        mLifxLights.init();
        mHueLights.init();
    }

    public static App get() {
        return sInstance;
    }

    public static Bus bus() {
        return sBus;
    }
}
