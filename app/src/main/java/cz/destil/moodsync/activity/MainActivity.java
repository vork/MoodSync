package cz.destil.moodsync.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.squareup.otto.Subscribe;
import cz.destil.moodsync.R;
import cz.destil.moodsync.core.App;
import cz.destil.moodsync.core.Config;
import cz.destil.moodsync.event.ErrorEvent;
import cz.destil.moodsync.event.LocalColorEvent;
import cz.destil.moodsync.light.LocalColorSwitcher;
import cz.destil.moodsync.light.MirroringHelper;
import cz.destil.moodsync.light.hue.HueController;
import cz.destil.moodsync.adapter.HueLightAdapter;
import cz.destil.moodsync.service.hue.HueLightsService;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity {
    @Bind(R.id.container) LinearLayout vContainer;
    @Bind(R.id.name) TextView vName;
    @Bind(R.id.progress_layout)
    LinearLayout vProgressLayout;
    @Bind(R.id.progress_bar) ProgressBar vProgressBar;
    @Bind(R.id.progress_text)
    TextView vProgressText;
    @Bind(R.id.control) ToggleButton vButton;
    @Bind(R.id.listView) ListView vLightList;
    private LocalColorSwitcher mColorSwitcher;
    MirroringHelper mMirroring;

    private PHHueSDK phHueSDK;

    private HueLightAdapter mLightAdapter;

    private HueController mHueController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);
        App.bus().register(this);
        phHueSDK = PHHueSDK.create();
        mColorSwitcher = LocalColorSwitcher.get();
        mMirroring = MirroringHelper.get();

        hideProgress();

        mHueController = HueController.get();
        mHueController.setActivity(this);

        vName.setText(Html.fromHtml(getString(R.string.hue_name)));

        vContainer.setBackgroundColor(mColorSwitcher.getPreviousColor());
        vButton.setChecked(mMirroring.isRunning());

        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> lights = bridge.getResourceCache().getAllLights();

        mLightAdapter =  new HueLightAdapter(this, lights);

        vLightList.setItemsCanFocus(true);
        vLightList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.w(getClass().getCanonicalName(), "TEST " + position);
            }
        });
        vLightList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.w(getClass().getCanonicalName(), "Selected " + position);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {
                Log.w(getClass().getCanonicalName(), "Nothing selected");
            }
        });

        vLightList.setAdapter(mLightAdapter);

        vButton.setEnabled(phHueSDK.getSelectedBridge() != null);
    }

    public void updateLights() {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> lights = bridge.getResourceCache().getAllLights();

        mLightAdapter.updateData(lights);
    }

    @Override
    protected void onDestroy() {
        App.bus().unregister(this);
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
            super.onDestroy();
        }
    }

    @OnClick(R.id.control)
    public void controlButtonClicked() {
        if (mMirroring.isRunning()) {
            stop();
        } else {
            showProgress(R.string.connecting);
            mMirroring.askForPermission(this);
        }
    }

    @Subscribe
    public void onNewLocalColor(LocalColorEvent event) {
        ColorDrawable[] colors = {new ColorDrawable(event.previousColor), new ColorDrawable(event.newColor)};
        TransitionDrawable trans = new TransitionDrawable(colors);
        vContainer.setBackground(trans);
        trans.startTransition(Config.DURATION_OF_COLOR_CHANGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != MirroringHelper.PERMISSION_CODE) {
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            App.bus().post(new ErrorEvent(R.string.give_permission));
            return;
        }

        mMirroring.permissionGranted(resultCode, data);
        Intent intent = new Intent(this, HueLightsService.class);
        intent.setAction("START");
        startService(intent);
    }

    private void stop() {
        Intent intent = new Intent(this, HueLightsService.class);
        intent.setAction("STOP");
        startService(intent);
    }

    PHLightListener listener = new PHLightListener() {

        @Override
        public void onSuccess() {
        }

        @Override
        public void onStateUpdate(Map<String, String> arg0, List<PHHueError> arg1) {
            Log.w(this.getClass().getCanonicalName(), "Light has updated");
        }

        @Override
        public void onError(int arg0, String arg1) {}

        @Override
        public void onReceivingLightDetails(PHLight arg0) {}

        @Override
        public void onReceivingLights(List<PHBridgeResource> arg0) {}

        @Override
        public void onSearchComplete() {}
    };

    private void showProgress(int textResId) {
        vProgressLayout.setVisibility(View.VISIBLE);
        vProgressBar.setVisibility(View.VISIBLE);
        vProgressText.setText(textResId);
        vButton.setVisibility(View.GONE);
    }

    private void hideProgress() {
        vProgressLayout.setVisibility(View.GONE);
        vButton.setVisibility(View.VISIBLE);
    }

    private void showError(int textResId) {
        vProgressLayout.setVisibility(View.VISIBLE);
        vProgressBar.setVisibility(View.GONE);
        vProgressText.setText(textResId);
        vButton.setVisibility(View.GONE);
        stop();
    }
}
