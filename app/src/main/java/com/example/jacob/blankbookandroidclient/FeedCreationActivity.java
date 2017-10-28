package com.example.jacob.blankbookandroidclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jacob.blankbookandroidclient.adapters.GroupSelectionRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.managers.LocalGroupsManger;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedCreationActivity extends AppCompatActivity {
    @BindView(R.id.toptoolbar)
    Toolbar toolbar;
    @BindView(R.id.name)
    EditText name;
    @BindView(R.id.name_error)
    TextView nameError;
    @BindView(R.id.group_list)
    RecyclerView groupList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_creation);
        ButterKnife.bind(this);
        setupToolbar();
        setupGroupList();
        setupErrorDetection();
        name.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                save(((GroupSelectionRecyclerViewAdapter) groupList.getAdapter()).getSelectedGroups());
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.creation, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.none, R.anim.fade_out);
        return false;
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.new_feed));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
    }

    private void setupGroupList() {
        groupList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        groupList.setAdapter(new GroupSelectionRecyclerViewAdapter(LocalGroupsManger.getInstance().getGroupNames()));
    }

    private void setupErrorDetection() {
        name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                errorCheckName();
            }
        });
    }

    private boolean errorCheckName() {
        nameError.setText("");
        if ("".equals(name.getText().toString())) {
            return false;
        }
        String nameText = name.getText().toString();
        for (String feedName : LocalGroupsManger.getInstance().getFeeds()) {
            if (nameText.equals(feedName)) {
                nameError.setText(getResources().getString(R.string.name_taken_error));
                return false;
            }
        }
        return true;
    }

    private void save(Set<String> selectedGroups) {
        String feedName = name.getText().toString();
        if (errorCheckName()) {
            LocalGroupsManger.getInstance().addFeed(feedName, selectedGroups);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(MainActivity.NEW_FEED_NAME_TAG, feedName);
            setResult(Activity.RESULT_OK, resultIntent);
            finishAndAnimate();
        } else {
            showFeedCreationError();
        }
    }

    private void showFeedCreationError() {
        Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.error_creating_feed),
                Toast.LENGTH_SHORT).show();
    }

    private void finishAndAnimate() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            // give time for everything to redraw after the keyboard collapses
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    overridePendingTransition(R.anim.none, R.anim.fade_out);
                }
            }, 50L);
        } else {
            finish();
            overridePendingTransition(R.anim.none, R.anim.fade_out);
        }
    }

}
