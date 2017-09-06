package com.example.jacob.blankbookandroidclient.managers;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;

import com.example.jacob.blankbookandroidclient.api.BlankBookAPI;
import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.Comment;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.api.models.RankedPosts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentListManager {
    private List<Comment> comments = new ArrayList<>();
    private final BlankBookAPI api;
    private List<UpdateListener> listeners = new ArrayList<>();
    private Call<List<Comment>> lastCall;

    public CommentListManager() {
        api = RetrofitClient.getInstance().getBlankBookAPI();
    }

    public void updateCommentList(@NonNull Long parentPost, Long parentComment, String ordering, final OnUpdate onUpdate) {
        lastCall = api.getComments(parentPost, parentComment, ordering);
        lastCall.enqueue(new Callback<List<Comment>>() {
                    @Override
                    public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                        comments = unflattenCommentList(response.body());
                        notifyListeners();
                        if (onUpdate != null) {
                            onUpdate.onSuccess();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Comment>> call, Throwable t) {
                        notifyListeners();
                        if (onUpdate != null) {
                            onUpdate.onFailure();
                        }
                    }
                });
    }

    public void emptyCommentList() {
        lastCall.cancel();
        comments = new ArrayList<>();
        notifyListeners();
    }

    public List<Comment> getCommentList() {
        return comments;
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

    private List<Comment> unflattenCommentList(List<Comment> comments) {
        LongSparseArray<List<Comment>> commentReplies = new LongSparseArray<>();
        for (Comment comment : comments) {
            if (commentReplies.get(comment.ParentComment) == null) {
                commentReplies.put(comment.ParentComment, new ArrayList<Comment>());
            }
            commentReplies.get(comment.ParentComment).add(comment);
        }

        List<Comment> unflattenedComments = commentReplies.get(-1L);
        if (unflattenedComments == null) {
            return new ArrayList<>();
        }
        Stack<Comment> commentsThatNeedReplies = new Stack<>();
        commentsThatNeedReplies.addAll(unflattenedComments);

        while (!commentsThatNeedReplies.isEmpty()) {
            Comment commentToAddTo = commentsThatNeedReplies.pop();
            final List<Comment> replies = commentReplies.get(commentToAddTo.ID);
            if (replies != null) {
                commentToAddTo.Replies = replies;
                commentsThatNeedReplies.addAll(replies);
            }
        }

        return unflattenedComments;
    }

    public interface UpdateListener {
        void onUpdate();
    }

    public interface OnUpdate {
        void onSuccess();
        void onFailure();
    }
}
