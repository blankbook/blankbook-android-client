package com.example.jacob.blankbookandroidclient;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import com.example.jacob.blankbookandroidclient.api.models.IDWrapper;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.CommentListManager;
import com.example.jacob.blankbookandroidclient.managers.ContributorIdManager;
import com.example.jacob.blankbookandroidclient.utils.AugmentedComment;
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
            public void onReplyClicked(final AugmentedComment parentComment) {
                addCommentThroughDialog(parentComment);
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
                addCommentThroughDialog(null);
            }
        });
    }

    private void addCommentThroughDialog(final AugmentedComment parentComment) {
        final CommentDialogFragment dialogFragment = new CommentDialogFragment();
        dialogFragment.setOnResult(new CommentDialogFragment.OnCommentDialogResultListener() {
            @Override
            public void onAccept(String content) {
                addComment(parentComment, content);
                dialogFragment.dismiss();
            }

            @Override
            public void onCancel() {
                dialogFragment.dismiss();
            }
        });
        dialogFragment.show(getFragmentManager(), "tag");
    }

    private void addComment(@Nullable final AugmentedComment parentComment, final String content) {
        final AugmentedComment newComment = new AugmentedComment();
        newComment.ParentPost = post.ID;
        newComment.Content = content;

        if (parentComment == null) {
            newComment.ParentComment = -1;
        } else {
            newComment.ParentComment = parentComment.ID;
        }

        ContributorIdManager.getInstance().getContributorId(post.ID, new ContributorIdManager.OnContributorIdRetrievalListener() {
            @Override
            public void onRetrieval(Integer id) {
                newComment.Color = ContributorIdManager.getInstance().getColorForId(id);
                addComment(parentComment, newComment);
            }

            @Override
            public void onFailure() {
                onCommentAddFailure(parentComment, content);
            }
        });
    }

    private void addComment(@Nullable final AugmentedComment parentComment, final AugmentedComment newComment) {
        if (parentComment == null) {
            commentListManager.addCommentToList(newComment);
        } else {
            commentListManager.addReply(parentComment, newComment);
        }
        RetrofitClient.getInstance().getBlankBookAPI().postPostComment(newComment)
                .enqueue(new Callback<IDWrapper>() {
                    @Override
                    public void onResponse(Call<IDWrapper> call, Response<IDWrapper> response) {
                        if (response.code() == 200 && response.body() != null) {
                            newComment.ID = response.body().ID;
                            newComment.onUpdate();
                        } else {
                            onCommentAddFailure(parentComment, newComment);
                        }
                    }

                    @Override
                    public void onFailure(Call<IDWrapper> call, Throwable t) {
                        onCommentAddFailure(parentComment, newComment);
                    }
                });
    }

    private void onCommentAddFailure(final AugmentedComment parentComment, final AugmentedComment newComment) {
        if (parentComment == null) {
            commentListManager.removeCommentFromList(newComment);
        } else {
            commentListManager.removeReply(parentComment, newComment);
        }
        onCommentAddFailure(parentComment, newComment.Content);
    }

    private void onCommentAddFailure(final AugmentedComment parentComment, final String content) {
        new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.error_adding_comment))
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addComment(parentComment, content);
                    }
                })
                .setNeutralButton(R.string.copy_content, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Comment", content);
                        clipboard.setPrimaryClip(clip);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
