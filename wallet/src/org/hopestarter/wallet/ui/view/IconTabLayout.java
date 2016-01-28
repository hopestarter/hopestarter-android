package org.hopestarter.wallet.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;

/**
 * Created by Adrian on 28/01/2016.
 */
public class IconTabLayout extends android.support.design.widget.TabLayout {
    public IconTabLayout(Context context) {
        super(context);
    }

    public IconTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTabsFromPagerAdapter(@NonNull PagerAdapter adapter) {
        removeAllTabs();
        if (adapter instanceof IconFragmentPagerAdapter) {
            setTabsFromIconFragmentPagerAdapter((IconFragmentPagerAdapter)adapter);
        } else {
            setTabsFromBasePagerAdapter(adapter);
        }
    }

    private void setTabsFromBasePagerAdapter(@NonNull PagerAdapter adapter) {
        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            addTab(newTab().setText(adapter.getPageTitle(i)));
        }
    }

    private void setTabsFromIconFragmentPagerAdapter(@NonNull IconFragmentPagerAdapter adapter) {
        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            addTab(newTab().setText(adapter.getPageTitle(i)).setIcon(adapter.getPageIcon(i)));
        }
    }
}
