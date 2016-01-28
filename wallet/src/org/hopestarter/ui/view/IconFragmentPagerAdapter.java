package org.hopestarter.ui.view;

import android.support.v4.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Adrian on 28/01/2016.
 */
public abstract class IconFragmentPagerAdapter extends FragmentPagerAdapter {
    public IconFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public abstract Drawable getPageIcon(int position);
}
