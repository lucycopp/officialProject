package com.example.android.fyp;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.fyp.JSONUtils.LOG_TAG;

public class EditKeywords extends AppCompatActivity {
    Button deleteButton;
    ListView keywordsList;
    Spinner roomsSpinner;
    Map<String, Integer> keywords = new HashMap<String, Integer>();
    Map<String, Integer> rooms = new HashMap<String, Integer>();
    String selectedKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_keywords);

        deleteButton = (Button) findViewById(R.id.deleteKeywordButton);
        keywordsList = (ListView) findViewById(R.id.keywordsListView);
        roomsSpinner = (Spinner) findViewById(R.id.roomNamesSpinnerKeywordsEdit);



        new getRoomNamesFromDatabase().execute();
        roomsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Integer ID = rooms.get(roomsSpinner.getSelectedItem());
                new getKeywords(ID).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //nothing
            }
        });
        keywordsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedKeyword = keywordsList.getSelectedItem().toString().trim();
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setMessage("Would you like to delete this keyword?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    new deleteKeyword(keywords.get(selectedKeyword)).execute();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

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
            ArrayList<String> roomNames = new ArrayList<String>();
            result = result.replace("<html>", "");
            if (result == null || result.trim() == "") {
                return;
            } else {
                try {
                    JSONArray read = new JSONArray(result);
                    for (int i = 0; i < read.length(); i++) {
                        JSONObject object = read.getJSONObject(i);
                        String name = object.getString("Name");
                        Integer id = object.getInt("Location ID");
                        roomNames.add(name);
                        rooms.put(name, id);
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, roomNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roomsSpinner.setAdapter(adapter);
        }
    }

    private class getKeywords extends AsyncTask<String, String, String>{
        Integer roomID;

        public getKeywords(Integer mRoomID){
            roomID = mRoomID;
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/searchKeywords.php?ID=" + roomID);
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
            ArrayList<String> keywordList = new ArrayList<String>();
            result = result.replace("<html>", "");
            if (result == null || result.trim() == "") {
                return;
            } else {
                try {
                    JSONArray read = new JSONArray(result);
                    for (int i = 0; i < read.length(); i++) {
                        JSONObject object = read.getJSONObject(i);
                        String name = object.getString("Keyword");
                        Integer id = object.getInt("ID");
                        keywordList.add(name);
                        keywords.put(name, id);
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, keywordList);
            keywordsList.setAdapter(adapter);
        }
    }

    private class deleteKeyword extends AsyncTask<String, String, String>{
        Integer ID;

        public deleteKeyword(int mID){
            ID = mID;
        }

            @Override
            protected String doInBackground(String... strings) {
                String result = null;
                URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/deleteKeyword.php?ID=" + ID);
                try {
                    result = JSONUtils.makeHTTPRequest(url);
                    Log.e(LOG_TAG, "deleteFromDatabase:ConnectionSuccess");
                } catch (Exception e) {
                    Log.e(LOG_TAG, "deleteFromDatabase:ConnectionFailed " + e.toString());
                }
                return result;
            }
        }

}
