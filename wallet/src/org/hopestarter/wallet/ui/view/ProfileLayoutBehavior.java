package org.hopestarter.wallet.ui.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

/**
 * Created by Adrian on 31/01/2016.
 */
public class ProfileLayoutBehavior extends CoordinatorLayout.Behavior<RelativeLayout> {
    private int mStartTop;
    private ScrollerCompat mScroller;

    private static final String TAG = "ProfileLayoutBehavior";
    private FlingRunnable mFlingRunnable;

    public ProfileLayoutBehavior(Context context, AttributeSet attrs) {}

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, RelativeLayout child,
            View directTargetChild, View target, int nestedScrollAxes) {
        if ((nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0) {
            return true;
        }

        return false;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, RelativeLayout child,
                                     View target, int dx, int dy, int[] consumed) {
        if (dy <= 0) {
            return;
        }

        consumed[1] = scrollChildView(child, dy);

    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout layout, RelativeLayout child, View target, float velocityX, float velocityY) {
        if (velocityY <= 0 || child.getTop() == mStartTop - child.getHeight()) {
            return false;
        }

        return flingChildView(layout, child, velocityY);
    }

    @Override
    public boolean onNestedFling(final CoordinatorLayout layout,
            final RelativeLayout child, View target, float velocityX, float velocityY,
            boolean consumed) {
        Log.d(TAG, "NestedFling consumed: " + Boolean.toString(consumed));
        if (consumed || velocityY >= 0 || child.getTop() == mStartTop) {
            return false;
        }

        boolean flung = flingChildView(layout, child, velocityY);
        target.invalidate();
        return flung;
    }

    private boolean flingChildView(CoordinatorLayout layout, RelativeLayout child, float velocityY) {
        if (mFlingRunnable != null) {
            layout.removeCallbacks(mFlingRunnable);
            mFlingRunnable = null;
        }

        if (mScroller == null) {
            mScroller = ScrollerCompat.create(layout.getContext());
        }

        mScroller.fling(
                0, 0, // curr
                0, Math.round(velocityY), // velocity.
                0, 0, // x
                child.getTop() + child.getHeight(), -child.getTop()); // y

        if (mScroller.computeScrollOffset()) {
            mFlingRunnable = new FlingRunnable(layout, child);
            ViewCompat.postOnAnimation(layout, mFlingRunnable);
            return true;
        }
        return false;
    }


    private int scrollChildView(RelativeLayout child, int dy) {
        int yOffset = dy;
        int newTop = child.getTop() - yOffset;

        if (newTop > mStartTop) {
            yOffset += newTop;
        }

        if (newTop < -child.getHeight()) {
            yOffset -= (-child.getHeight() - newTop);
        }

        int oldTop = child.getTop();
        ViewCompat.offsetTopAndBottom(child, -yOffset);

        return oldTop - child.getTop(); // Consumed dy
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, RelativeLayout child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);
        mStartTop = child.getTop();
        return true;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, RelativeLayout child,
                               View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        if (dyConsumed == 0 && dyUnconsumed < 0) {
            scrollChildView(child, dyUnconsumed);
        }
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, RelativeLayout abl,
                                   View target) {
        Log.d(TAG, "Stopped scroll");
    }

    private class FlingRunnable implements Runnable {
        private final CoordinatorLayout mParent;
        private final RelativeLayout mLayout;

        FlingRunnable(CoordinatorLayout parent, RelativeLayout child) {
            mParent = parent;
            mLayout = child;
        }

        @Override
        public void run() {
            if (mLayout != null && mScroller != null && mScroller.computeScrollOffset()) {
                scrollChildView(mLayout, mScroller.getCurrY());

                // Post ourselves so that we run on the next animation
                ViewCompat.postOnAnimation(mLayout, this);
            }
        }
    }

}
