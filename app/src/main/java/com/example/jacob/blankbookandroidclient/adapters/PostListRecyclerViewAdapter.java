package com.example.jacob.blankbookandroidclient.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.PostListManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostListRecyclerViewAdapter extends RecyclerView.Adapter<PostListRecyclerViewAdapter.ViewHolder> {
    private PostListManager postListManager;
    private PostListManager.UpdateListener postListener;
    private boolean showGroupName = true;

    public PostListRecyclerViewAdapter(PostListManager postListManager) {
        this.postListManager = postListManager;
        postListener = new PostListManager.UpdateListener() {
            @Override
            public void onUpdate() {
                Log.d("PostListRecyclerView", "got update");
                PostListRecyclerViewAdapter.this.notifyDataSetChanged();
            }
        };
        this.postListManager.addListener(postListener);
    }

    public void setPostListManager(PostListManager postListManager) {
        this.postListManager.removeListener(postListener);
        this.postListManager = postListManager;
        this.postListManager.addListener(postListener);
        notifyDataSetChanged();
    }

    public void setShowGroupName(boolean showGroupName) {
        this.showGroupName = showGroupName;
    }

    @Override
    public int getItemCount() {
        return postListManager.getPostList().size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setPost(postListManager.getPostList().get(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.group_name)
        TextView groupName;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.content)
        TextView content;
        @BindView(R.id.score)
        TextView score;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void setPost(Post post) {
            groupName.setText(post.GroupName);
            title.setText(post.Title);
            content.setText(post.Content);
            score.setText(Integer.toString(post.Score));
            if (showGroupName) {
                groupName.setVisibility(View.VISIBLE);
            } else {
                groupName.setVisibility(View.GONE);
            }
        }
    }
}
