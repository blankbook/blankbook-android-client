package com.example.jacob.blankbookandroidclient.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.PostListManager;
import com.example.jacob.blankbookandroidclient.viewholders.PostViewHolder;

public class PostListRecyclerViewAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private PostListManager postListManager;
    private PostListManager.UpdateListener postListener;
    private boolean showGroupName = true;
    private OnClickListener clickListener;

    public PostListRecyclerViewAdapter(PostListManager postListManager, OnClickListener clickListener) {
        this.postListManager = postListManager;
        postListener = new PostListManager.UpdateListener() {
            @Override
            public void onUpdate() {
                Log.d("PostListRecyclerView", "got update");
                PostListRecyclerViewAdapter.this.notifyDataSetChanged();
            }
        };
        this.postListManager.addListener(postListener);
        this.clickListener = clickListener;
    }

    public void setShowGroupName(boolean showGroupName) {
        this.showGroupName = showGroupName;
    }

    @Override
    public int getItemCount() {
        return postListManager.getPostList().size();
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_info, parent, false);
        return new PostViewHolder(view, showGroupName, clickListener);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        holder.setPost(postListManager.getPostList().get(position));
    }

    public interface OnClickListener {
        void onClick(Post post, View view);
    }
}
