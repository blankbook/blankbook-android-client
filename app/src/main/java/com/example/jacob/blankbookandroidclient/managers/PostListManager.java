package com.example.jacob.blankbookandroidclient.managers;

import android.support.annotation.NonNull;

import com.example.jacob.blankbookandroidclient.api.BlankBookAPI;
import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.api.models.RankedPosts;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostListManager {
    private List<Post> posts = new ArrayList<>();
    private final BlankBookAPI api;
    private List<UpdateListener> listeners = new ArrayList<>();
    private Call<RankedPosts> lastCall;

    public PostListManager() {
        api = RetrofitClient.getInstance().getBlankBookAPI();
    }

    public void updatePostList(@NonNull Set<String> groupNames, Long firstRank, Long lastRank,
                               Long rankVersion, String ordering, Long firstTime, Long lastTime,
                               Integer maxCount, final OnUpdate onUpdate) {

        lastCall = api.getPosts(groupNames, firstRank, lastRank, rankVersion, ordering, firstTime, lastTime, maxCount);
        lastCall.enqueue(new Callback<RankedPosts>() {
            @Override
            public void onResponse(Call<RankedPosts> call, Response<RankedPosts> response) {
                final RankedPosts body = response.body();
                if (body == null) {
                    if (onUpdate != null) {
                        onUpdate.onFailure();
                    }
                } else {
                    posts = body.Posts;
                    notifyListeners();
                    if (onUpdate != null) {
                        onUpdate.onSuccess();
                    }
                }
            }

            @Override
            public void onFailure(Call<RankedPosts> call, Throwable t) {
                if (onUpdate != null) {
                    onUpdate.onFailure();
                }
            }
        });
    }

    public void emptyPostList() {
        lastCall.cancel();
        posts = new ArrayList<>();
        notifyListeners();
    }

    public List<Post> getPostList() {
        return posts;
    }

    public void addListener(UpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(UpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (UpdateListener listener : listeners) {
            listener.onUpdate();
        }
    }

    public interface UpdateListener {
        void onUpdate();
    }

    public interface OnUpdate {
        void onSuccess();
        void onFailure();
    }
}
