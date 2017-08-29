package com.example.jacob.blankbookandroidclient;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
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
import com.example.jacob.blankbookandroidclient.api.models.Group;
import com.example.jacob.blankbookandroidclient.managers.LocalGroupsManger;
import com.example.jacob.blankbookandroidclient.managers.PostListManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    @BindView(R.id.loading_icon)
    FrameLayout loadingIcon;
    @BindView(R.id.post_list_refresh)
    SwipeRefreshLayout postListRefresh;

    private PostListManager postListManager;
    private LocalGroupsManger localGroupsManager;
    private MainDrawerRecyclerViewAdapter postListAdapter;
    private ActionBar bar;
    private List<String> selectedGroups = new ArrayList<>();

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

        setupLoadingSpinner();
        setupPostList();
        setupDrawer();
        setupPostListRefresh();
        setupAnimations();
        setFabToComment();
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

        // TESTING
        selectedGroups.add("group");
        selectedGroups.add("mygroup");
        postListManager.updatePostList(selectedGroups, 20L, 30L, null, "rank", null, null, null, null);
        // END TESTING
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, root, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        // TESTING ONLY
        List<String> feeds = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        feeds.add("feed1");
        feeds.add("feed2");
        feeds.add("feed3");
        feeds.add("feed01");
        feeds.add("feed02");
        feeds.add("feed03");
        feeds.add("feed11");
        groups.add("mgroup");
        groups.add("adfsasdf");
        groups.add("group");
        groups.add("mygroup");
        // END TESTING ONLY

        drawer.setLayoutManager(new LinearLayoutManager(drawer.getContext()));
        postListAdapter = new MainDrawerRecyclerViewAdapter(localGroupsManager,
                new MainDrawerRecyclerViewAdapter.OnSelect() {
                    @Override
                    public void onMainFeedSelect() {
                        animateTitleChange(getString(R.string.main_feed));
                        closeDrawer();
                        setFabToComment();
                    }

                    @Override
                    public void onFeedSelect(String name) {
                        animateTitleChange(name);
                        closeDrawer();
                        setFabToComment();
                    }

                    @Override
                    public void onNewFeedSelect() {
                        animateTitleChange(getString(R.string.add_feed));
                        closeDrawer();
                        setFabToComment();
                    }

                    @Override
                    public void onGroupSelect(String name) {
                        setSelectedGroup(name);
                        animateTitleChange(name);
                        closeDrawer();
                        setFabToComment();
                    }
                });
        postListAdapter.highlightMainFeed();
        bar.setTitle(getString(R.string.main_feed));
        drawer.setAdapter(postListAdapter);
    }

    private void setupLoadingSpinner() {
        postListManager.addListener(new PostListManager.UpdateListener() {
            @Override
            public void onUpdate() {
                loadingIcon.setVisibility(View.GONE);
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
    }

    public void onGroupSearchDialogResult(Group group) {
        setSelectedGroup(group.Name);
        animateTitleChange(group.Name);
        postListAdapter.clearHighlight();
        setFabToAddGroup(group);
    }

    private void setSelectedGroup(String group) {
        setSelectedGroups(Collections.singletonList(group));
    }

    private void setSelectedGroups(List<String> groups) {
        selectedGroups = groups;
        loadingIcon.setVisibility(View.VISIBLE);
        refreshPostList();
    }

    private void refreshPostList() {
        refreshPostList(null);
    }

    private void refreshPostList(PostListManager.OnUpdate onUpdate) {
        if (selectedGroups.size() == 1) {
            ((PostListRecyclerViewAdapter) postList.getAdapter()).setShowGroupName(false);
        } else {
            ((PostListRecyclerViewAdapter) postList.getAdapter()).setShowGroupName(true);
        }
        postListManager.updatePostList(selectedGroups, null, null, null, "rank", null, null, null, onUpdate);
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

    private void animateTitleChange(final String newTitle) {
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
                addGroupAndGo(targetGroup);
            }
        });
    }

    private void addGroupAndGo(final Group group) {
        localGroupsManager.addGroup(group.Name);
        drawer.getAdapter().notifyDataSetChanged();
        setSelectedGroup(group.Name);
        setFabToComment();
    }
}
