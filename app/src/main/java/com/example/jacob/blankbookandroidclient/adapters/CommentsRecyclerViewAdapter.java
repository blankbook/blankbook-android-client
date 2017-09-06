package com.example.jacob.blankbookandroidclient.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.Comment;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.managers.PostListManager;
import com.example.jacob.blankbookandroidclient.viewholders.CommentViewHolder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private final Post post;
    private List<Comment> rootComments = new ArrayList<>();

    public CommentsRecyclerViewAdapter(Post post) {
        this.post = post;
        RetrofitClient.getInstance().getBlankBookAPI().getComments(post.ID, -1L, "rank")
                .enqueue(new Callback<List<Comment>>() {
                    @Override
                    public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                        rootComments = response.body();
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<List<Comment>> call, Throwable t) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return rootComments.size();
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_info, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        holder.setComment(rootComments.get(position));
    }
}
