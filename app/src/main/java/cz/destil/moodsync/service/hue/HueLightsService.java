package cz.destil.moodsync.service.hue;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import cz.destil.moodsync.R;
import cz.destil.moodsync.activity.MainActivity;
import cz.destil.moodsync.core.Config;
import cz.destil.moodsync.data.Lamp;
import cz.destil.moodsync.light.ColorExtractor;
import cz.destil.moodsync.light.LightsController;
import cz.destil.moodsync.light.LocalColorSwitcher;
import cz.destil.moodsync.light.MirroringHelper;
import cz.destil.moodsync.light.hue.HueController;
import cz.destil.moodsync.service.LightsService;
import cz.destil.moodsync.util.SleepTask;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;

/**
 * Created by benediktboss on 19/01/16.
 */
public class HueLightsService extends LightsService {


  @Override
  public void onCreate() {
    super.onCreate();
    mActiveController = HueController.get();
  }

  @Override protected void start() {
    Intent i = new Intent(this, MainActivity.class);
    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
    Notification
        notification = new Notification.Builder(this).setSmallIcon(R.drawable.ic_notification).setContentTitle(getString(R.string
        .mirroring)).setContentText(getString(R.string.tap_to_change))
        .setContentIntent(pi).build();
    startForeground(42, notification);

    mActiveController.start();

    mColorExtractor.start(mMirroring, new ColorExtractor.Listener() {
      @Override public void onColorExtracted(List<ColorExtractor.ColorGroup> colorGroups) {
        for(ColorExtractor.ColorGroup group : colorGroups) {
          if(group.extractMode != ColorExtractor.COLOR_EXTRACT_OFF) {
            if (!mLocalSwitcher.isRunning()) {
              mActiveController.changeColor(group.color, group.extractMode);
            }
          }
        }
      }
    });
  }

  @Override protected void stop() {
    mColorExtractor.stop();
    mMirroring.stop();
    mActiveController.signalStop();
    new SleepTask(Config.FINAL_DELAY, new SleepTask.Listener() {
      @Override
      public void awoken() {
        mActiveController.stop();
        stopForeground(true);
        stopSelf();
      }
    }).start();
  }
}
