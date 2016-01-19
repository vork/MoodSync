package cz.destil.moodsync.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import android.util.Log;
import com.squareup.otto.Subscribe;

import cz.destil.moodsync.R;
import cz.destil.moodsync.activity.MainActivity;
import cz.destil.moodsync.core.App;
import cz.destil.moodsync.core.Config;
import cz.destil.moodsync.event.LocalColorEvent;
import cz.destil.moodsync.light.ColorExtractor;
import cz.destil.moodsync.light.LightsController;
import cz.destil.moodsync.light.LocalColorSwitcher;
import cz.destil.moodsync.light.MirroringHelper;

/**
 * Service which does all the work.
 *
 * @author David Vávra (david@vavra.me)
 */
public abstract class LightsService extends Service {
    protected MirroringHelper mMirroring;
    protected ColorExtractor mColorExtractor;
    protected LightsController mActiveController;
    protected LocalColorSwitcher mLocalSwitcher;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMirroring = MirroringHelper.get();
        mColorExtractor = ColorExtractor.get();
        mLocalSwitcher = LocalColorSwitcher.get();
        App.bus().register(this);
    }

    @Override
    public void onDestroy() {
        App.bus().unregister(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(this.getClass().getCanonicalName(), "Action is: " + intent.getAction());
        if (intent.getAction().equals("START")) {
            start();
        } else if (intent.getAction().equals("STOP")) {
            stop();
        }

        return START_REDELIVER_INTENT;
    }

    protected abstract void start();

    protected abstract void stop();

    @Subscribe
    public void onNewLocalColor(LocalColorEvent event) {
        mActiveController.changeColor(event.newColor);
    }
}
