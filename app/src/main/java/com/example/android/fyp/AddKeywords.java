package com.example.android.fyp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.fyp.JSONUtils.LOG_TAG;

public class AddKeywords extends AppCompatActivity {

    Spinner roomNamesSpinner;
    Button addKeywordButton;
    EditText keywordsEditText;
    ArrayList<String> roomNames = new ArrayList<String>();
    Map<String, Integer> rooms = new HashMap<String, Integer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_keywords);

        roomNamesSpinner = (Spinner) findViewById(R.id.roomNamesSpinnerKeywords);
        addKeywordButton = (Button) findViewById(R.id.addSingleKeywordButton);
        keywordsEditText = (EditText) findViewById(R.id.editTextKeyword);

        new getRoomNamesFromDatabase().execute();

        addKeywordButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if ((keywordsEditText.getText().toString() != "") && (roomNamesSpinner.getSelectedItem() != "")) {
                    new addKeywordToDatabase().execute();
                }
            }
        });
    }

    private class getRoomNamesFromDatabase extends AsyncTask<String, String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/getRoomNames.php"); //make URL
            try {
                result = JSONUtils.makeHTTPRequest(url); //get result from HTTP Request
                Log.e(LOG_TAG, "getFromDatabase:ConnectionSuccess"); //successful
            } catch (Exception e) {
                Log.e(LOG_TAG, "getFromDatabase:ConnectionFailed " + e.toString()); //log any exceptions
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.replace("<html>","");
            if (result == null || result.trim() == "") { return;} //if no results
            else{
                try{
                    JSONArray read = new JSONArray(result);  //get JSON Array
                    for (int i = 0; i < read.length(); i++){ //iterate through array
                        JSONObject object = read.getJSONObject(i); //get the JSON object
                        String name = object.getString("Name"); //get name from object
                        Integer id = object.getInt("Location ID"); //get ID from object
                        rooms.put(name, id);
                        roomNames.add(name); //add to lists
                    }
                }
                catch (JSONException e){
                    Log.e(LOG_TAG, e.toString()); //log any exceptions
                }
            }
            ArrayAdapter<String > adapter = new ArrayAdapter<String> (getApplicationContext(), R.layout.spinner_item, roomNames );
            roomNamesSpinner.setAdapter(adapter);

        }
    }


    private class addKeywordToDatabase extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            Integer ID = (Integer) rooms.get(roomNamesSpinner.getSelectedItem().toString()); //get the room ID

            URL url = JSONUtils.makeURL("https://lcgetdata.azurewebsites.net/addKeywords.php?ID=" + ID + "&Keyword=" + keywordsEditText.getText()); //make url with parameters
            try {
                result = JSONUtils.makeHTTPRequest(url); //get result from http request
                Log.e(LOG_TAG, "addToDatabase:ConnectionSuccess"); //successful
            } catch (Exception e) {
                Log.e(LOG_TAG, "addToDatabase:ConnectionFailed " + e.toString()); //log any exceptions
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.replace("<html>","");
            if (result.contains("Added")){ //if keyword has been added
                keywordsEditText.setText("");
                Toast.makeText(getApplicationContext(), "Keyword added", Toast.LENGTH_LONG); //tell user keyword has been added
            } else{
                Toast.makeText(getApplicationContext(), "Unable to add keyword", Toast.LENGTH_LONG); //tell user keyword has not been added
            }

        }
    }


}
