package com.example.jacob.blankbookandroidclient.api.models;

import java.io.Serializable;

public class Post implements Serializable {
    public long Rank;
    public long ID;
    public int Score;
    public String Title;
    public String EditTitle;
    public String Content;
    public String EditContent;
    public String ContentType = "text";
    public String GroupName;
    public long Time;
    public String Color = "000000";
}
