package com.example.android.fyp;

/**
 * Created by Lucy on 26/03/2018.
 */

public class RoomObject {
    private String name;
    private String[] keywords;
    private int time;

    public RoomObject(){}

    public String Name() { return name; }
    public String[] Keywords() { return keywords; }
    public int Time() { return time; }

    public void setName(String mName) { name = mName; }
    public void setKeywords(String[] mKeywords) { keywords = mKeywords; }
    public void setTime (int mTime) { time = mTime; }

}
