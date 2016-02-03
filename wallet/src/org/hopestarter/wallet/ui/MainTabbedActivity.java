package org.hopestarter.wallet.ui;

import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import org.hopestarter.wallet.WalletApplication;
import org.hopestarter.wallet.ui.view.IconFragmentPagerAdapter;
import org.hopestarter.wallet_test.R;

public class MainTabbedActivity extends AppCompatActivity implements WalletFragment.OnFragmentInteractionListener{

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabbed);

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

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        setSupportActionBar(mToolbar);
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
