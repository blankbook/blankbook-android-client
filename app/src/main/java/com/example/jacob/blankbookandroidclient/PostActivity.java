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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jacob.blankbookandroidclient.adapters.CommentListRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.animations.ElevationAnimation;
import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.Group;
import com.example.jacob.blankbookandroidclient.api.models.IDWrapper;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.CommentListManager;
import com.example.jacob.blankbookandroidclient.managers.ContributorIdManager;
import com.example.jacob.blankbookandroidclient.managers.GroupPasswordManager;
import com.example.jacob.blankbookandroidclient.managers.PublicGroupsManager;
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
    @BindView(R.id.bottomtoolbar)
    Toolbar bottomToolbar;


    public final static String POST_TAG = "post";

    private final String[] SORT_OPTIONS = {"rank", "time"};

    private CommentListManager commentListManager;
    private Post post;
    private String sortingMethod = SORT_OPTIONS[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);

        post = (Post) getIntent().getSerializableExtra(POST_TAG);
        commentListManager = new CommentListManager();

        setupBottomToolbar();
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

    private void setupBottomToolbar() {
        Menu bottomMenu = bottomToolbar.getMenu();
        getMenuInflater().inflate(R.menu.sorting, bottomMenu);
        MenuItem filterItem = bottomMenu.findItem(R.id.filter_spinner);
        Spinner filterSpinner = (Spinner) filterItem.getActionView();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.sort_spinner_list_item_black, SORT_OPTIONS);
        adapter.setDropDownViewResource(R.layout.sort_spinner_list_item_black);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setSelection(0);
        sortingMethod = SORT_OPTIONS[0];

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setSortingMethod(SORT_OPTIONS[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setSortingMethod(String method) {
        if (!method.equals(sortingMethod)) {
            sortingMethod = method;
            updateCommentList(new CommentListManager.OnUpdate() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure() {
                    showCommentsUpdateFailureMessage();
                }
            });
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
                        showCommentsUpdateFailureMessage();
                    }
                });
            }
        });
    }

    private void updateCommentList(CommentListManager.OnUpdate onUpdate) {
        commentListManager.updateCommentList(post.ID, null, sortingMethod, onUpdate);
    }

    private void setupFab() {
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
            public void onAccept(final String content) {
                PublicGroupsManager.getGroup(post.GroupName, new PublicGroupsManager.OnGroupRetrieval() {
                            @Override
                            public void onRetrieval(Group group) {
                                if (group.Protected) {
                                    try {
                                        String encrypted = GroupPasswordManager.getInstance().encryptString(group.Name, group.Salt, content);
                                        addComment(parentComment, encrypted);
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), "Couldn't encrypt comment", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    addComment(parentComment, content);
                                }
                                dialogFragment.dismiss();
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(getApplicationContext(), "Error connecting to the server", Toast.LENGTH_SHORT).show();
                            }
                        });
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

    private void showCommentsUpdateFailureMessage() {
        Toast.makeText(this, getResources().getString(R.string.could_not_refresh_comments), Toast.LENGTH_SHORT).show();
    }
}
