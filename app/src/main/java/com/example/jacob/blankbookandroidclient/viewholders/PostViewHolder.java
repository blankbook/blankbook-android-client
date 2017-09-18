package com.example.jacob.blankbookandroidclient.viewholders;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.adapters.PostListRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.animations.ElevationAnimation;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.ContributorIdManager;
import com.example.jacob.blankbookandroidclient.managers.VoteManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.group_name)
    TextView groupName;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.score)
    TextView score;
    @BindView(R.id.upvote_btn)
    Button upvote;
    @BindView(R.id.downvote_btn)
    Button downvote;

    private View view;
    private PostListRecyclerViewAdapter.OnClickListener clickListener;
    private int vote = 0;
    private final float voteBtnLwAlpha;
    private final float voteBtnHiAlpha;
    private int initialVote = 0;

    public PostViewHolder(View view, PostListRecyclerViewAdapter.OnClickListener clickListener) {
        super(view);
        ButterKnife.bind(this, view);
        this.view = view;
        this.clickListener = clickListener;
        TypedValue out = new TypedValue();
        view.getContext().getResources().getValue(R.dimen.hi_alpha, out, true);
        voteBtnHiAlpha = out.getFloat();
        view.getContext().getResources().getValue(R.dimen.lw_alpha, out, true);
        voteBtnLwAlpha = out.getFloat();
    }

    public void setPost(final Post post, boolean showGroupName) {
        groupName.setText(post.GroupName);
        title.setText(post.Title);
        content.setText(post.Content);
        score.setText(String.valueOf(post.Score));
        title.setMaxLines(2);
        content.setMaxLines(3);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onClick(post, view);
            }
        });
        initialVote = VoteManager.getInstance().getPostVote(post.ID);
        setVote(initialVote, post);
        upvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vote == 1) {
                    setVote(0, post);
                } else {
                    setVote(1, post);
                }
            }
        });
        downvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vote == -1) {
                    setVote(0, post);
                } else {
                    setVote(-1, post);
                }
            }
        });
        if (showGroupName) {
            groupName.setVisibility(View.VISIBLE);
        } else {
            groupName.setVisibility(View.GONE);
        }
    }

    private void setVote(final int newVote, final Post post) {
        final int oldVote = vote;
        vote = newVote;
        updateUIForVote(vote, post);
        ContributorIdManager.getInstance().getContributorId(post.ID, new ContributorIdManager.OnContributorIdRetrievalListener() {
            @Override
            public void onRetrieval(Integer contributorId) {
                VoteManager.getInstance().putPostVote(post.ID, contributorId, newVote, new VoteManager.OnVoteUpdateComplete() {
                    @Override
                    public void onComplete(int vote) {

                    }

                    @Override
                    public void onFailure(int vote) {
                        updateUIForVote(oldVote, post);
                    }
                });
            }

            @Override
            public void onFailure() {
                updateUIForVote(oldVote, post);
            }
        });
    }

    private void updateUIForVote(int vote, Post post) {
        switch (vote) {
            case 0:
                upvote.setAlpha(voteBtnLwAlpha);
                downvote.setAlpha(voteBtnLwAlpha);
                break;
            case 1:
                upvote.setAlpha(voteBtnHiAlpha);
                downvote.setAlpha(voteBtnLwAlpha);
                break;
            case -1:
                upvote.setAlpha(voteBtnLwAlpha);
                downvote.setAlpha(voteBtnHiAlpha);
                break;

        }
        score.setText(String.valueOf(post.Score + vote - initialVote));
    }
}
