package cz.destil.moodsync.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.otto.Subscribe;
import cz.destil.moodsync.R;
import cz.destil.moodsync.core.App;
import cz.destil.moodsync.core.Config;
import cz.destil.moodsync.event.ErrorEvent;
import cz.destil.moodsync.event.LocalColorEvent;
import cz.destil.moodsync.event.SuccessEvent;
import cz.destil.moodsync.light.LocalColorSwitcher;
import cz.destil.moodsync.light.MirroringHelper;
import cz.destil.moodsync.service.LightsService;
import cz.destil.moodsync.service.hue.HueLightsService;

public class HueFragment extends Fragment {
    @Bind(R.id.container) LinearLayout vContainer;
    @Bind(R.id.name)
    TextView vName;
    @Bind(R.id.progress_layout)
    LinearLayout vProgressLayout;
    @Bind(R.id.progress_bar) ProgressBar vProgressBar;
    @Bind(R.id.progress_text)
    TextView vProgressText;
    @Bind(R.id.control) ToggleButton vButton;
    private LocalColorSwitcher mColorSwitcher;
    MirroringHelper mMirroring;

    public HueFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColorSwitcher = LocalColorSwitcher.get();
        mMirroring = MirroringHelper.get();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_lifx, container, false);

        ButterKnife.bind(this, rootView);

        hideProgress();

        vName.setText(getString(R.string.hue_name));

        vContainer.setBackgroundColor(mColorSwitcher.getPreviousColor());
        vButton.setChecked(mMirroring.isRunning());

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mColorSwitcher.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mColorSwitcher.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.bus().register(this);
    }

    @Override
    public void onDetach() {
        App.bus().unregister(this);
        super.onDetach();
    }

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

    private void stop() {
        Intent intent = new Intent(getActivity(), LightsService.class);
        intent.setAction("STOP");
        getActivity().startService(intent);
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

    @Subscribe
    public void onError(ErrorEvent event) {
        showError(event.textRes);
    }

    @Subscribe
    public void onSuccess(SuccessEvent event) {
        hideProgress();
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
        Intent intent = new Intent(getActivity(), HueLightsService.class);
        intent.setAction("START");
        getActivity().startService(intent);
    }
}
