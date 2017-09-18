package com.example.jacob.blankbookandroidclient.viewholders;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.adapters.CommentReplyListRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.api.models.Comment;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.ContributorIdManager;
import com.example.jacob.blankbookandroidclient.managers.VoteManager;
import com.example.jacob.blankbookandroidclient.utils.AugmentedComment;
import com.example.jacob.blankbookandroidclient.utils.SimpleCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.graphics.Color.parseColor;

public class CommentViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.score)
    TextView score;
    @BindView(R.id.replies)
    RecyclerView replies;
    @BindView(R.id.color)
    View color;
    @BindView(R.id.post_info)
    RelativeLayout postInfo;
    @BindView(R.id.reply)
    Button reply;
    @BindView(R.id.upvote_btn)
    Button upvote;
    @BindView(R.id.downvote_btn)
    Button downvote;

    private View view;
    private OnReplyClickListener replyClickListener;
    private float voteBtnHiAlpha;
    private float voteBtnLwAlpha;
    private int vote;
    private int initialVote;

    public CommentViewHolder(View view, OnReplyClickListener replyClickedListener) {
        super(view);
        ButterKnife.bind(this, view);
        this.view = view;
        this.replyClickListener = replyClickedListener;
        TypedValue out = new TypedValue();
        view.getContext().getResources().getValue(R.dimen.hi_alpha, out, true);
        voteBtnHiAlpha = out.getFloat();
        view.getContext().getResources().getValue(R.dimen.lw_alpha, out, true);
        voteBtnLwAlpha = out.getFloat();
    }

    public void setComment(final AugmentedComment comment) {
        content.setText(comment.Content);
        score.setText(String.valueOf(comment.Score));
        replies.setLayoutManager(new LinearLayoutManager(view.getContext()));
        replies.setAdapter(new CommentReplyListRecyclerViewAdapter(comment.Replies, replyClickListener));
        try {
            color.setBackgroundColor(parseColor("#" + comment.Color));
            postInfo.setBackgroundColor(parseColor("#0b" + comment.Color));
        } catch (IllegalArgumentException e) {
        }
        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replyClickListener.onReplyClicked(comment);
            }
        });
        comment.setUpdateListener(new SimpleCallback() {
            @Override
            public void run() {
                content.setText(comment.Content);
                score.setText(String.valueOf(comment.Score));
                replies.getAdapter().notifyDataSetChanged();
            }
        });
        initialVote = VoteManager.getInstance().getCommentVote(comment.ParentPost, comment.ID);
        setVote(initialVote, comment);
        upvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vote == 1) {
                    setVote(0, comment);
                } else {
                    setVote(1, comment);
                }
            }
        });
        downvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vote == -1) {
                    setVote(0, comment);
                } else {
                    setVote(-1, comment);
                }
            }
        });

    }


    private void setVote(final int newVote, final Comment comment) {
        final int oldVote = vote;
        vote = newVote;
        updateUIForVote(vote, comment);
        ContributorIdManager.getInstance().getContributorId(comment.ParentPost, new ContributorIdManager.OnContributorIdRetrievalListener() {
            @Override
            public void onRetrieval(Integer contributorId) {
                VoteManager.getInstance().putCommentVote(comment.ParentPost, comment.ID, contributorId, newVote, new VoteManager.OnVoteUpdateComplete() {
                    @Override
                    public void onComplete(int vote) {

                    }

                    @Override
                    public void onFailure(int vote) {
                        updateUIForVote(oldVote, comment);
                    }
                });
            }

            @Override
            public void onFailure() {
                updateUIForVote(oldVote, comment);
            }
        });
    }

    private void updateUIForVote(int vote, Comment comment) {
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
        score.setText(String.valueOf(comment.Score + vote - initialVote));
    }

    public interface OnReplyClickListener {
        void onReplyClicked(AugmentedComment parentComment);
    }
}
