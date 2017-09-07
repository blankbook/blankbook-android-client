package com.example.jacob.blankbookandroidclient.utils;

import com.example.jacob.blankbookandroidclient.api.models.Comment;
import com.example.jacob.blankbookandroidclient.managers.CommentListManager;

import java.util.ArrayList;
import java.util.List;

public class AugmentedComment extends Comment {
    public List<AugmentedComment> Replies = new ArrayList<>();
    private SimpleCallback updateListener;

    public AugmentedComment() {
    }

    public AugmentedComment(Comment comment) {
        ID = comment.ID;
        Score = comment.Score;
        ParentPost = comment.ParentPost;
        ParentComment = comment.ParentComment;
        Content = comment.Content;
        EditContent = comment.EditContent;
        Time = comment.Time;
        Color = comment.Color;
    }

    public void setUpdateListener(SimpleCallback updateListener) {
        this.updateListener = updateListener;
    }

    public void onUpdate() {
        if (updateListener != null) {
            updateListener.run();
        }
    }
}
