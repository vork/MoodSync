package cz.destil.moodsync.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.model.PHLight;
import cz.destil.moodsync.R;
import cz.destil.moodsync.data.Lamp;
import io.realm.Realm;
import java.util.List;

/**
 * Created by benediktboss on 19/01/16.
 */
public class HueLightAdapter extends BaseAdapter {
  private LayoutInflater mInflater;
  private List<PHLight> lights;
  private Context mContext;

  class LampListItem {
    private TextView lampName;
    private TextView groupAssigned;
    private LinearLayout layout;
  }

  public HueLightAdapter(Context context, List<PHLight> lights) {
    // Cache the LayoutInflate to avoid asking for a new one each time.
    mInflater = LayoutInflater.from(context);
    this.lights = lights;
    this.mContext = context;
  }

  public View getView(final int position, View convertView, ViewGroup parent) {

    final LampListItem item;

    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.lamp_select_item, null);

      item = new LampListItem();
      item.lampName = (TextView) convertView.findViewById(R.id.lamp_name);
      item.groupAssigned = (TextView) convertView.findViewById(R.id.extract_mode);
      item.layout = (LinearLayout) convertView.findViewById(R.id.layout);

      convertView.setTag(item);
    } else {
      item = (LampListItem) convertView.getTag();
    }
    final PHLight light = lights.get(position);
    item.lampName.setText(light.getName());

    item.layout.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.extract_mode_prompt)
            .setItems(R.array.color_extract_modes, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                Realm realm = Realm.getInstance(mContext);

                Log.w(getClass().getCanonicalName(), "Selected item " + which);

                Lamp newLamp = new Lamp();
                newLamp.setId(light.getUniqueId());
                newLamp.setGroupAssigned(which);
                realm.beginTransaction();

                realm.copyToRealmOrUpdate(newLamp);

                realm.commitTransaction();

                item.groupAssigned.setText(mContext.getResources().getStringArray(R.array.color_extract_modes)[which]);

                realm.close();
              }
            });
        builder.create().show();
      }
    });

    Realm realm = Realm.getInstance(mContext);
    Lamp lampDb = realm.where(Lamp.class).equalTo("id", light.getUniqueId()).findFirst();

    if(lampDb != null) {
      item.groupAssigned.setText(mContext.getResources().getStringArray(R.array.color_extract_modes)[lampDb.getGroupAssigned()]);
      Log.d(getClass().getCanonicalName(), light.getName() + " is " + mContext.getResources().getStringArray(R.array.color_extract_modes)[lampDb.getGroupAssigned()]);
    }
    realm.close();

    return convertView;
  }

  /**
   * Get the row id associated with the specified position in the list.
   *
   * @param position  The row index.
   * @return          The id of the item at the specified position.
   */
  @Override
  public long getItemId(int position) {
    return 0;
  }

  /**
   * How many items are in the data set represented by this Adapter.
   *
   * @return Count of items.
   */
  @Override
  public int getCount() {
    return lights.size();
  }

  /**
   * Get the data item associated with the specified position in the data set.
   *
   * @param position      Position of the item whose data we want within the adapter's data set.
   * @return              The data at the specified position.
   */
  @Override
  public Object getItem(int position) {
    return lights.get(position);
  }

  public void updateData(List<PHLight> lights) {
    this.lights = lights;
    notifyDataSetChanged();
  }

  @Override
  public boolean isEnabled(int position) {
    //Set a Toast or Log over here to check.
    return true;
  }

}
