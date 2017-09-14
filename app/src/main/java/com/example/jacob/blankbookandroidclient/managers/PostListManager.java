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
    private Call<RankedPosts> currentCall;
    private Long lastRankVersion;
    private String lastOrdering;
    private Set<String> lastGroupNames;

    private static final String rankString = "rank";
    private static final String timeString = "time";
    public static final String[] SORT_OPTIONS = {rankString, timeString};

    public PostListManager() {
        api = RetrofitClient.getInstance().getBlankBookAPI();
    }

    public void updatePostList(@NonNull final Set<String> groupNames, final String ordering, Integer maxCount, final OnUpdate onUpdate) {
        getPostList(groupNames, null, null, null, null, null, ordering, maxCount, new OnPostListRetrieval() {
            @Override
            public void onPostListRetrieval(RankedPosts rankedPosts) {
                lastRankVersion = rankedPosts.RankVersion;
                lastOrdering = ordering;
                lastGroupNames = groupNames;
                posts = rankedPosts.Posts;
                notifyListeners();
                if (onUpdate != null) {
                    onUpdate.onSuccess();
                }
            }

            @Override
            public void onPostListRetrievalFailure() {
                if (onUpdate != null) {
                    onUpdate.onFailure();
                }
            }
        });
    }

    private void getPostList(@NonNull Set<String> groupNames, Long firstRank, Long lastRank,
                            Long rankVersion, Long firstTime, Long lastTime, final String ordering,
                            Integer maxCount, @NonNull final OnPostListRetrieval callback) {

        currentCall = api.getPosts(groupNames, firstRank, lastRank, rankVersion, ordering, firstTime, lastTime, maxCount);
        currentCall.enqueue(new Callback<RankedPosts>() {
            @Override
            public void onResponse(Call<RankedPosts> call, Response<RankedPosts> response) {
                currentCall = null;
                final RankedPosts body = response.body();
                if (body == null) {
                    callback.onPostListRetrievalFailure();
                } else {
                    callback.onPostListRetrieval(body);
                }
            }

            @Override
            public void onFailure(Call<RankedPosts> call, Throwable t) {
                currentCall = null;
                callback.onPostListRetrievalFailure();
            }
        });
    }

    public void loadNextPostListChunk(int size, final OnUpdate onUpdate) {
        if (posts.size() == 0) {
            return;
        }
        Long oldestTime = null;
        Long lowestRank = null;
        switch (lastOrdering) {
            case timeString:
                oldestTime = getOldestPostTime() + 1;
                break;
            case rankString:
                lowestRank = getLowestPostRank() + 1;
                break;
        }
        getPostList(lastGroupNames, lowestRank, null, lastRankVersion, oldestTime,
                null, lastOrdering, size, new OnPostListRetrieval() {
                    @Override
                    public void onPostListRetrieval(RankedPosts rankedPosts) {
                        posts.addAll(posts.size(), rankedPosts.Posts);
                        notifyListeners();
                        if (onUpdate != null) {
                            onUpdate.onSuccess();
                        }
                    }

                    @Override
                    public void onPostListRetrievalFailure() {
                        if (onUpdate != null) {
                            onUpdate.onFailure();
                        }
                    }
                });
    }

    public boolean isLoading() {
        return currentCall == null;
    }

    private long getOldestPostTime() {
        if (posts.size() == 0) {
            return 0;
        }
        long oldestTime = posts.get(0).Time;
        for (int i = 1; i < posts.size(); ++i) {
            oldestTime = Math.min(oldestTime, posts.get(i).Time);
        }
        return oldestTime;
    }

    private long getLowestPostRank() {
        if (posts.size() == 0) {
            return 0;
        }
        long lowestRank = posts.get(0).Time;
        for (int i = 1; i < posts.size(); ++i) {
            lowestRank = Math.max(lowestRank, posts.get(i).Rank);
        }
        return lowestRank;
    }

    public void emptyPostList() {
        currentCall.cancel();
        currentCall = null;
        posts = new ArrayList<>();
        notifyListeners();
    }

    public List<Post> getPostList() {
        return posts;
    }

    public void addPost(Post newPost) {
        posts.add(0, newPost);
        notifyListeners();
    }

    public void removePost(Post post) {
        posts.remove(post);
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

    private interface OnPostListRetrieval {
        void onPostListRetrieval(RankedPosts rankedPosts);

        void onPostListRetrievalFailure();
    }
}
