package com.example.jacob.blankbookandroidclient.viewholders;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.adapters.CommentReplyListRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.api.models.Comment;

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
    LinearLayout postInfo;

    private View view;

    public CommentViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        this.view = view;
    }

    public void setComment(Comment comment) {
        content.setText(comment.Content);
        score.setText(String.valueOf(comment.Score));
        replies.setLayoutManager(new LinearLayoutManager(view.getContext()));
        replies.setAdapter(new CommentReplyListRecyclerViewAdapter(comment.Replies));
        try {
            color.setBackgroundColor(parseColor("#" + comment.Color));
            postInfo.setBackgroundColor(parseColor("#09" + comment.Color));
        } catch (IllegalArgumentException e) {}
    }
}
