package com.example.android.fyp;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;

import static com.example.android.fyp.JSONUtils.LOG_TAG;

/**
 * Created by Lucy on 26/03/2018.
 */

public class TourFunctions {
    RoomObject currentRoom = new RoomObject();
    private Context thisContext;

    public TourFunctions(Context mContext){
         thisContext = mContext;
    }

    public RoomObject Room() { return currentRoom; }

}


class scanWifiPoints extends AsyncTask<String, String, String> {
    Context thisContext;

    public scanWifiPoints(Context mContext) {
        thisContext = mContext;
    }

    @Override
    protected String doInBackground(String... strings) {
        String mac = null;
        try {
            WifiManager wifi = (WifiManager) thisContext.getSystemService(Context.WIFI_SERVICE);
            wifi.startScan();
            List<ScanResult> results = wifi.getScanResults();


            int rssi = 100;

            for (ScanResult result : results) {
                if (result.SSID.toString().equals("eduroam")) {
                    if (rssi > Math.abs(result.level)) {
                        rssi = result.level;
                        mac = result.BSSID;
                    }
                }
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
        return mac;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null){

        }
    }
}

class getCurrentRoomID extends AsyncTask<String, String,String>{
private String MACAddress;
private RoomObject currentRoom;

public getCurrentRoomID (String mMACAddress, RoomObject mCurrentRoom){
    MACAddress = mMACAddress;
    currentRoom = mCurrentRoom;
}

    @Override
    protected String doInBackground(String... strings) {
        String result = null;
        URL url = JSONUtils.makeURL("http://http://lcgetdata.azurewebsites.net/searchAccessPoint.php?MAC=" + MACAddress);
        try {
            result = JSONUtils.makeHTTPRequest(url);
            Log.e(LOG_TAG, "searchDatabase:ConnectionSuccess");
        } catch (Exception e) {
            Log.e(LOG_TAG, "searchDatabase:ConnectionFailed " + e.toString());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
    int locationID = 0;
        result = result.replace("<html>","");
        if (result == null || result.trim() == "") { return;}
        else{
            try{
                JSONArray read = new JSONArray(result);
                JSONObject object = read.getJSONObject(0);
                locationID = object.getInt("Location ID");
                new getCurrentRoomNameandTime(locationID, currentRoom).execute();

            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.toString());
            }
        }
    }
}

class getCurrentRoomKeywords extends AsyncTask<String, String,String>{
    private int ID;
    private RoomObject currentRoom;

    public getCurrentRoomKeywords(int mID, RoomObject mCurrentRoom){
        ID = mID;
        currentRoom = mCurrentRoom;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = null;
        URL url = JSONUtils.makeURL("http://http://lcgetdata.azurewebsites.net/searchRoomNames.php?ID=" + ID);
        try {
            result = JSONUtils.makeHTTPRequest(url);
            Log.e(LOG_TAG, "searchDatabase:ConnectionSuccess");
        } catch (Exception e) {
            Log.e(LOG_TAG, "searchDatabase:ConnectionFailed " + e.toString());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        int locationID = 0;
        result = result.replace("<html>","");
        if (result == null || result.trim() == "") { return;}
        else{
            try{
                JSONArray read = new JSONArray(result);
                JSONObject object = read.getJSONObject(0);
                currentRoom.setName(object.getString("Name"));
                currentRoom.setTime(object.getInt("Time"));
            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.toString());
            }
        }
    }
}


class getCurrentRoomNameandTime extends AsyncTask<String, String,String>{
    private int ID;
    private RoomObject currentRoom;

    public getCurrentRoomNameandTime (int mID, RoomObject mCurrentRoom){
        ID = mID;
        currentRoom = mCurrentRoom;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = null;
        URL url = JSONUtils.makeURL("http://http://lcgetdata.azurewebsites.net/searchRoomNames.php?ID=" + ID);
        try {
            result = JSONUtils.makeHTTPRequest(url);
            Log.e(LOG_TAG, "searchDatabase:ConnectionSuccess");
        } catch (Exception e) {
            Log.e(LOG_TAG, "searchDatabase:ConnectionFailed " + e.toString());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        int locationID = 0;
        result = result.replace("<html>","");
        if (result == null || result.trim() == "") { return;}
        else{
            try{
                JSONArray read = new JSONArray(result);
                JSONObject object = read.getJSONObject(0);
                currentRoom.setName(object.getString("Name"));
                currentRoom.setTime(object.getInt("Time"));
            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.toString());
            }
        }
    }
}







