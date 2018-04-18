package com.example.android.fyp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.icu.util.VersionInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.fyp.JSONUtils.LOG_TAG;

public class GuideScreen extends AppCompatActivity {
Button startTourButton, keywordsButton, chooseRoomButton;
TourFunctions currentTour;
TextView display, displayTime;
Boolean tourOccuring;
User currentUser;
Map<Integer, String> roomMap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_screen);
        keywordsButton = (Button) findViewById(R.id.viewKeywordsButton);
        startTourButton = (Button) findViewById(R.id.startTourButton);
        display = (TextView) findViewById(R.id.displayRoomName);
        displayTime = (TextView) findViewById(R.id.displayTimeRemaining);
        chooseRoomButton = (Button) findViewById(R.id.chooseRoomButton);


        currentUser = new User("lucy", 13);

        currentTour = new TourFunctions(this, display, displayTime, currentUser);
        tourOccuring = false;

        startTourButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (tourOccuring){
                    currentTour.stopLocationSearches();
                    startTourButton.setText("START TOUR");
                    display.setText("");
                    displayTime.setText("");
                    tourOccuring = false;
                }else {
                    currentTour.startLocationSearches();
                    startTourButton.setText("STOP TOUR");
                    display.setText("Scanning for location...");
                    tourOccuring = true;
                }
            }
        });

        chooseRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentTour.getOfflineMode()){
                    new getRoomNamesFromDatabase().execute();
                }
            }
            });

        keywordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("KeywordList", currentTour.currentRoom.Keywords());
                KeywordsFragment frag = new KeywordsFragment();
                frag.setArguments(bundle);
                loadFragment(frag);
            }
        });
    }

    private void loadFragment(Fragment fragment){
        // create a FragmentManager
        FragmentManager fm = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.frameLayoutKeywords, fragment);
        fragmentTransaction.commit(); // save the changes
    }

    private class getRoomNamesFromDatabase extends AsyncTask<String, String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            if (roomMap.size() == 0) {
                URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/getRoomNames.php");
                try {
                    result = JSONUtils.makeHTTPRequest(url);
                    Log.e(LOG_TAG, "getFromDatabase:ConnectionSuccess");
                } catch (Exception e) {
                    Log.e(LOG_TAG, "getFromDatabase:ConnectionFailed " + e.toString());
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (roomMap.size() == 0) {
                ArrayList<String> roomNames = new ArrayList<>();
                result = result.replace("<html>", "");
                if (result == null || result.trim() == "") {
                    return;
                } else {
                    try {
                        JSONArray read = new JSONArray(result);
                        for (int i = 0; i < read.length(); i++) {
                            JSONObject object = read.getJSONObject(i);
                            roomMap.put(object.getInt("Location ID"), object.getString("Name"));
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.toString());
                    }
                }
            }

            Bundle bundle = new Bundle();
            RoomsFragment frag = new RoomsFragment();
            frag.passData(currentTour, roomMap);
            frag.setArguments(bundle);
            loadFragment(frag);
        }
    }

}
