package com.example.jacob.blankbookandroidclient.api.models;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    public long ID;
    public int Score;
    public long ParentPost;
    public long ParentComment;
    public String Content;
    public String EditContent;
    public long Time;
    public String Color;
    public List<Comment> Replies = new ArrayList<>();
}
