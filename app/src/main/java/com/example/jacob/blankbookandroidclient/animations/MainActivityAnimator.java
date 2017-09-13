package com.example.jacob.blankbookandroidclient.animations;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.utils.SimpleCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivityAnimator {
    @BindView(R.id.toptoolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.root)
    DrawerLayout root;
    @BindView(R.id.left_drawer)
    RecyclerView drawer;
    @BindView(R.id.post_list)
    RecyclerView postList;
    @BindView(R.id.post_list_refresh)
    SwipeRefreshLayout postListRefresh;
    @BindView(R.id.drawer_wrapper)
    FrameLayout drawerWrapper;

    final private int shortAnimTime;


    public MainActivityAnimator(Activity activity) {
        ButterKnife.bind(this, activity);
        shortAnimTime = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    public void setupListeners() {
        postList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    fab.hide();
                } else {
                    fab.show();
                }
            }
        });
    }

    public void expandDrawerToClearScreen(final SimpleCallback callback) {
        Animation alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(shortAnimTime);
        alphaAnimation.setFillAfter(true);
        drawer.startAnimation(alphaAnimation);
        Animation widthAnimation = new WidthAnimation(drawerWrapper, root.getWidth());
        widthAnimation.setDuration(shortAnimTime);
        widthAnimation.setInterpolator(new FastOutSlowInInterpolator());
        widthAnimation.setFillAfter(true);
        widthAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                callback.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        drawerWrapper.startAnimation(widthAnimation);
    }

    public boolean isDrawerExpanded() {
        return drawerWrapper.getWidth() == root.getWidth() && root.getWidth() != 0;
    }

    public void reverseExpandDrawerToClearScreen() {
        Animation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setDuration(shortAnimTime);
        drawer.startAnimation(alphaAnimation);
        Animation widthAnimation = new WidthAnimation(drawerWrapper, drawer.getWidth());
        widthAnimation.setDuration(shortAnimTime);
        widthAnimation.setFillAfter(true);
        widthAnimation.setInterpolator(new FastOutSlowInInterpolator());
        widthAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                root.closeDrawer(Gravity.START);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        drawerWrapper.startAnimation(widthAnimation);
    }

    public void animatePostListRefreshStateEnter() {
        final Animation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(shortAnimTime);

        postList.startAnimation(fadeOut);
        postList.getAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                postListRefresh.setRefreshing(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    public void animatePostListRefreshStateExit() {
        final Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(shortAnimTime);

        postListRefresh.setRefreshing(false);
        if (postList.isAnimating()) {
            postList.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    postList.getAdapter().notifyDataSetChanged();
                    postList.setAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } else {
            postList.setAnimation(fadeIn);
        }
    }

    public void fadeTransitionViewProperties(final View view, final SimpleCallback transition) {
        final Animation fadeOut = new AlphaAnimation(1f, 0f);
        final Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeOut.setDuration(shortAnimTime / 2);
        fadeIn.setDuration(shortAnimTime / 2);

        view.startAnimation(fadeOut);
        view.getAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                transition.run();
                view.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }
}
