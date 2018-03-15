package com.example.android.fyp;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.URL;

import static com.example.android.fyp.JSONUtils.LOG_TAG;

public class AddRoomActivity extends AppCompatActivity {

    Button addRoomButton;
    EditText roomNameEditText, roomTimeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room2);

        addRoomButton = (Button) findViewById(R.id.addRoomToDatabaseButton);
        roomNameEditText = (EditText) findViewById(R.id.roomNameInsert);
        roomTimeEditText  =(EditText) findViewById(R.id.roomTimeInsert);


        addRoomButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new AddRoomToDatabase().execute(roomNameEditText.getText().toString() , roomTimeEditText.getText().toString());
            }
        });
    }

    private class AddRoomToDatabase extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String name = strings[0];
            String time = strings[1];

            String result = null;

            URL url = JSONUtils.makeURL("https://lcgetdata.azurewebsites.net/addRoom.php?Name=" + name + "&Time=" + time);
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
            result = result.replace("<html>", "");
            if (result.contains("added")) {
                Log.i(LOG_TAG, "Room Added to Database");
            }
            roomTimeEditText.setText("");
            roomNameEditText.setText("");

        }
    }

}


