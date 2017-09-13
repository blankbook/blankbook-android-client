package com.example.jacob.blankbookandroidclient.api;

import com.example.jacob.blankbookandroidclient.api.models.Comment;
import com.example.jacob.blankbookandroidclient.api.models.ContributorId;
import com.example.jacob.blankbookandroidclient.api.models.Group;
import com.example.jacob.blankbookandroidclient.api.models.IDWrapper;
import com.example.jacob.blankbookandroidclient.api.models.Post;
import com.example.jacob.blankbookandroidclient.api.models.RankedPosts;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface BlankBookAPI {
    @GET("content/read/posts")
    Call<RankedPosts> getPosts(
        @Query("groupname") Set<String> groupNames,
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
    Call<ContributorId> getContributorid(
        @Query("postid") Long postId
    );

    @POST("content/write/post")
    Call<IDWrapper> postPost(
        @Body Post post
    );

    @PUT("content/write/post/vote")
    Call<Void> putPostVote(
        @Query("userid") Long userId,
        @Query("postid") Long postId,
        @Query("vote") int vote
    );

    @POST("content/write/post/comment")
    Call<IDWrapper> postPostComment(
        @Body Comment comment
    );

    @PUT("content/write/post/comment/vote")
    Call<Void> putPostCommentVote(
        @Query("userid") Long userId,
        @Query("postid") Long postId,
        @Query("commentid") Long commentId,
        @Query("vote") Integer vote
    );

    @GET("groups/read/search")
    Call<List<Group>> getGroupSearch(
        @Query("term") String term
    );

    @GET("groups/read/group")
    Call<Group> getGroup(
        @Query("name") String name
    );

    @POST("groups/write/group")
    Call<Void> postGroup(
        @Body Group group
    );
}
