package com.example.android.fyp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.fyp.JSONUtils.LOG_TAG;

public class ViewCurrentLocations extends AppCompatActivity {
    Map<Integer, String> rooms = new HashMap<Integer, String>();
    ArrayList<User> allUsers = new ArrayList<>();
    ListView currentLocationsList;
    Button refreshButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_current_locations);

        currentLocationsList = (ListView) findViewById(R.id.currentLocationsListView);
        refreshButton = (Button) findViewById(R.id.refreshButton);
        populateList();

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLocationsList.setAdapter(null);
                rooms.clear();
                allUsers.clear();
                populateList();
            }
            });


    }

    public void populateList(){
        new getRooms().execute();
    }


    private class getRooms extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/getRoomNames.php");
            try {
                result = JSONUtils.makeHTTPRequest(url); //make request
                Log.e(LOG_TAG, "getFromDatabase:ConnectionSuccess");
            } catch (Exception e) {
                Log.e(LOG_TAG, "getFromDatabase:ConnectionFailed " + e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                result = result.replace("<html>", "");
                if (result == null || result.trim() == "") {
                    return;
                } else {
                    try {
                        JSONArray read = new JSONArray(result);
                        for (int i = 0; i < read.length(); i++) {
                            JSONObject object = read.getJSONObject(i);
                            String name = object.getString("Name"); //extract name
                            Integer id = object.getInt("Location ID"); //extract id
                            rooms.put(id, name); //add to global hash map
                        }
                        new getUsers().execute(); //get users

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.toString());
                    }
                }
            }
        }
    }

    private class getUsers extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/getUsers.php");
            try {
                result = JSONUtils.makeHTTPRequest(url); //make request
                Log.e(LOG_TAG, "getFromDatabase:ConnectionSuccess");
            } catch (Exception e) {
                Log.e(LOG_TAG, "getFromDatabase:ConnectionFailed " + e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.replace("<html>", "");
            if (result == null || result.trim() == "") {
                return;
            } else {
                try {
                    JSONArray read = new JSONArray(result);
                    for (int i = 0; i < read.length(); i++) {
                        JSONObject object = read.getJSONObject(i);
                        boolean guide = object.getBoolean("Guide");
                        if(guide) {
                            String name = object.getString("Email");
                            name = name.split("@")[0]; //username will be first part of email
                            Integer userID = object.getInt("User ID");
                            Integer roomID = object.getInt("Location ID");
                            User newUser = new User(name, userID); //create new user
                            newUser.setLocationID(roomID); //add room id
                            allUsers.add(newUser); //add all users to global list
                        }
                        displayToUser(); //display to user
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
        }
    }

    public void displayToUser(){
        ArrayList<String> output = new ArrayList<>();
        for(int i = 0; i < allUsers.size(); i++){
            if(allUsers.get(i).getLocationID() == 0){ //if no location
                output.add("ID: " + allUsers.get(i).getUserID() + " Name: " + allUsers.get(i).getName() + " OFFLINE");
            } else {
                output.add("ID: " + allUsers.get(i).getUserID() + " Name: " + allUsers.get(i).getName() + " Location: " + rooms.get(allUsers.get(i).getLocationID()));
            }
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, output);
        currentLocationsList.setAdapter(adapter);
    }


}
