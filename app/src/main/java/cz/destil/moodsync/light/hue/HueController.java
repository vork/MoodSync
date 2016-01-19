package cz.destil.moodsync.light.hue;

import com.philips.lighting.hue.sdk.PHHueSDK;
import cz.destil.moodsync.light.LightsController;

/**
 * Created by benediktboss on 19/01/16.
 */
public class HueController extends LightsController {
  private PHHueSDK phHueSDK;

  private static HueController sInstance;

  public static HueController get() {
    if (sInstance == null) {
      sInstance = new HueController();
    }
    return sInstance;
  }


  @Override public void changeColor(int color) {

  }

  @Override public void start() {

  }

  @Override public void signalStop() {

  }

  @Override public void init() {
    super.init();
  }
}
