package com.example.android.fyp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.util.VersionInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.data.DataBufferObserver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;

import static com.example.android.fyp.JSONUtils.LOG_TAG;

public class GuideScreen extends AppCompatActivity {
    Button startTourButton, keywordsButton, chooseRoomButton, logOutButton;
    TourFunctions currentTour;
    TextView display, displayTime, displayMessage;
    Boolean tourOccuring;
    User currentUser;
    Map<Integer, String> roomMap = new HashMap<>();

    private BroadcastReceiver activityReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            displayMessage = (TextView) findViewById(R.id.displayMessage); //get the textview
            Bundle bundle = intent.getBundleExtra("msg");  //get the message
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");  //create format
            Calendar cal = Calendar.getInstance();
            dateFormat.setTimeZone(cal.getTimeZone()); //get timezone of phone
            Date date = new Date(); //new date
            displayMessage.setText("[" + dateFormat.format(date) + "] " + bundle.getString("msgBody")); //output message
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_screen);
        displayMessage = (TextView) findViewById(R.id.displayMessage);
        keywordsButton = (Button) findViewById(R.id.viewKeywordsButton);
        startTourButton = (Button) findViewById(R.id.startTourButton);
        display = (TextView) findViewById(R.id.displayRoomName);
        displayTime = (TextView) findViewById(R.id.displayTimeRemaining);
        chooseRoomButton = (Button) findViewById(R.id.chooseRoomButton);
        logOutButton = (Button) findViewById(R.id.logOutButtonGuide);

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        currentUser = new User(getIntent().getStringExtra("Email"), (int) getIntent().getIntExtra("ID", 0));

        if (activityReciever != null){
            IntentFilter intentFilter = new IntentFilter("ACTION_STRING_ACTIVITY");
            registerReceiver(activityReciever, intentFilter);
        }

        currentTour = new TourFunctions(this, display, displayTime, currentUser);
        tourOccuring = false;

        startTourButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (tourOccuring) {
                    currentTour.stopLocationSearches();
                    startTourButton.setText("START TOUR");
                    display.setText("");
                    displayTime.setText("");
                    tourOccuring = false;
                } else {
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
                if (currentTour.getOfflineMode()) {
                    if(roomMap.size() == 0) {
                        new getRoomNamesFromDatabase().execute();
                    } else {
                        openChooseRoomFragment();
                    }
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

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FirebaseAuth.getInstance().signOut(); //sign out of firebase service
                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
                        new updateUserCurrentRoom(0, currentUser).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //set users current location to 0
                    } else {
                        new updateUserCurrentRoom(0, currentUser).execute(); //set users current location to 0
                    }
                } catch(Exception e){
                    Log.i(LOG_TAG, e.toString());
                }
            }
        });
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

        @Override
        protected void onPostExecute(String s) {
            startActivity(new Intent(GuideScreen.this, MainMenu.class));
        }
    }
    private void loadFragment(Fragment fragment) {
        // create a FragmentManager
        FragmentManager fm = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.frameLayoutKeywords, fragment);
        fragmentTransaction.commit(); // save the changes
    }

    private class getRoomNamesFromDatabase extends AsyncTask<String, String, String> {

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
                        openChooseRoomFragment();
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.toString());
                    }

            }


        }
    }


    private void openChooseRoomFragment(){
        Bundle bundle = new Bundle();
        RoomsFragment frag = new RoomsFragment();
        frag.passData(currentTour, roomMap);
        frag.setArguments(bundle);
        loadFragment(frag);
    }
    }


