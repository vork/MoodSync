package cz.destil.moodsync.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.destil.moodsync.R;
import cz.destil.moodsync.fragment.HueFragment;
import cz.destil.moodsync.fragment.LifxFragment;

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
