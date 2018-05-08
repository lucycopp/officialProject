package com.example.android.fyp;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.android.fyp.JSONUtils.LOG_TAG;

public class AddAccessPointsActivity extends AppCompatActivity {

    Spinner displayRoomNamesSpinner;
    TextView displayWifiInfoTextView;
    Button addAccessPointButton, scanButton;
    ArrayList<String> roomNames = new ArrayList<String>();
    Map<String, Integer> rooms = new HashMap<>();
    String currentMac = "";
    Integer currentRSSI = 0;
    Context currentContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_acess_points);

        currentContext = this;

        displayRoomNamesSpinner = (Spinner) findViewById(R.id.roomNamesSpinner);
        displayWifiInfoTextView = (TextView) findViewById(R.id.displayWifiInfo);
        addAccessPointButton = (Button) findViewById(R.id.addScannedAccessPointButton);
        scanButton = (Button) findViewById(R.id.scanWifiButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
                    new scanWifiPoints(currentContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new scanWifiPoints(currentContext).execute();
                }
            }
        });

        addAccessPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
                    new addAccessPoint(displayRoomNamesSpinner.getSelectedItem().toString().trim(), currentContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new addAccessPoint(displayRoomNamesSpinner.getSelectedItem().toString().trim(), currentContext).execute();
                }
            }
        });


        try {
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
                new getRoomNamesFromDatabase(currentContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new scanWifiPoints(currentContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new getRoomNamesFromDatabase(currentContext).execute();
                new scanWifiPoints(currentContext).execute();
            }

        } catch (Exception e) {
            Log.i(LOG_TAG, "AddAccessPointsStartUp" + e.toString());
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private class getRoomNamesFromDatabase extends AsyncTask<String, String,String>{
        Context thisContext;

        private getRoomNamesFromDatabase(Context mContext){
            thisContext = mContext;
        }
        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/getRoomNames.php");
            try {
                result = JSONUtils.makeHTTPRequest(url);
                Log.e(LOG_TAG, "getFromDatabase:ConnectionSuccess");
            } catch (Exception e) {
                Log.e(LOG_TAG, "getFromDatabase:ConnectionFailed " + e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.replace("<html>","");
            if (result == null || result.trim() == "") { return;}
            else{
                try{
                    JSONArray read = new JSONArray(result);
                    for (int i = 0; i < read.length(); i++){
                        JSONObject object = read.getJSONObject(i);
                        roomNames.add(object.getString("Name"));
                        rooms.put(object.getString("Name"), object.getInt("Location ID"));
                    }
                }
                catch (JSONException e){
                    Log.e(LOG_TAG, e.toString());
                }
            }
            ArrayAdapter adapter = new ArrayAdapter(thisContext, android.R.layout.simple_spinner_item, roomNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            displayRoomNamesSpinner.setAdapter(adapter);

        }
    }

    private class scanWifiPoints extends AsyncTask<String, String, String>{
        String display = "";
        Context thisContext;

        private scanWifiPoints (Context mContext){
            thisContext = mContext;
        }

        final Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return (rhs.level < lhs.level ? -1 : (lhs.level==rhs.level ? 1 : 0));
            }
        };

        @Override
        protected String doInBackground(String... strings) {
            try {
                WifiManager wifi = (WifiManager) thisContext.getSystemService(Context.WIFI_SERVICE); //create instance
                wifi.startScan();
                List<ScanResult> results = wifi.getScanResults(); //put results into list
                Collections.sort(results, comparator); //sort the list in order of str

                String mac = null;
                int rssi = 0;

                for (int i = 0; i < results.size();i++){
                    if (!results.get(i).SSID.toString().equals("eduroam")) {
                        results.remove(results.get(i)); //remove any results which aren't eduroam
                    }
                }
                mac = results.get(0).BSSID;
                rssi = results.get(0).level; //get strongest mac and rssi
                currentMac = mac;
                currentRSSI = rssi; //set global variables
                display = "MAC: " + mac + " RSSI: " + rssi; //to output to user
            }
            catch (Exception e){
                Log.e(LOG_TAG, e.toString());
            }
            return display;
        }

        @Override
        protected void onPostExecute(String s) {
            displayWifiInfoTextView.setText(display);
        }
    }

    private class addAccessPoint extends AsyncTask<String, String, String>{
        String roomName;
        Context thisContext;
        private addAccessPoint (String mRoomName, Context mContext){
            roomName = mRoomName;
            thisContext = mContext;
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            Integer ID = rooms.get(roomName);
            if (currentMac != "" && currentRSSI != 0) {
                URL url = JSONUtils.makeURL("https://lcgetdata.azurewebsites.net/addAccessPoint.php?ID=" + ID + "&mac=" + currentMac + "&rssi=" + currentRSSI);
                try {
                    result = JSONUtils.makeHTTPRequest(url);
                    Log.e(LOG_TAG, "addToDatabase:ConnectionSuccess");
                } catch (Exception e) {
                    Log.e(LOG_TAG, "addToDatabase:ConnectionFailed " + e.toString());
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.replace("<html>","");
            if (result.contains("Added")){
                displayWifiInfoTextView.setText("");
                Toast.makeText(thisContext, "Access Point added", Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(thisContext, "MAC address already exists", Toast.LENGTH_LONG).show();
            }
        }
    }

}
