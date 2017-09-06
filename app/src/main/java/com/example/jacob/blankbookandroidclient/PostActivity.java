package com.example.jacob.blankbookandroidclient;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.adapters.CommentListRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.animations.ElevationAnimation;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.CommentListManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostActivity extends AppCompatActivity {
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.comment_list)
    RecyclerView commentList;
    @BindView(R.id.comment_list_refresh)
    SwipeRefreshLayout commentListRefresh;
    @BindView(R.id.group_name)
    TextView groupName;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.score)
    TextView score;
    @BindView(R.id.post_info)
    LinearLayout postInfo;


    public final static String POST_TAG = "post";

    private CommentListManager commentListManager;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);

        post = (Post) getIntent().getSerializableExtra(POST_TAG);
        commentListManager = new CommentListManager();

        preventScreenFlash();
        elevatePostInfo();
        populatePost();
        populateCommentList();
        setupCommentListRefresh();
        setupFab();
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Animation animation = new ElevationAnimation(postInfo, 10, 0);
            animation.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            animation.setFillAfter(true);
            postInfo.startAnimation(animation);
            finishAfterTransition();
        } else {
            finish();
        }
    }

    private void preventScreenFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();

            final View decor = getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startPostponedEnterTransition();
                    }
                    return true;
                }
            });
        }
    }

    private void elevatePostInfo() {
        Animation animation = new ElevationAnimation(postInfo, 0, 10);
        animation.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        animation.setFillAfter(true);
        postInfo.startAnimation(animation);
    }

    private void populatePost() {
        groupName.setText(post.GroupName);
        title.setText(post.Title);
        content.setText(post.Content);
        score.setText(String.valueOf(post.Score));
    }

    private void populateCommentList() {
        updateCommentList(null);
        commentList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        commentList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        commentList.setAdapter(new CommentListRecyclerViewAdapter(post, commentListManager));
    }

    private void setupCommentListRefresh() {
        commentListRefresh.setColorSchemeResources(R.color.accent, R.color.primary, R.color.primaryDark);
        commentListRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateCommentList(new CommentListManager.OnUpdate() {
                    @Override
                    public void onSuccess() {
                        commentListRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onFailure() {
                        commentListRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void updateCommentList(CommentListManager.OnUpdate onUpdate) {
        commentListManager.updateCommentList(post.ID, null, null, onUpdate);
    }

    private void setupFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
