package com.example.jacob.blankbookandroidclient.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.PostListManager;
import com.example.jacob.blankbookandroidclient.utils.SimpleCallback;
import com.example.jacob.blankbookandroidclient.viewholders.PostViewHolder;
import com.example.jacob.blankbookandroidclient.viewholders.SimpleViewHolder;

public class PostListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int POST_VIEW_TYPE = 0;
    private final int LOADING_VIEW_TYPE = 1;

    private PostListManager postListManager;
    private PostListManager.UpdateListener updateListener;
    private PostListManager.LoadStateChangedListener loadStateChangedListener;
    private boolean showGroupName = true;
    private boolean showLoadingSpinner = true;
    private OnClickListener clickListener;

    public PostListRecyclerViewAdapter(final PostListManager postListManager, OnClickListener clickListener) {
        this.postListManager = postListManager;
        updateListener = new PostListManager.UpdateListener() {
            @Override
            public void onUpdate() {
                Log.d("Log", "Calling notify dataset changed, show group name is " + showGroupName);
                notifyDataSetChanged();
            }
        };
        loadStateChangedListener = new PostListManager.LoadStateChangedListener() {
            @Override
            public void onLoadStateChanged(PostListManager.LoadState newState) {
                if (showLoadingSpinner && newState == PostListManager.LoadState.noMoreAvailable) {
                    showLoadingSpinner = false;
                    notifyItemRemoved(getItemCount() - 1);
                } else if (!showLoadingSpinner && (newState == PostListManager.LoadState.loading
                        || newState == PostListManager.LoadState.moreAvailable)) {
                    showLoadingSpinner = true;
                    notifyItemInserted(getItemCount());
                }
            }
        };
        this.postListManager.addListener(updateListener);
        this.postListManager.addLoadStateListener(loadStateChangedListener);
        this.clickListener = clickListener;
    }

    public void setShowGroupName(boolean showGroupName) {
        this.showGroupName = showGroupName;
    }

    @Override
    public int getItemCount() {
        return postListManager.getPostList().size() + (showLoadingSpinner ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < postListManager.getPostList().size()) {
            return POST_VIEW_TYPE;
        } else {
            return LOADING_VIEW_TYPE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == POST_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post_info, parent, false);
            return new PostViewHolder(view, clickListener);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.loading_tile, parent, false);
            return new SimpleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < postListManager.getPostList().size()) {
            ((PostViewHolder) holder).setPost(postListManager.getPostList().get(position), showGroupName);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        postListManager.removeListener(updateListener);
    }

    public interface OnClickListener {
        void onClick(Post post, View view);
    }
}
