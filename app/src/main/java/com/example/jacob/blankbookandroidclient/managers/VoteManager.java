package com.example.jacob.blankbookandroidclient.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseArray;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.ContributorId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoteManager {
    private static VoteManager instance;
    private SharedPreferences sharedPrefs;
    private String votePrefix;
    private String POST = "post";
    private String COMMENT = "comment";

    public static VoteManager getInstance() {
        if (instance == null) {
            instance = new VoteManager();
        }
        return instance;
    }

    public void init(Context context) {
        final String fileKey = context.getResources().getString(R.string.preferences_file_key);
        votePrefix = context.getResources().getString(R.string.preferences_vote_prefix);
        sharedPrefs = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
    }

    public Integer getPostVote(Long id) {
        return sharedPrefs.getInt(getPostVotePath(id), 0);
    }

    public Integer getCommentVote(Long postId, Long commentId) {
        return sharedPrefs.getInt(getCommentVotePath(postId, commentId), 0);
    }

    public void putPostVote(final Long postId, final Integer contributorId, final Integer vote, final OnVoteUpdateComplete onComplete) {
        RetrofitClient.getInstance().getBlankBookAPI().putPostVote(contributorId, postId, vote)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        sharedPrefs.edit().putInt(getPostVotePath(postId), vote).apply();
                        if (onComplete != null) {
                            onComplete.onComplete(vote);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (onComplete != null) {
                            onComplete.onFailure(getPostVote(postId));
                        }
                    }
                });
    }

    public void putCommentVote(final Long postId, final Long commentId, final Integer contributorId, final Integer vote, final OnVoteUpdateComplete onComplete) {
        RetrofitClient.getInstance().getBlankBookAPI().putPostCommentVote(contributorId, postId, commentId, vote)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        sharedPrefs.edit().putInt(getCommentVotePath(postId, commentId), vote).apply();
                        if (onComplete != null) {
                            onComplete.onComplete(vote);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (onComplete != null) {
                            onComplete.onFailure(getCommentVote(postId, commentId));
                        }
                    }
                });
    }

    private String getPostVotePath(Long id) {
        return votePrefix + POST + String.valueOf(id);
    }

    private String getCommentVotePath(Long postId, Long commentId) {
        return votePrefix + COMMENT + String.valueOf(postId) + ":" + String.valueOf(commentId);
    }

    public interface OnVoteUpdateComplete {
        void onComplete(int vote);
        void onFailure(int vote);
    }
}
