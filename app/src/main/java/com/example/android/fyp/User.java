package com.example.android.fyp;

/**
 * Created by Lucy on 12/04/2018.
 */

public class User {
    private String name;
    private int UserID, locationID;

    public User(String mName, int mUserID){
        name = mName;
        UserID = mUserID;
    }


    public void setLocationID(int mID){
        locationID = mID;
    }

    public int getLocationID(){ return locationID; }
    public int getUserID() {return UserID; }
    public String getName() {return name; }


}
