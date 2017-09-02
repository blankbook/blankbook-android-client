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
import android.transition.TransitionManager;
import android.view.ViewGroup;
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
    public static final int GROUP_CREATION_ACTIVITY_ID = 0;

    private PostListManager postListManager;
    private LocalGroupsManger localGroupsManager;
    private MainDrawerRecyclerViewAdapter postListAdapter;
    private ActionBar bar;
    private Set<String> selectedGroups = new HashSet<>();
    private Set<Callback> runOnNextResume = new HashSet<>();

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
            callback.onComplete();
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
        postListAdapter = new MainDrawerRecyclerViewAdapter(localGroupsManager,
                new MainDrawerRecyclerViewAdapter.OnSelect() {
                    @Override
                    public void onMainFeedSelect(View view) {
                        selectMainFeed();
                        closeDrawer();
                        setFabToComment();
                    }

                    @Override
                    public void onFeedSelect(View view, String name) {
                        selectFeed(name, LocalGroupsManger.getInstance().getFeedGroups(name));
                        closeDrawer();
                        setFabToComment();
                    }

                    @Override
                    public void onNewFeedSelect(View view) {
                    }

                    @Override
                    public void onGroupSelect(View view, String name) {
                        selectGroup(name);
                        closeDrawer();
                        setFabToComment();
                    }

                    @Override
                    public void onNewGroupSelect(View view) {
                        runOnNextResume.add(new Callback() {
                            @Override
                            public void onComplete() {
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
                            public void onComplete() {
                                Intent intent = new Intent(MainActivity.this, GroupCreationActivity.class);
                                startActivityForResult(intent, GROUP_CREATION_ACTIVITY_ID);
                                overridePendingTransition(R.anim.fade_in, R.anim.none);
                            }
                        });
                    }
                });
        postListAdapter.highlightMainFeed();
        bar.setTitle(getString(R.string.main_feed));
        drawer.setAdapter(postListAdapter);
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
        ((PostListRecyclerViewAdapter) postList.getAdapter()).setShowGroupName(true);
        selectedGroups = new HashSet<>(LocalGroupsManger.getInstance().getGroups());
        setActivityTitle(getResources().getString(R.string.main_feed));
        postListAdapter.highlightMainFeed();
        refreshPostList();
    }

    private void selectFeed(String name, Set<String> groups) {
        ((PostListRecyclerViewAdapter) postList.getAdapter()).setShowGroupName(true);
        selectedGroups = groups;
        setActivityTitle(name);
        postListAdapter.highlightFeed(name);
        refreshPostList();
    }

    private void selectGroup(String group) {
        selectGroup(group, true);
    }

    private void selectGroup(String group, boolean refreshList) {
        ((PostListRecyclerViewAdapter) postList.getAdapter()).setShowGroupName(false);
        selectedGroups.clear();
        selectedGroups.add(group);
        postListAdapter.highlightGroup(group);
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
                postListAdapter.highlightGroup(targetGroup.Name);
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
                callback.onComplete();
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
        void onComplete();
    }
}
