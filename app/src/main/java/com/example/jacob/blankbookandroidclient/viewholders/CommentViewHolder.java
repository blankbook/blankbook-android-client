package com.example.jacob.blankbookandroidclient.viewholders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.adapters.CommentsRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.api.models.Comment;
import com.example.jacob.blankbookandroidclient.api.models.Post;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.score)
    TextView score;

    private View view;

    public CommentViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        this.view = view;
    }

    public void setComment(Comment comment) {
        content.setText(comment.Content);
        score.setText(String.valueOf(comment.Score));
    }
}
