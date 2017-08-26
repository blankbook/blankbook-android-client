package com.example.jacob.blankbookandroidclient;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.example.jacob.blankbookandroidclient.adapters.MainDrawerRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.adapters.PostListRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.PostListManager;

import java.util.ArrayList;
import java.util.Arrays;
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

    private PostListManager postListManager;
    private ActionBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        postListManager = new PostListManager();
        setSupportActionBar(toolbar);
        bar = getSupportActionBar();

        setupLoadingSpinner();
        setupPostList();
        setupDrawer();
    }

    private void setupPostList() {
        postList.setLayoutManager(new LinearLayoutManager(postList.getContext()));
        postList.setAdapter(new PostListRecyclerViewAdapter(postListManager));

        // TESTING
        List<String> groupNames = new ArrayList<>();
        groupNames.add("group");
        groupNames.add("mygroup");
        postListManager.updatePostList(groupNames, 20L, 30L, null, "rank", null, null, null, null);
        // END TESTING
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, root, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        root.setDrawerListener(toggle);
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
        MainDrawerRecyclerViewAdapter adapter = new MainDrawerRecyclerViewAdapter(feeds, groups,
                new MainDrawerRecyclerViewAdapter.OnSelect() {
                    @Override
                    public void onMainFeedSelect() {
                        bar.setTitle(getString(R.string.main_feed));
                        closeDrawer();
                    }

                    @Override
                    public void onFeedSelect(String name) {
                        bar.setTitle(name);
                        closeDrawer();
                    }

                    @Override
                    public void onNewFeedSelect() {
                        bar.setTitle(getString(R.string.new_feed));
                        closeDrawer();
                    }

                    @Override
                    public void onGroupSelect(String name) {
                        setSelectedGroup(name);
                        bar.setTitle(name);
                        closeDrawer();
                    }

                    @Override
                    public void onNewGroupSelect() {
                        bar.setTitle(getString(R.string.new_group));
                        closeDrawer();
                    }
                });
        adapter.highlightMainFeed();
        bar.setTitle(getString(R.string.main_feed));
        drawer.setAdapter(adapter);
    }

    private void setupLoadingSpinner() {
        postListManager.addListener(new PostListManager.UpdateListener() {
            @Override
            public void onUpdate() {
                loadingIcon.setVisibility(View.GONE);
            }
        });
    }

    private void setSelectedGroup(String group) {
        loadingIcon.setVisibility(View.VISIBLE);
        setSelectedGroups(Collections.singletonList(group));
    }

    private void setSelectedGroups(List<String> groups) {
        if (groups.size() == 1) {
            ((PostListRecyclerViewAdapter) postList.getAdapter()).setShowGroupName(false);
        } else {
            ((PostListRecyclerViewAdapter) postList.getAdapter()).setShowGroupName(true);
        }
        postListManager.updatePostList(groups, null, null, null, "rank", null, null, null, null);
    }

    private void closeDrawer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                root.closeDrawer(Gravity.START);
            }
        }, 250);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
