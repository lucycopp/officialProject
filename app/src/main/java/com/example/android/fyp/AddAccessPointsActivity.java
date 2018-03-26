package com.example.android.fyp;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.fyp.JSONUtils.LOG_TAG;

public class AddAccessPointsActivity extends AppCompatActivity {

    Spinner displayRoomNamesSpinner;
    TextView displayWifiInfoTextView;
    Button addAccessPointButton, scanButton;
    ArrayList<String> roomNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_acess_points);

        displayRoomNamesSpinner = (Spinner) findViewById(R.id.roomNamesSpinner);
        displayWifiInfoTextView = (TextView) findViewById(R.id.displayWifiInfo);
        addAccessPointButton = (Button) findViewById(R.id.addScannedAccessPointButton);
        scanButton = (Button) findViewById(R.id.scanWifiButton);

        new getRoomNamesFromDatabase().execute();
        new scanWifiPoints().execute();
    }

    private class getRoomNamesFromDatabase extends AsyncTask<String, String,String>{

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/getRoomNames.php");
            try {
                result = JSONUtils.makeHTTPRequest(url);
                Log.e(LOG_TAG, "addToDatabase:ConnectionSuccess");
            } catch (Exception e) {
                Log.e(LOG_TAG, "addToDatabase:ConnectionFailed " + e.toString());
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
                    }
                }
                catch (JSONException e){
                    Log.e(LOG_TAG, e.toString());
                }
            }
            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, roomNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            displayRoomNamesSpinner.setAdapter(adapter);

        }


    }

    private class scanWifiPoints extends AsyncTask<String, String, String>{
        String display = "";
        @Override
        protected String doInBackground(String... strings) {
            try {
                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifi.startScan();
                List<ScanResult> results = wifi.getScanResults();

                String mac = null;
                int rssi = 100;

                for (ScanResult result : results) {
                    if (result.SSID.toString().equals("eduroam")) {
                        if (rssi > Math.abs(result.level)) {
                            rssi = result.level;
                            mac = result.BSSID;
                        }
                    }
                }

                display = "MAC: " + mac + " RSSI: " + rssi;
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

}
