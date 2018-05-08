package com.example.android.fyp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.android.fyp.JSONUtils.LOG_TAG;

/**
 * Created by Lucy on 26/03/2018.
 */

public class TourFunctions {
    RoomObject currentRoom = new RoomObject();
    private Context thisContext;
    private TextView displayRoom;
    private TextView displayTime;
    private Button chooseRoomButton;
    private User user;
    private boolean offlineMode;
    private startTimerForRoom displayTimeAsyncTask;
    private checkLocations checkLocationsAsyncTask;
    roomChangedObservable roomChangedBoolean;
    BoolObserver observer = new BoolObserver();
    private boolean running;
    private getCurrentRoomKeywords offlineGetCurrentKeywords;
    private getCurrentRoomNameandTime offlineGetRoomNameandTime;
    private updateUserCurrentRoom offlineUpdateUser;


    Timer timer;

    public TourFunctions(Context mContext, TextView mDisplayRoom, TextView mDisplayTime, User currentUser) {
        thisContext = mContext;
        displayRoom = mDisplayRoom;
        displayTime = mDisplayTime;
        user = currentUser;

        roomChangedBoolean = new roomChangedObservable(false);
        roomChangedBoolean.addObserver(observer);
    }

    public void startLocationSearches() {
        timer = new Timer();
        checkLocationsAsyncTask = new checkLocations(thisContext, currentRoom, user); //new instance of task
        timer.schedule(checkLocationsAsyncTask, 0, 2000);  //timer repeat method every 2 seconds
        running = true; //tour is now running
    }

    public void setLocationOfflineMode(int roomID){
        if (offlineMode == true) {
            currentRoom.setKeywords(new ArrayList<String>());
            currentRoom.setName(new String());
            currentRoom.setTime(0);
            currentRoom.setID(roomID);

            try {
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
                    new getCurrentRoomKeywords(roomID, currentRoom).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new getCurrentRoomNameandTime(roomID, currentRoom).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new updateUserCurrentRoom(roomID, user).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else {
                    new getCurrentRoomKeywords(roomID, currentRoom).execute();
                    new getCurrentRoomNameandTime(roomID, currentRoom).execute();
                    new updateUserCurrentRoom(roomID, user).execute();
                }

            } catch (Exception e) {
                Log.i(LOG_TAG, e.toString());
            }
        }

    }

    public void roomChanged() {
        if (running) {
            if ((currentRoom.MAC() == null || currentRoom.MAC().equals("")) && (currentRoom.Name() == null || currentRoom.Name().equals(""))) {
                displayRoom.setText("UNABLE TO FIND LOCATION");
                roomChangedBoolean.setRoomChanged(false);
                if(displayTimeAsyncTask != null){
                    //if displaying a timer is running
                    displayTimeAsyncTask.cancel(true);
                }
            } else {
                displayRoom.setText("CURRENT ROOM: " + currentRoom.Name());
                if(displayTimeAsyncTask != null){
                    //if displaying a timer is running
                    displayTimeAsyncTask.cancel(true);
                }
                displayTimeAsyncTask = new startTimerForRoom();

                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) { //if newer build version
                   displayTimeAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                   displayTimeAsyncTask.execute();
                }

                roomChangedBoolean.setRoomChanged(false);
            }
        }
    }

    private class startTimerForRoom extends  AsyncTask<String, Integer, String> {
        private volatile boolean running = true;

        @Override
        protected void onCancelled() {
            running = false;
        }

        @Override
        protected String doInBackground(String... strings) {
            Scanner scan = new Scanner(String.valueOf(currentRoom.Time()));
            int timet= scan.nextInt(); // get time
            long delay = timet * 1000;  //delay of 1 second
            try {
                do {
                    int minutes = timet / 60; //get the minutes
                    int seconds = timet % 60; //get the seconds
                    publishProgress(minutes, seconds);  //display to user
                    Thread.sleep(1000);  //sleep one second
                    timet = timet - 1; //time reduced
                    delay = delay - 1000; //delay reduced
                } while ((delay > 0) && (running == true));
            } catch (Exception e){ Log.i(LOG_TAG, e.toString()); }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            displayTime.setText("Time Remaining: " + values[0] + " minutes(s), " + values[1] + " seconds(s)");
        }

        @Override
        protected void onPostExecute(String s) {
            displayTime.setText("YOU ARE RUNNING BEHIND SCHEDULE");
            running = false;
        }
    }

    public void stopLocationSearches() {
        try {
            if(timer != null) {
                timer.cancel();
                timer.purge();
            } //cancel current tour timer
            if(displayTimeAsyncTask != null) {
                displayTimeAsyncTask.cancel(true);
            } //cancel current time timer
        } catch (Exception e){
            Log.i(LOG_TAG, "CANCEL: " + e.toString());
        } finally {
            running = false; //no tour running
        }

    }

    public boolean getOfflineMode(){ return offlineMode; }

    public RoomObject Room() {
        return currentRoom;
    }


//===========================================================================================================
    class checkLocations extends TimerTask {
    Context thisContext;
    RoomObject currentRoom;
    User currentUser;

    public checkLocations(Context mContext, RoomObject mRoom, User mUser) {
        thisContext = mContext;
        currentRoom = mRoom;
        currentUser = mUser;
    }


    public void run() {
        try {
            String macAddress = null;
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
                macAddress = new scanWifiPoints(thisContext, currentRoom).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get(); //set users current location to 0
            } else {
                macAddress = new scanWifiPoints(thisContext, currentRoom).execute().get();
            }

            if (macAddress != null && macAddress != currentRoom.MAC()) {
                try {
                    offlineMode = false; //now in online mode

                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
                        new getCurrentRoom(macAddress, currentRoom, currentUser).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //get room details
                    } else {
                        new getCurrentRoom(macAddress, currentRoom, currentUser).execute(); //get room details
                    }

                } catch (Exception e) {
                    Log.i(LOG_TAG, e.toString());
                }
            } else if (macAddress == null){
                currentRoom.setMac(new String());
                if (!offlineMode) {
                    roomChangedBoolean.setRoomChanged(true); //room has only changed if its not in offline mode already
                    offlineMode = true;
                }
            }
        } catch (Exception e) {
            Log.i(LOG_TAG, e.toString());
        }
    }
}

    public class scanWifiPoints extends AsyncTask<String, String, String> {
        Context thisContext;
        RoomObject currentRoom;

        public scanWifiPoints(Context mContext, RoomObject mCurrentRoom) {
            thisContext = mContext;
            currentRoom = mCurrentRoom;
        }

        final Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return (rhs.level < lhs.level ? -1 : (lhs.level==rhs.level ? 1 : 0));
            }
        };
        //https://stackoverflow.com/questions/17285337/how-can-i-sort-the-a-list-of-getscanresults-based-on-signal-strength-in-ascend
        @Override
        protected String doInBackground(String... strings) {
            String mac = null;
            try {
                WifiManager wifi = (WifiManager) thisContext.getSystemService(Context.WIFI_SERVICE);
                wifi.startScan();
                List<ScanResult> results = wifi.getScanResults();
                Collections.sort(results, comparator);


                if(results.size() > 0) {
                    for (int i = 0; i < results.size(); i++) {
                        if (!results.get(i).SSID.toString().equals("eduroam")) {
                            results.remove(results.get(i)); //remove any results which aren't eduroam
                        }
                    }
                    mac = results.get(0).BSSID;
                }

            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
            }

            return mac;
        }
    }

    public class getCurrentRoom extends AsyncTask<String, String, String> {
        private String MACAddress;
        private RoomObject currentRoom;
        private User currentUser;

        public getCurrentRoom(String mMACAddress, RoomObject mCurrentRoom, User mUser) {
            MACAddress = mMACAddress;
            currentRoom = mCurrentRoom;
            currentUser = mUser;

        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/searchAccessPoint.php?MAC=" + MACAddress);
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
            result = result.replace("<html>", "");
            if (result == null || result.trim() == "") {
                roomChangedBoolean.setRoomChanged(true); //room has changed to null
            } else {
                try {
                    JSONArray read = new JSONArray(result);
                    JSONObject object = read.getJSONObject(0);
                    Integer id = object.getInt("Location ID");
                    if(id != currentRoom.ID()) {
                        currentRoom.setID(id); //change current rooms ID
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            new getCurrentRoomNameandTime(id, currentRoom).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            new getCurrentRoomKeywords(id, currentRoom).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            new updateUserCurrentRoom(id, currentUser).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            new getCurrentRoomNameandTime(id, currentRoom).execute(); //get room name and time
                            new getCurrentRoomKeywords(id, currentRoom).execute(); //get keywords
                            new updateUserCurrentRoom(id, currentUser).execute(); //update user
                        }
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }

        }
    }

    public class updateUserCurrentRoom extends AsyncTask<String, String, String>{
        private User thisUser;
        private int ID;

        public updateUserCurrentRoom(int mID, User mUser){
            thisUser = mUser;
            ID = mID;
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/updateCurrentRoom.php?userID=" + thisUser.getUserID() + "&roomID=" + ID);
            try {
                result = JSONUtils.makeHTTPRequest(url);
                Log.e(LOG_TAG, "searchDatabase:ConnectionSuccess");
                thisUser.setLocationID(ID);
            } catch (Exception e) {
                Log.e(LOG_TAG, "searchDatabase:ConnectionFailed " + e.toString());
            }
            return result;
        }
    }

    public class getCurrentRoomKeywords extends AsyncTask<String, String, String> {
        private int ID;
        private RoomObject currentRoom;
        ArrayList<String> keywords = new ArrayList<String>();

        public getCurrentRoomKeywords(int mID, RoomObject mCurrentRoom) {
            ID = mID;
            currentRoom = mCurrentRoom;
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/searchKeywords.php?ID=" + ID);
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
            result = result.replace("<html>", "");
            if (result == null || result.trim() == "") {
                return;
            } else {
                try {
                    JSONArray read = new JSONArray(result);
                    for (int i = 0; i < read.length(); i++) {
                        JSONObject object = read.getJSONObject(i);
                        keywords.add(object.getString("Keyword"));
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
            currentRoom.setKeywords(keywords);
        }
    }

    public class getCurrentRoomNameandTime extends AsyncTask<String, String, String> {
        private int ID;
        private RoomObject currentRoom;

        public getCurrentRoomNameandTime(int mID, RoomObject mCurrentRoom) {
            ID = mID;
            currentRoom = mCurrentRoom;
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/searchRoomNames.php?ID=" + ID);
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
            result = result.replace("<html>", "");
            if (result == null || result.trim() == "") {
                return;
            } else {
                try {
                    JSONArray read = new JSONArray(result);
                    JSONObject object = read.getJSONObject(0);
                    currentRoom.setName(object.getString("Name"));
                    currentRoom.setTime(object.getInt("Time"));
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }

            roomChangedBoolean.setRoomChanged(true);
        }
    }

    public class roomChangedObservable extends Observable {
        private Boolean roomChanged;

        public roomChangedObservable(Boolean mRoomChanged) {
            this.roomChanged = mRoomChanged;
        }

        public Boolean getRoomChanged() {
            return roomChanged;
        }

        public void setRoomChanged(Boolean mRoomChanged) {
            this.roomChanged = mRoomChanged;
            setChanged();
            notifyObservers(roomChanged);
        }
    }

    public class BoolObserver implements Observer {
        private Boolean changed;

        public BoolObserver() {
            changed = null;
        }

        @Override
        public void update(Observable observable, Object o) {
            if (o instanceof Boolean) {
                changed = (Boolean) o;
                if (changed == true){
                roomChanged();}
            }
        }
    }

}





