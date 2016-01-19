package cz.destil.moodsync.light.lifx;

import android.graphics.Color;
import cz.destil.moodsync.R;
import cz.destil.moodsync.core.App;
import cz.destil.moodsync.core.BaseAsyncTask;
import cz.destil.moodsync.core.Config;
import cz.destil.moodsync.event.ErrorEvent;
import cz.destil.moodsync.event.SuccessEvent;
import cz.destil.moodsync.light.LightsController;
import cz.destil.moodsync.util.Toas;
import lifx.java.android.client.LFXClient;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXTaggedLightCollection;
import lifx.java.android.network_context.LFXNetworkContext;

/**
 * Created by benediktboss on 19/01/16.
 */
public class LifxController extends LightsController {
  private LFXNetworkContext mNetworkContext;
  private static LifxController sInstance;

  public static LifxController get() {
    if (sInstance == null) {
      sInstance = new LifxController();
    }
    return sInstance;
  }

  @Override
  public void changeColor(int color) {
    if (mWorkingFine && color != mPreviousColor) {
      mNetworkContext.getAllLightsCollection().setColorOverDuration(convertColor(color), Config.DURATION_OF_COLOR_CHANGE);
      mPreviousColor = color;
    }
  }

  @Override
  public void init() {
    super.init();
    mNetworkContext = LFXClient.getSharedInstance(App.get()).getLocalNetworkContext();
    mNetworkContext.addNetworkContextListener(new LFXNetworkContext.LFXNetworkContextListener() {
      @Override
      public void networkContextDidConnect(LFXNetworkContext networkContext) {
        mDisconnected = false;
      }

      @Override
      public void networkContextDidDisconnect(LFXNetworkContext networkContext) {
        if (!mDisconnected && mWorkingFine) {
          mWorkingFine = false;
          Toas.t(R.string.lifx_disconnected);
          App.bus().post(new ErrorEvent(R.string.lifx_disconnected));
        }
      }

      @Override
      public void networkContextDidAddTaggedLightCollection(LFXNetworkContext networkContext, LFXTaggedLightCollection collection) {
        startRocking();
      }

      @Override
      public void networkContextDidRemoveTaggedLightCollection(LFXNetworkContext networkContext, LFXTaggedLightCollection collection) {
      }
    });
  }

  @Override
  protected void startRocking() {
    super.startRocking();
    mNetworkContext.getAllLightsCollection().setPowerState(LFXTypes.LFXPowerState.ON);
  }

  @Override
  public void start() {
    mNetworkContext.connect();
    if (!mWorkingFine) {
      new LifxTimeoutTask().start();
    } else {
      startRocking();
    }
  }

  @Override
  public void stop() {
    super.stop();
    if (mNetworkContext != null && mWorkingFine) {
      mNetworkContext.disconnect();
    }
  }

  private LFXHSBKColor convertColor(int color) {
    float[] hsv = new float[3];
    Color.colorToHSV(color, hsv);
    return LFXHSBKColor.getColor(hsv[0], hsv[1], Config.LIFX_BRIGHTNESS, 3500);
  }

  @Override
  public void signalStop() {
    int color = App.get().getResources().getColor(android.R.color.white);
    mNetworkContext.getAllLightsCollection().setColorOverDuration(convertColor(color), 100);
  }

   class LifxTimeoutTask extends TimeoutTask {

    @Override
    public void postExecute() {
      int numLights = mNetworkContext.getAllLightsCollection().getLights().size();
      if (numLights == 0 || mDisconnected) {
        App.bus().post(new ErrorEvent(R.string.no_lights_found));
      } else {
        startRocking();
      }
    }
  }
}
