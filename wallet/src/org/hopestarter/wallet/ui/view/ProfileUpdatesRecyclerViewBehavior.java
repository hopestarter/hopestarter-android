package org.hopestarter.wallet.ui.view;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by Adrian on 31/01/2016.
 */
public class ProfileUpdatesRecyclerViewBehavior extends CoordinatorLayout.Behavior<ViewGroup> {
    private static final String TAG = "ProfUpdRecyViewBeh";

    public ProfileUpdatesRecyclerViewBehavior(Context context, AttributeSet attrs) {}

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ViewGroup recyclerView, View dependency) {
        return dependency instanceof RelativeLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ViewGroup child, View dependency) {
        updateChildOffset(parent, child, dependency);
        return true;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, ViewGroup child, int layoutDirection) {
        List<View> dependencies = parent.getDependencies(child);

        if (dependencies.size() == 0) {
            return false;
        }

        View profileLayout = dependencies.get(0);
        parent.onLayoutChild(child, layoutDirection);
        updateChildOffset(parent, child, profileLayout);
        return true;
    }

    public void updateChildOffset(CoordinatorLayout parent, ViewGroup child, View dependency) {
        child.setTop(dependency.getTop() + dependency.getHeight());
    }

}
