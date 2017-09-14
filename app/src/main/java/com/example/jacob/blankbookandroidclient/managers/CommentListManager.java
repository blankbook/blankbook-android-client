package com.example.jacob.blankbookandroidclient.managers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.LongSparseArray;
import android.widget.Toast;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.BlankBookAPI;
import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.Comment;
import com.example.jacob.blankbookandroidclient.utils.AugmentedComment;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentListManager {
    private List<AugmentedComment> comments = new ArrayList<>();
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
                        if (onUpdate != null) {
                            onUpdate.onFailure();
                        }
                    }
                });
    }

    public void addCommentToList(AugmentedComment comment) {
        comments.add(0, comment);
        notifyListeners();
    }

    public void removeCommentFromList(AugmentedComment comment) {
        comments.remove(comment);
        notifyListeners();
    }


    public void addReply(AugmentedComment parent, AugmentedComment reply) {
        parent.Replies.add(0, reply);
        parent.onUpdate();
    }

    public void removeReply(AugmentedComment parent, AugmentedComment reply) {
        parent.Replies.remove(reply);
        parent.onUpdate();
    }

    public void emptyCommentList() {
        lastCall.cancel();
        comments = new ArrayList<>();
        notifyListeners();
    }

    public List<AugmentedComment> getCommentList() {
        return comments;
    }

    public void addRootCommentsUpdateListener(UpdateListener listener) {
        listeners.add(listener);
    }

    public void removeRootCommentsUpdateListener(UpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (UpdateListener listener : listeners) {
            listener.onUpdate();
        }
    }

    private List<AugmentedComment> unflattenCommentList(List<Comment> comments) {
        LongSparseArray<List<AugmentedComment>> commentReplies = new LongSparseArray<>();
        for (Comment comment : comments) {
            if (commentReplies.get(comment.ParentComment) == null) {
                commentReplies.put(comment.ParentComment, new ArrayList<AugmentedComment>());
            }
            AugmentedComment augmentedComment = new AugmentedComment(comment);
            augmentedComment.Replies = new ArrayList<>();
            commentReplies.get(comment.ParentComment).add(augmentedComment);
        }

        List<AugmentedComment> unflatenedComments = commentReplies.get(-1L);
        if (unflatenedComments == null) {
            return new ArrayList<>();
        }
        Stack<AugmentedComment> commentsThatNeedReplies = new Stack<>();
        commentsThatNeedReplies.addAll(unflatenedComments);

        while (!commentsThatNeedReplies.isEmpty()) {
            AugmentedComment commentToAddTo = commentsThatNeedReplies.pop();
            final List<AugmentedComment> replies = commentReplies.get(commentToAddTo.ID);
            if (replies != null) {
                commentToAddTo.Replies = replies;
                commentsThatNeedReplies.addAll(replies);
            }
        }

        return unflatenedComments;
    }

    public interface UpdateListener {
        void onUpdate();
    }

    public interface OnUpdate {
        void onSuccess();
        void onFailure();
    }

}
