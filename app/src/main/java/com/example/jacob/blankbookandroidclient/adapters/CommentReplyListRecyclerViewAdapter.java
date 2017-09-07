package com.example.jacob.blankbookandroidclient.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.models.Comment;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.CommentListManager;
import com.example.jacob.blankbookandroidclient.viewholders.CommentViewHolder;

import java.util.List;

public class CommentReplyListRecyclerViewAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private final List<Comment> comments;
    private final CommentViewHolder.OnReplyClickListener replyClickListener;

    public CommentReplyListRecyclerViewAdapter(List<Comment> comments, CommentViewHolder.OnReplyClickListener replyClickedListener) {
        this.comments = comments;
        this.replyClickListener = replyClickedListener;
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_info, parent, false);
        return new CommentViewHolder(view, replyClickListener);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        holder.setComment(comments.get(position));
    }
}
