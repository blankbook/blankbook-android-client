package com.example.jacob.blankbookandroidclient;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.jacob.blankbookandroidclient.adapters.MainDrawerRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.adapters.PostListRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.animations.ElevationAnimation;
import com.example.jacob.blankbookandroidclient.animations.MainActivityAnimator;
import com.example.jacob.blankbookandroidclient.api.models.Group;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.LocalGroupsManger;
import com.example.jacob.blankbookandroidclient.managers.PostListManager;
import com.example.jacob.blankbookandroidclient.utils.Callback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private final long ACTIVITY_ENTRY_ANIMATION_TIME = 200;

    private MainActivityAnimator animator;
    private Menu menu;
    private PostListManager postListManager;
    private LocalGroupsManger localGroupsManager;
    private MainDrawerRecyclerViewAdapter drawerAdapter;
    private ActionBar bar;
    private Set<String> selectedGroups = new HashSet<>();
    private Callback deleteCallback;
    private boolean onMainFeed = false;

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

        postListRefresh.setColorSchemeResources(R.color.accent, R.color.primary, R.color.primaryDark);
        animator = new MainActivityAnimator(this);
        animator.setupListeners();

        postList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        setupDrawer();
        setupPostListRefresh();
        setFabToComment();
        setupPostList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (animator.isDrawerExpanded()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animator.reverseExpandDrawerToClearScreen();
                }
            }, ACTIVITY_ENTRY_ANIMATION_TIME);
        }
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
        this.menu = menu;
        if (onMainFeed) {
            menuOptionRemoveHide();
        }
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
        postList.setAdapter(new PostListRecyclerViewAdapter(postListManager, new PostListRecyclerViewAdapter.OnClickListener() {
            @Override
            public void onClick(Post post, View view) {
                goToPostActivity(post, view);
            }
        }));
        selectMainFeed();
    }

    private void goToPostActivity(Post post, View postView) {
        Intent intent = new Intent(MainActivity.this, PostActivity.class);
        intent.putExtra(PostActivity.POST_TAG, post);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View statusBar = findViewById(android.R.id.statusBarBackground);
            View navigationBar = findViewById(android.R.id.navigationBarBackground);

            Pair[] pairs = {
                    new Pair<>(postView, getResources().getString(R.string.post_info_transition)),
                    new Pair<>(fab, getResources().getString(R.string.fab_transition)),
                    new Pair<>(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME),
                    new Pair<>(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)
            };

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(MainActivity.this, pairs);

            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
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
        animator.expandDrawerToClearScreen(new Callback() {
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

    public void onGroupSearchDialogResult(Group group) {
        selectGroup(group.Name);
        setFabToAddGroup(group);
    }

    private void selectMainFeed() {
        onMainFeed = true;
        menuOptionRemoveHide();
        deleteCallback = null;
        selectedGroups = new HashSet<>(LocalGroupsManger.getInstance().getGroups());
        setActivityTitle(getResources().getString(R.string.main_feed));
        drawerAdapter.highlightMainFeed();
        refreshPostList();
    }

    private void selectFeed(final String feed) {
        onMainFeed = false;
        menuOptionRemoveShow();
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
        onMainFeed = false;
        menuOptionRemoveShow();
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

    private void menuOptionRemoveHide() {
        if (menu != null) { menu.findItem(R.id.action_remove).setVisible(false); }
    }

    private void menuOptionRemoveShow() {
        if (menu != null) { menu.findItem(R.id.action_remove).setVisible(true); }
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

        animator.animatePostListRefreshStateEnter();

        postListManager.updatePostList(selectedGroups, null, null, null, "rank", null, null, null,
                new PostListManager.OnUpdate() {
                    @Override
                    public void onSuccess() {
                        if (onUpdate != null) { onUpdate.onSuccess(); }
                        animator.animatePostListRefreshStateExit();
                    }

                    @Override
                    public void onFailure() {
                        if (onUpdate != null) { onUpdate.onFailure(); }
                        animator.animatePostListRefreshStateExit();
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
        animator.fadeTransitionViewProperties(title, new Callback() {
            @Override
            public void run() {
                bar.setTitle(newTitle);
            }
        });
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
}
