package com.example.jacob.blankbookandroidclient;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.adapters.CommentListRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.animations.ElevationAnimation;
import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.Comment;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.CommentListManager;
import com.example.jacob.blankbookandroidclient.utils.SimpleCallback;
import com.example.jacob.blankbookandroidclient.viewholders.CommentViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        commentList.setAdapter(new CommentListRecyclerViewAdapter(post, commentListManager, new CommentViewHolder.OnReplyClickListener() {
            @Override
            public void onReplyClicked(final Comment parentComment, final SimpleCallback onReplyAdded) {
                addReplyThroughDialog(parentComment, onReplyAdded);
            }
        }));
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        commentList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewCommentThroughDialog();
            }
        });
    }

    private void addReplyThroughDialog(final Comment parentComment, final SimpleCallback onReplyAdded) {
        final CommentDialogFragment dialogFragment = new CommentDialogFragment();
        dialogFragment.setOnResult(new OnCommentDialogResultListener() {
            @Override
            public void onAccept(String content) {
                Comment newComment = addComment(parentComment.ParentPost, parentComment.ID, content);
                parentComment.Replies.add(newComment);
                if (onReplyAdded != null) {
                    onReplyAdded.run();
                }
                dialogFragment.dismiss();
            }

            @Override
            public void onCancel() {
                dialogFragment.dismiss();
            }
        });
        dialogFragment.show(getFragmentManager(), "tag");
    }

    private void addNewCommentThroughDialog() {
        final CommentDialogFragment dialogFragment = new CommentDialogFragment();
        dialogFragment.setOnResult(new OnCommentDialogResultListener() {
            @Override
            public void onAccept(String content) {
                Comment newComment = addComment(post.ID, null, content);
                commentListManager.addCommentToList(newComment);
                dialogFragment.dismiss();
            }

            @Override
            public void onCancel() {
                dialogFragment.dismiss();
            }
        });
        dialogFragment.show(getFragmentManager(), "tag");
    }

    private Comment addComment(Long parentPost, @Nullable Long parentComment, String content) {
        Comment newComment = new Comment();
        // TODO: get contributor ID and use that to get color, put in some sort of manager
        newComment.ParentPost = parentPost;
        if (parentComment != null) {
            newComment.ParentComment = parentComment;
        }
        newComment.Content = content;
        newComment.Color = "639df9";
        RetrofitClient.getInstance().getBlankBookAPI().postPostComment(newComment); // TODO: Handle failures by removing the comment, showing an error
        return newComment;
    }

    public interface OnCommentDialogResultListener {
        void onAccept(String content);
        void onCancel();
    }
}
