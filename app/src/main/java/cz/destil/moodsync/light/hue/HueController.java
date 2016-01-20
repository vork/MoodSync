package cz.destil.moodsync.light.hue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.hue.sdk.utilities.impl.Color;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import cz.destil.moodsync.R;
import cz.destil.moodsync.activity.MainActivity;
import cz.destil.moodsync.core.App;
import cz.destil.moodsync.data.Lamp;
import cz.destil.moodsync.event.BridgeFoundEvent;
import cz.destil.moodsync.event.ErrorEvent;
import cz.destil.moodsync.event.PressBridgeButtonEvent;
import cz.destil.moodsync.light.ColorExtractor;
import cz.destil.moodsync.light.LightsController;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benediktboss on 19/01/16.
 */
public class HueController extends LightsController {
  private PHHueSDK mPhHueSDK;
  private static HueController sInstance;
  private FragmentActivity mActivity = null;
  private PHBridge mBridge;

  List<PHLight> leftLights = new ArrayList<>();
  List<PHLight> rightLights = new ArrayList<>();
  List<PHLight> centerLights = new ArrayList<>();
  List<PHLight> allLights = new ArrayList<>();

  public void setActivity(FragmentActivity mActivity) {
    this.mActivity = mActivity;
  }

  public static HueController get() {
    if (sInstance == null) {
      sInstance = new HueController();
    }
    return sInstance;
  }


  @Override public void changeColor(int color, @ColorExtractor.ColorExtractMode int extractMode) {
    if(extractMode == ColorExtractor.COLOR_EXTRACT_CENTER) {
      for(PHLight light : centerLights) {

        float xy[] = PHUtilities.calculateXYFromRGB(Color.red(color), Color.green(color), Color.blue(color), light.getModelNumber());
        PHLightState lightState = new PHLightState();
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        mBridge.updateLightState(light, lightState);
      }
    } else if (extractMode == ColorExtractor.COLOR_EXTRACT_LEFT) {
      for(PHLight light : leftLights) {
        float xy[] = PHUtilities.calculateXYFromRGB(Color.red(color), Color.green(color), Color.blue(color), light.getModelNumber());
        PHLightState lightState = new PHLightState();
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        mBridge.updateLightState(light, lightState);
      }
    } else if (extractMode == ColorExtractor.COLOR_EXTRACT_RIGHT) {
      for(PHLight light : rightLights) {
        float xy[] = PHUtilities.calculateXYFromRGB(Color.red(color), Color.green(color), Color.blue(color), light.getModelNumber());
        PHLightState lightState = new PHLightState();
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        mBridge.updateLightState(light, lightState);
      }
    } else if (extractMode == ColorExtractor.COLOR_EXTRACT_ALL) {
      for(PHLight light : allLights) {
        float xy[] = PHUtilities.calculateXYFromRGB(Color.red(color), Color.green(color), Color.blue(color), light.getModelNumber());
        PHLightState lightState = new PHLightState();
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        mBridge.updateLightState(light, lightState);
      }
    }
  }

  @Override public void start() {
    mBridge = mPhHueSDK.getSelectedBridge();

    if(mBridge == null) {
      throw new RuntimeException("No bridge selected");
    }

    List<PHLight> lights = mBridge.getResourceCache().getAllLights();

    ((MainActivity) mActivity).updateLights();

    Realm realm = Realm.getInstance(mActivity);

    RealmResults<Lamp> centerLamps = realm.where(Lamp.class).equalTo("groupAssigned", ColorExtractor.COLOR_EXTRACT_CENTER).findAll();
    RealmResults<Lamp> leftLamps = realm.where(Lamp.class).equalTo("groupAssigned", ColorExtractor.COLOR_EXTRACT_LEFT).findAll();
    RealmResults<Lamp> rightLamps = realm.where(Lamp.class).equalTo("groupAssigned", ColorExtractor.COLOR_EXTRACT_RIGHT).findAll();
    RealmResults<Lamp> allLamps = realm.where(Lamp.class).equalTo("groupAssigned", ColorExtractor.COLOR_EXTRACT_ALL).findAll();

    for(PHLight light : lights) {
      for(Lamp centerLight : centerLamps) {
        if(centerLight.getId().equals(light.getUniqueId())) {
          centerLights.add(light);
        }
      }

      for(Lamp leftLight : leftLamps) {
        if(leftLight.getId().equals(light.getUniqueId())) {
          leftLights.add(light);
        }
      }

      for(Lamp rightLight : rightLamps) {
        if(rightLight.getId().equals(light.getUniqueId())) {
          rightLights.add(light);
        }
      }

      for(Lamp allLight : allLamps) {
        if(allLight.getId().equals(light.getUniqueId())) {
          allLights.add(light);
        }
      }
    }

    realm.close();
  }

  @Override
  public void stop() {
    super.stop();
    mPhHueSDK.disableAllHeartbeat();
  }

  @Override public void signalStop() {
    List<PHLight> lights = mBridge.getResourceCache().getAllLights();

    int color = App.get().getResources().getColor(android.R.color.white);

    for(PHLight light : lights) {
      if(light.getLastKnownLightState().isOn()) {
        float xy[] = PHUtilities.calculateXYFromRGB(Color.red(color), Color.green(color), Color.blue(color), light.getModelNumber());
        PHLightState lightState = new PHLightState();
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        lightState.setBrightness((int) (254 * 0.4f));
        mBridge.updateLightState(light, lightState);
      }
    }
  }

  @Override public void init() {
    super.init();
    mPhHueSDK = PHHueSDK.getInstance();
  }

}
