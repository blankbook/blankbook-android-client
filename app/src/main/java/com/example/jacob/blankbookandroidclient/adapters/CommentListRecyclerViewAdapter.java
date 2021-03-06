package com.example.jacob.blankbookandroidclient.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.CommentListManager;
import com.example.jacob.blankbookandroidclient.viewholders.CommentViewHolder;

public class CommentListRecyclerViewAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private final Post post;
    private final CommentListManager commentListManager;
    private final CommentListManager.UpdateListener updateListener;
    private final CommentViewHolder.OnReplyClickListener replyClickedListener;

    public CommentListRecyclerViewAdapter(Post post, CommentListManager commentListManager, CommentViewHolder.OnReplyClickListener replyClickedListener) {
        this.post = post;
        this.commentListManager = commentListManager;
        this.replyClickedListener = replyClickedListener;
        updateListener = new CommentListManager.UpdateListener() {
            @Override
            public void onUpdate() {
                notifyDataSetChanged();
            }
        };
        this.commentListManager.addRootCommentsUpdateListener(updateListener);
    }

    @Override
    public int getItemCount() {
        return commentListManager.getCommentList().size();
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_info, parent, false);
        return new CommentViewHolder(view, replyClickedListener);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        holder.setComment(commentListManager.getCommentList().get(position));
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        commentListManager.removeRootCommentsUpdateListener(updateListener);
    }
}
