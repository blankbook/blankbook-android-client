package com.example.jacob.blankbookandroidclient.viewholders;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.adapters.CommentReplyListRecyclerViewAdapter;
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

    private View view;
    private OnReplyClickListener replyClickListener;

    public CommentViewHolder(View view, OnReplyClickListener replyClickedListener) {
        super(view);
        ButterKnife.bind(this, view);
        this.view = view;
        this.replyClickListener = replyClickedListener;
    }

    public void setComment(final AugmentedComment comment) {
        content.setText(comment.Content);
        score.setText(String.valueOf(comment.Score));
        replies.setLayoutManager(new LinearLayoutManager(view.getContext()));
        replies.setAdapter(new CommentReplyListRecyclerViewAdapter(comment.Replies, replyClickListener));
        try {
            color.setBackgroundColor(parseColor("#" + comment.Color));
            postInfo.setBackgroundColor(parseColor("#0b" + comment.Color));
        } catch (IllegalArgumentException e) {}
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
    }

    public interface OnReplyClickListener {
        void onReplyClicked(AugmentedComment parentComment);
    }
}
