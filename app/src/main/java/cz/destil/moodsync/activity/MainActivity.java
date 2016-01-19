package cz.destil.moodsync.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.destil.moodsync.R;
import cz.destil.moodsync.core.App;
import cz.destil.moodsync.core.Config;
import cz.destil.moodsync.event.ErrorEvent;
import cz.destil.moodsync.event.LocalColorEvent;
import cz.destil.moodsync.event.SuccessEvent;
import cz.destil.moodsync.fragment.HueFragment;
import cz.destil.moodsync.fragment.LifxFragment;
import cz.destil.moodsync.light.LocalColorSwitcher;
import cz.destil.moodsync.light.MirroringHelper;
import cz.destil.moodsync.service.LightsService;

public class MainActivity extends FragmentActivity {
    @Bind(R.id.pager) ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_switcher);
        ButterKnife.bind(this);

        Fragment[] lightFragments = new Fragment[2];
        lightFragments[0] = new LifxFragment();
        lightFragments[1] = new HueFragment();

        mPagerAdapter = new LightSystemSwitcherAdapter(getSupportFragmentManager(), lightFragments);
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class LightSystemSwitcherAdapter extends FragmentStatePagerAdapter {
        final Fragment[] mLightTypes;
        public LightSystemSwitcherAdapter(FragmentManager fm, Fragment[] lightFragments) {
            super(fm);
            mLightTypes = lightFragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mLightTypes[position];
        }

        @Override
        public int getCount() {
            return mLightTypes.length;
        }
    }
}
