package com.example.android.fyp;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Lucy on 26/03/2018.
 */

public class RoomObject {
    private String name;
    private int ID;
    private String mac;
    private ArrayList<String> keywords;
    private int time;

    public RoomObject(){}

    public String Name() { return name; }
    public ArrayList<String> Keywords() { return keywords; }
    public int Time() { return time; }
    public String MAC() { return mac; }
    public int ID() {return ID;}

    public void setName(String mName) { name = mName; }
    public void setKeywords(ArrayList<String> mKeywords) { keywords = mKeywords; }
    public void setTime (int mTime) { time = mTime; }
    public void setMac (String mMac) { mac = mMac; }
    public void setID (int mID){ID = mID;}

}
