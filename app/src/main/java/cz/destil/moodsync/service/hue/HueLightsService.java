package cz.destil.moodsync.service.hue;

import cz.destil.moodsync.light.hue.HueController;
import cz.destil.moodsync.service.LightsService;

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

  }

  @Override protected void stop() {

  }
}
