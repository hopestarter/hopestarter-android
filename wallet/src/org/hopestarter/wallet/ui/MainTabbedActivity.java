package org.hopestarter.wallet.ui;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.hopestarter.wallet.WalletApplication;
import org.hopestarter.wallet.data.UserInfoPrefs;
import org.hopestarter.wallet.ui.view.IconFragmentPagerAdapter;
import org.hopestarter.wallet_test.R;

public class MainTabbedActivity extends AbstractWalletActivity implements WalletFragment.OnFragmentInteractionListener{

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private Handler mHandler = new Handler();
    private TabLayout mTabLayout;
    private ImageView mTipPointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ViewGroup rootLayout = (ViewGroup)getLayoutInflater().inflate(R.layout.activity_main_tabbed, null);
        final ViewTreeObserver vto = rootLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                setPointerPosition();
            }
        });

        setContentView(rootLayout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(null);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        final String[] tabTitles = {
                getString(R.string.title_fragment_wallet),
                getString(R.string.title_sendrequest_fragment),
                getString(R.string.title_world_fragment) ,
                getString(R.string.title_profile_fragment)
        };

        setToolbarTitle(tabTitles[0]);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                setToolbarTitle(tabTitles[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mTipPointer = (ImageView)findViewById(R.id.tip_pointer);

        SharedPreferences preferences = getSharedPreferences(UserInfoPrefs.PREF_FILE, MODE_PRIVATE);
        if (preferences.getBoolean(UserInfoPrefs.FIRST_TIME, true)) {
            final RelativeLayout oneTimeStartupTip = (RelativeLayout)findViewById(R.id.onetime_startup_tip);
            final RelativeLayout tipWrapper = (RelativeLayout)findViewById(R.id.tip_wrapper);
            tipWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oneTimeStartupTip.setVisibility(View.GONE);
                    mTabLayout.getTabAt(2).select();
                }
            });
            oneTimeStartupTip.setVisibility(View.VISIBLE);
            Button tipCloseBtn = (Button)findViewById(R.id.onetime_tip_close_btn);
            tipCloseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oneTimeStartupTip.setVisibility(View.GONE);
                }
            });
            preferences.edit()
                    .putBoolean(UserInfoPrefs.FIRST_TIME, false)
                    .apply();
        }

        setSupportActionBar(mToolbar);
    }

    private void setPointerPosition() {
        View tabView = ((ViewGroup)mTabLayout.getChildAt(0)).getChildAt(2);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTipPointer.getLayoutParams();
        params.setMargins(tabView.getLeft() + (tabView.getWidth() / 2) - (mTipPointer.getWidth() / 2), params.topMargin, params.rightMargin, params.bottomMargin);
        mTipPointer.setLayoutParams(params);
        mTipPointer.requestLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((WalletApplication)getApplication()).startBlockchainService(true);
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    public void setToolbarTitle(CharSequence title) {
        mToolbar.setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    public class SectionsPagerAdapter extends IconFragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return createWalletFragment();
                case 1:
                    return createSendReceiveFragment();
                case 2:
                    return createWorldUpdatesFragment();
                case 3:
                    return createProfileFragment();
                default:
                    return null;
            }
        }

        private ProfileFragment createProfileFragment() {
            return ProfileFragment.newInstance();
        }

        private WalletFragment createWalletFragment() {
            return WalletFragment.newInstance();
        }

        private WorldUpdatesFragment createWorldUpdatesFragment() { return WorldUpdatesFragment.newInstance(); }

        private SendReceiveFragment createSendReceiveFragment() { return SendReceiveFragment.newInstance();}

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        @SuppressWarnings("deprecation")
        @Override
        public Drawable getPageIcon(int position) {
            switch (position) {
                case 0:
                    return getResources().getDrawable(R.drawable.wallet_tab_icon);
                case 1:
                    return getResources().getDrawable(R.drawable.sendreceive_tab_icon);
                case 2:
                    return getResources().getDrawable(R.drawable.world_tab_icon);
                case 3:
                    return getResources().getDrawable(R.drawable.profile_tab_icon);
                default:
                    return null;
            }
        }
    }
}
