package com.example.android.fyp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.sql.ResultSet;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessagingPage extends AppCompatActivity {
    EditText messageInput;
    Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_page);

        messageInput = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendMessageButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageInput.getText() != null) {
                    try {
                        new sendMessage(messageInput.getText().toString()).execute();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Unable to send message", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Please enter message", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private class sendMessage extends AsyncTask<String, String, String> {
        String LEGACY_SERVER_KEY = "AIzaSyCXFhviZuSwgDpLMj5z3P6gz1L9AmruuUY";
        String message;

        private sendMessage(String mMessage){
            message = mMessage;
        }

        @Override
        protected String doInBackground(String... strings) {
            String responseCode = null;
            try {
                OkHttpClient client = new OkHttpClient(); //create new client
                JSONObject json = new JSONObject();
                JSONObject dataJson = new JSONObject(); //make new JSON objects
                dataJson.put("body", message); //put inputted message as body
                dataJson.put("title", "Message"); //title is message
                json.put("notification", dataJson); //nest data as notification value
                json.put("to", "/topics/all"); //send to those subscribed to all topic
                RequestBody body = RequestBody.create(JSON, json.toString()); //create request
                Request request = new Request.Builder()
                        .header("Authorization", "key=" + LEGACY_SERVER_KEY) //add legacy key to the header
                        .url("https://fcm.googleapis.com/fcm/send") //URL which handles requests
                        .post(body) //put body into request
                        .build();
                Response response = client.newCall(request).execute(); //send request and get reponse
                String finalResponse = response.body().string(); //get body of response
                responseCode = String.valueOf(response.code()); //get response key
            } catch (Exception e) {
                Log.d("SENDING MESSAGE", e.toString());
            }
            return responseCode;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                if (response.equals("200")) { //if response is OK
                    Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Message Failed to Send", Toast.LENGTH_SHORT).show();
                }
            }
            }
        }
    }



