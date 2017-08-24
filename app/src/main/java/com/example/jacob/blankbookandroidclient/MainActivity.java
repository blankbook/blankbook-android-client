package com.example.jacob.blankbookandroidclient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.jacob.blankbookandroidclient.adapters.MainDrawerRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.root) DrawerLayout root;
    @BindView(R.id.left_drawer) RecyclerView drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Toolbar
        setSupportActionBar(toolbar);
        final ActionBar bar = getSupportActionBar();

        // Drawer
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
        feeds.add("feed12");
        feeds.add("feed13");
        groups.add("group1");
        groups.add("group2");
        groups.add("group3");
        groups.add("group21");
        groups.add("group22");
        groups.add("group23");
        groups.add("group31");
        groups.add("group32");
        groups.add("group33");
        // END TESTING ONLY

        drawer.setLayoutManager(new LinearLayoutManager(drawer.getContext()));
        MainDrawerRecyclerViewAdapter adapter = new MainDrawerRecyclerViewAdapter(feeds, groups,
            new MainDrawerRecyclerViewAdapter.OnSelect() {

                @Override
                public void onMainFeedSelect() {
                    bar.setTitle(getString(R.string.main_feed));
                    root.closeDrawer(Gravity.START);
                }

                @Override
                public void onFeedSelect(String name) {
                    bar.setTitle(name);
                    root.closeDrawer(Gravity.START);
                }

                @Override
                public void onNewFeedSelect() {
                    bar.setTitle(getString(R.string.new_feed));
                    root.closeDrawer(Gravity.START);
                }

                @Override
                public void onGroupSelect(String name) {
                    bar.setTitle(name);
                    root.closeDrawer(Gravity.START);
                }

                @Override
                public void onNewGroupSelect() {
                    bar.setTitle(getString(R.string.new_group));
                    root.closeDrawer(Gravity.START);
                }
            });
        adapter.highlightMainFeed();
        bar.setTitle(getString(R.string.main_feed));
        drawer.setAdapter(adapter);
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
