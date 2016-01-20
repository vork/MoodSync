package cz.destil.moodsync.event;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import java.util.List;

/**
 * Created by benediktboss on 19/01/16.
 */
public class BridgeFoundEvent {
  public List<PHAccessPoint> list;

  public BridgeFoundEvent(List<PHAccessPoint> list) {
    this.list = list;
  }
}
