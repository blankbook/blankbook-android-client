package com.example.jacob.blankbookandroidclient.managers;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.jacob.blankbookandroidclient.api.BlankBookAPI;
import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.Group;
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
    private List<LoadStateChangedListener> loadStateChangedListeners = new ArrayList<>();
    private Call<RankedPosts> currentCall;
    private Long lastRankVersion;
    private String lastOrdering;
    private LoadState loadState = LoadState.moreAvailable;
    private Set<String> lastGroupNames;

    public static final String rankString = "rank";
    public static final String timeString = "time";
    public static final String[] SORT_OPTIONS = {rankString, timeString};

    public enum LoadState {loading, moreAvailable, noMoreAvailable}

    public PostListManager() {
        api = RetrofitClient.getInstance().getBlankBookAPI();
    }

    public void updatePostList(@NonNull final Set<String> groupNames, final String ordering, final Integer maxCount, final OnUpdate onUpdate) {
        setLoadState(LoadState.loading);
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
                if (posts.size() == maxCount) {
                    setLoadState(LoadState.moreAvailable);
                } else {
                    setLoadState(LoadState.noMoreAvailable);
                }
            }

            @Override
            public void onPostListRetrievalFailure() {
                if (onUpdate != null) {
                    onUpdate.onFailure();
                }
                // if there is a failure, we don't want to keep trying to load more posts,
                // since the loading will likely fail, so we say there are no more
                // available, and let the user force a refresh if they want
                setLoadState(LoadState.noMoreAvailable);
            }
        });
    }

    private void getPostList(@NonNull Set<String> groupNames, Long firstRank, Long lastRank,
                             Long rankVersion, Long firstTime, Long lastTime, final String ordering,
                             Integer maxCount, @NonNull final OnPostListRetrieval callback) {

        if (groupNames.size() == 0) {
            final RankedPosts body = new RankedPosts();
            body.Posts = new ArrayList<>();
            callback.onPostListRetrieval(body);
            return;
        }
        currentCall = api.getPosts(groupNames, firstRank, lastRank, rankVersion, ordering, firstTime, lastTime, maxCount);
        currentCall.enqueue(new Callback<RankedPosts>() {
            @Override
            public void onResponse(Call<RankedPosts> call, Response<RankedPosts> response) {
                currentCall = null;
                final RankedPosts body = response.body();
                if (body == null) {
                    Log.e("PostListManager", "got null body when getting posts");
                    callback.onPostListRetrievalFailure();
                } else {
                    callback.onPostListRetrieval(body);
                }
            }

            @Override
            public void onFailure(Call<RankedPosts> call, Throwable t) {
                currentCall = null;
                callback.onPostListRetrievalFailure();
                Log.e("PostListManager", "error getting post list: " + t.getMessage());
            }
        });
    }

    private class CallbackCounter {
        private int executingCallbacks = 0;

        CallbackCounter(int executingCallbacks) {
            this.executingCallbacks = executingCallbacks;
        }

        public void setExecutingCallbacks(int executingCallbacks) {
            this.executingCallbacks = executingCallbacks;
        }

        public void decrimentExecutingCallbacks() {
            --executingCallbacks;
        }

        public boolean allCallbacksComplete() {
            return executingCallbacks == 0;
        }
    }

    public void loadNextPostListChunk(final int size, final OnUpdate onUpdate) {
        setLoadState(LoadState.loading);
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
                    public void onPostListRetrieval(final RankedPosts rankedPosts) {
                        final CallbackCounter counter = new CallbackCounter(rankedPosts.Posts.size());
                        for (Post post : rankedPosts.Posts) {
                            decryptPost(post, new OnDecryption() {
                                @Override
                                public void onDecryption(Post post) {
                                    counter.decrimentExecutingCallbacks();
                                    if (counter.allCallbacksComplete()) {
                                        posts.addAll(posts.size(), rankedPosts.Posts);
                                        notifyListeners();
                                        if (onUpdate != null) {
                                            onUpdate.onSuccess();
                                        }
                                        if (rankedPosts.Posts.size() == size) {
                                            setLoadState(LoadState.moreAvailable);
                                        } else {
                                            setLoadState(LoadState.noMoreAvailable);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure() {
                                    if (onUpdate != null) {
                                        onUpdate.onFailure();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onPostListRetrievalFailure() {
                        if (onUpdate != null) {
                            onUpdate.onFailure();
                        }
                        // if there is a failure, we don't want to keep trying to load more posts,
                        // since the loading will likely fail, so we say there are no more
                        // available, and let the user force a refresh if they want
                        setLoadState(LoadState.noMoreAvailable);
                    }
                });
    }

    private void decryptPost(final Post post, final OnDecryption cb) {
        PublicGroupsManager.getGroup(post.GroupName, new PublicGroupsManager.OnGroupRetrieval() {
            @Override
            public void onRetrieval(Group group) {
                GroupPasswordManager pm = GroupPasswordManager.getInstance();
                if (pm.hasPassword(group.Name)) {
                    try {
                        Post decrypted = post;
                        decrypted.Title = pm.decryptString(group, post.Title);
                        decrypted.Content = pm.decryptString(group, post.Content);
                    } catch (Exception e) {
                        cb.onFailure();
                    }
                } else {
                    cb.onFailure();
                }
            }

            @Override
            public void onFailure() {
                cb.onFailure();
            }
        });
    }

    public boolean isLoading() {
        return currentCall != null;
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
        long lowestRank = posts.get(0).Rank;
        for (int i = 1; i < posts.size(); ++i) {
            lowestRank = Math.max(lowestRank, posts.get(i).Rank);
        }
        return lowestRank;
    }

    public void emptyPostList() {
        if (currentCall != null) {
            currentCall.cancel();
        }
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

    public void addLoadStateListener(LoadStateChangedListener listener) {
        loadStateChangedListeners.add(listener);
    }

    public void removeLoadStateListener(LoadStateChangedListener listener) {
        loadStateChangedListeners.remove(listener);
    }

    private void notifyListeners() {
        for (UpdateListener listener : listeners) {
            listener.onUpdate();
        }
    }

    private void setLoadState(LoadState newState) {
        loadState = newState;
        for (LoadStateChangedListener listener : loadStateChangedListeners) {
            listener.onLoadStateChanged(newState);
        }
    }

    public LoadState getLoadState() {
        return loadState;
    }

    public interface UpdateListener {
        void onUpdate();
    }

    public interface LoadStateChangedListener {
        void onLoadStateChanged(LoadState newState);
    }

    public interface OnUpdate {
        void onSuccess();

        void onFailure();
    }

    private interface OnPostListRetrieval {
        void onPostListRetrieval(RankedPosts rankedPosts);

        void onPostListRetrievalFailure();
    }

    private interface OnDecryption {
        void onDecryption(Post post);

        void onFailure();
    }
}
