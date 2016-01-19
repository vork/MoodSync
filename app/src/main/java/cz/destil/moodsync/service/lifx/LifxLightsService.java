package cz.destil.moodsync.service.lifx;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import cz.destil.moodsync.R;
import cz.destil.moodsync.activity.MainActivity;
import cz.destil.moodsync.core.Config;
import cz.destil.moodsync.light.ColorExtractor;
import cz.destil.moodsync.light.lifx.LifxController;
import cz.destil.moodsync.service.LightsService;
import cz.destil.moodsync.util.SleepTask;

/**
 * Created by benediktboss on 19/01/16.
 */
public class LifxLightsService extends LightsService {
  private WifiManager.MulticastLock mMulticastLock;

  @Override
  public void onCreate() {
    super.onCreate();
    mActiveController = LifxController.get();
  }

  @Override
  protected void start() {
    Intent i = new Intent(this, MainActivity.class);
    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
    Notification
        notification = new Notification.Builder(this).setSmallIcon(R.drawable.ic_notification).setContentTitle(getString(R.string
        .mirroring)).setContentText(getString(R.string.tap_to_change))
        .setContentIntent(pi).build();
    startForeground(42, notification);

    WifiManager wifi;
    wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    mMulticastLock = wifi.createMulticastLock("lifx");
    mMulticastLock.acquire();

    mActiveController.start(); //TODO add groups
    mColorExtractor.start(mMirroring, new ColorExtractor.Listener() {
      @Override
      public void onColorExtracted(int color) {
        if (!mLocalSwitcher.isRunning()) {
          mActiveController.changeColor(color);
        }
      }
    }, ColorExtractor.COLOR_EXTRACT_ALL);
  }

  @Override
  protected void stop() {
    mColorExtractor.stop();
    mMirroring.stop();
    mActiveController.signalStop();
    new SleepTask(Config.FINAL_DELAY, new SleepTask.Listener() {
      @Override
      public void awoken() {
        mActiveController.stop();
        if (mMulticastLock != null) {
          mMulticastLock.release();
        }
        stopForeground(true);
        stopSelf();
      }
    }).start();
  }
}
