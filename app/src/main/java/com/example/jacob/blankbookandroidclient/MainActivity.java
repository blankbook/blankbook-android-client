package com.example.jacob.blankbookandroidclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.jacob.blankbookandroidclient.adapters.MainDrawerRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.adapters.PostListRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.animations.WidthAnimation;
import com.example.jacob.blankbookandroidclient.api.models.Group;
import com.example.jacob.blankbookandroidclient.managers.LocalGroupsManger;
import com.example.jacob.blankbookandroidclient.managers.PostListManager;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
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

    public static final String NEW_GROUP_NAME_TAG = "NewGroupName";
    public static final String NEW_FEED_NAME_TAG = "NewFeedName";
    public static final int GROUP_CREATION_ACTIVITY_ID = 0;
    public static final int FEED_CREATION_ACTIVITY_ID = 1;

    private PostListManager postListManager;
    private LocalGroupsManger localGroupsManager;
    private MainDrawerRecyclerViewAdapter drawerAdapter;
    private ActionBar bar;
    private Set<String> selectedGroups = new HashSet<>();
    private Set<Callback> runOnNextResume = new HashSet<>();
    private Callback deleteCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        postListManager = new PostListManager();
        setSupportActionBar(toolbar);
        bar = getSupportActionBar();

        localGroupsManager = LocalGroupsManger.getInstance();
        localGroupsManager.init(this);

        setupDrawer();
        setupPostListRefresh();
        setupAnimations();
        setFabToComment();
        setupPostList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (Callback callback : runOnNextResume) {
            callback.run();
        }
        runOnNextResume = new HashSet<>();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GROUP_CREATION_ACTIVITY_ID:
                if (data != null && data.hasExtra(NEW_GROUP_NAME_TAG)) {
                    String group = data.getStringExtra(NEW_GROUP_NAME_TAG);
                    selectGroup(group);
                    postListManager.emptyPostList();
                }
                break;
            case FEED_CREATION_ACTIVITY_ID:
                if (data != null && data.hasExtra(NEW_FEED_NAME_TAG)) {
                    String feed = data.getStringExtra(NEW_FEED_NAME_TAG);
                    selectFeed(feed);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (root.isDrawerOpen(GravityCompat.START)) {
            root.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_remove) {
            if (deleteCallback != null) {
                deleteCallback.run();
            }
        } else if (id == R.id.search) {
            GroupSearchDialogFragment dialogFragment = new GroupSearchDialogFragment();
            dialogFragment.show(getFragmentManager(), "tag");
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupPostList() {
        postList.setLayoutManager(new LinearLayoutManager(postList.getContext()));
        postList.setAdapter(new PostListRecyclerViewAdapter(postListManager));
        selectMainFeed();
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, root, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        drawer.setLayoutManager(new LinearLayoutManager(drawer.getContext()));
        drawerAdapter = new MainDrawerRecyclerViewAdapter(localGroupsManager,
                new MainDrawerRecyclerViewAdapter.OnSelect() {
                    @Override
                    public void onMainFeedSelect(View view) {
                        selectMainFeed();
                        closeDrawer();
                        setFabToComment();
                    }

                    @Override
                    public void onFeedSelect(View view, String name) {
                        selectFeed(name);
                        closeDrawer();
                        setFabToComment();
                    }

                    @Override
                    public void onNewFeedSelect(View view) {
                        startActivityFromDrawer(FeedCreationActivity.class, FEED_CREATION_ACTIVITY_ID);
                    }

                    @Override
                    public void onGroupSelect(View view, String name) {
                        selectGroup(name);
                        closeDrawer();
                        setFabToComment();
                    }

                    @Override
                    public void onNewGroupSelect(View view) {
                        startActivityFromDrawer(GroupCreationActivity.class, GROUP_CREATION_ACTIVITY_ID);
                    }
                });
        drawerAdapter.highlightMainFeed();
        bar.setTitle(getString(R.string.main_feed));
        drawer.setAdapter(drawerAdapter);
    }

    private void startActivityFromDrawer(final Class activityClass, final int activityId) {
        runOnNextResume.add(new Callback() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reverseAnimateFromDrawerToBlankScreen();
                    }
                }, getResources().getInteger(android.R.integer.config_shortAnimTime));
            }
        });
        animateFromDrawerToBlankScreen(new Callback() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, activityClass);
                startActivityForResult(intent, activityId);
                overridePendingTransition(R.anim.fade_in, R.anim.none);
            }
        });
    }

    private void setupPostListRefresh() {
        postListRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPostList(new PostListManager.OnUpdate() {
                    @Override
                    public void onSuccess() {
                        postListRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onFailure() {
                        postListRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void setupAnimations() {
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

        postListRefresh.setColorSchemeResources(R.color.accent, R.color.primary, R.color.primaryDark);
    }

    public void onGroupSearchDialogResult(Group group) {
        selectGroup(group.Name);
        setFabToAddGroup(group);
    }

    private void selectMainFeed() {
        deleteCallback = null;
        selectedGroups = new HashSet<>(LocalGroupsManger.getInstance().getGroups());
        setActivityTitle(getResources().getString(R.string.main_feed));
        drawerAdapter.highlightMainFeed();
        refreshPostList();
    }

    private void selectFeed(final String feed) {
        deleteCallback = new Callback() {
            @Override
            public void run() {
                LocalGroupsManger.getInstance().removeFeed(feed);
                drawerAdapter.notifyDataSetChanged();
                selectMainFeed();
            }
        };
        selectedGroups = LocalGroupsManger.getInstance().getFeedGroups(feed);
        setActivityTitle(feed);
        drawerAdapter.highlightFeed(feed);
        refreshPostList();
    }

    private void selectGroup(String group) {
        selectGroup(group, true);
    }

    private void selectGroup(final String group, boolean refreshList) {
        deleteCallback = new Callback() {
            @Override
            public void run() {
                LocalGroupsManger.getInstance().removeGroup(group);
                drawerAdapter.notifyDataSetChanged();
                selectMainFeed();
            }
        };
        selectedGroups.clear();
        selectedGroups.add(group);
        drawerAdapter.highlightGroup(group);
        setActivityTitle(group);
        if (refreshList) {
            refreshPostList();
        }
    }

    private void refreshPostList() {
        refreshPostList(null);
    }

    private void refreshPostList(final PostListManager.OnUpdate onUpdate) {
        if (selectedGroups.size() == 1) {
            ((PostListRecyclerViewAdapter) postList.getAdapter()).setShowGroupName(false);
        } else {
            ((PostListRecyclerViewAdapter) postList.getAdapter()).setShowGroupName(true);
        }

        final Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(200);
        final Animation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(200);

        postList.startAnimation(fadeOut);
        postList.getAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // LOADING ICON
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        postListManager.updatePostList(selectedGroups, null, null, null, "rank", null, null, null,
                new PostListManager.OnUpdate() {
                    @Override
                    public void onSuccess() {
                        if (onUpdate != null) {
                            onUpdate.onSuccess();
                        }
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
                            // LOADING ICON
                            postList.setAnimation(fadeIn);
                        }
                    }

                    @Override
                    public void onFailure() {
                        postList.setVisibility(View.VISIBLE);
                        if (onUpdate != null) {
                            onUpdate.onFailure();
                        }
                        if (postList.isAnimating()) {
                            postList.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
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
                });
    }

    private void closeDrawer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (root != null) {
                    root.closeDrawer(Gravity.START);
                }
            }
        }, 250);
    }

    private void setActivityTitle(final String newTitle) {
        if (newTitle.equals(bar.getTitle().toString())) {
            return;
        }
        final View title = toolbar.getChildAt(0);

        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(100);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bar.setTitle(newTitle);
                AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(100);
                title.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        title.startAnimation(fadeOut);
    }

    private void setFabToComment() {
        fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_comment));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Add a comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFabToAddGroup(final Group targetGroup) {
        fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_add));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalGroupsManger.getInstance().addGroup(targetGroup.Name);
                drawerAdapter.highlightGroup(targetGroup.Name);
                setFabToComment();
            }
        });
    }

    private void animateFromDrawerToBlankScreen(final Callback callback) {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
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
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                callback.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        drawerWrapper.startAnimation(widthAnimation);
    }

    private void reverseAnimateFromDrawerToBlankScreen() {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
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

    private interface Callback {
        void run();
    }
}
