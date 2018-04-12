package com.example.android.fyp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
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
    private User user;
    private startTimerForRoom displayTimeAsyncTask;
    private checkLocations checkLocationsAsyncTask;
    roomChangedObservable roomChangedBoolean;
    BoolObserver observer = new BoolObserver();
    private boolean running;


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
        checkLocationsAsyncTask = new checkLocations(thisContext, currentRoom, user);
        timer.schedule(checkLocationsAsyncTask, 0, 2000);
        running = true;
    }

    public void roomChanged() {
        if (running) {
            displayRoom.setText("CURRENT ROOM: " + currentRoom.Name());
            displayTimeAsyncTask = new startTimerForRoom();
            displayTimeAsyncTask.execute();
            roomChangedBoolean.setRoomChanged(false);
            //UPDATE USER ROOM
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
            int timet= scan.nextInt(); // Convert to seconds
            long delay = timet * 1000;
            try {
                do {
                    int minutes = timet / 60;
                    int seconds = timet % 60;
                    publishProgress(minutes, seconds);
                    Thread.sleep(1000);
                    timet = timet - 1;
                    delay = delay - 1000;
                } while (delay != 0 && running);
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
        }
    }


    public void stopLocationSearches() {
        try {
            timer.cancel();
            timer.purge();
            displayTimeAsyncTask.cancel(true);
        } catch (Exception e){
            Log.i(LOG_TAG, "CANCEL: " + e.toString());
        } finally {
            running = false;
        }

    }

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
            String macAddress = new scanWifiPoints(thisContext, currentRoom).execute().get();
            if (macAddress != null && macAddress != currentRoom.MAC()) {
                try {
                    new getCurrentRoom(macAddress, currentRoom, currentUser).execute();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
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
                roomChangedBoolean.setRoomChanged(true);
            } else {
                try {
                    JSONArray read = new JSONArray(result);
                    JSONObject object = read.getJSONObject(0);
                    Integer id = object.getInt("Location ID");
                    currentRoom.setID(id);
                    new getCurrentRoomNameandTime(id, currentRoom).execute();
                    new getCurrentRoomKeywords(id, currentRoom).execute();
                    new updateUserCurrentRoom(id, currentUser).execute();
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





