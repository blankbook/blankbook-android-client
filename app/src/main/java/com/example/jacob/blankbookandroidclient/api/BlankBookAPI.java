package com.example.jacob.blankbookandroidclient.api;

import com.example.jacob.blankbookandroidclient.api.models.Comment;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.api.models.RankedPosts;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface BlankBookAPI {
    @GET("content/read/posts")
    Call<RankedPosts> getPosts(
        @Query("groupname") List<String> groupName,
        @Query("firstrank") Long firstRank,
        @Query("lastrank") Long lastRank,
        @Query("rankversion") Long rankVersion,
        @Query("ordering") String ordering,
        @Query("firsttime") Long firstTime,
        @Query("lasttime") Long lastTime,
        @Query("maxcount") Integer maxCount
    );

    @GET("content/read/comments")
    Call<List<Comment>> getComments(
        @Query("parentpost") Long parentPost,
        @Query("parentcomment") Long parentComment,
        @Query("ordering") String ordering
    );

    @GET("content/read/contributorid")
    Call<Integer> getContributorid(
        @Query("postid") String postId
    );

    @POST("content/write/post")
    Call<Void> postPost(
        @Body Post post
    );

    @PUT("content/write/post/vote")
    Call<Void> putPostVote(
        @Query("userid") Long userId,
        @Query("postid") Long postId,
        @Query("vote") int vote
    );

    @POST("content/write/post/comment")
    Call<Void> postPostComment(
        @Body Comment comment
    );

    @PUT("content/write/post/comment/vote")
    Call<Void> putPostCommentVote(
            @Query("userid") Long userId,
            @Query("postid") Long postId,
            @Query("commentid") Long commentId,
            @Query("vote") Integer vote
    );
}
