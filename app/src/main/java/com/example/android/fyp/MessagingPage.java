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


        FirebaseMessaging.getInstance().subscribeToTopic("all");
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageInput.getText() != null) {
                    try {
                        new sendMessage(messageInput.getText().toString(), MyFirebaseInstanceIDService.getToken()).execute();
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
        String regToken;

        private sendMessage(String mMessage, String mToken){
            message = mMessage;
            regToken = mToken;
        }

        @Override
        protected String doInBackground(String... strings) {
            String responseCode = null;
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                JSONObject dataJson = new JSONObject();
                dataJson.put("body", message);
                dataJson.put("title", "Message");
                json.put("notification", dataJson);
                json.put("to", "/topics/all");
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .header("Authorization", "key=" + LEGACY_SERVER_KEY)
                        .url("https://fcm.googleapis.com/fcm/send")
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                String finalResponse = response.body().string();
                responseCode = String.valueOf(response.code());
            } catch (Exception e) {
                Log.d("SENDING MESSAGE", e.toString());
            }
            return responseCode;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                if (response.equals("200")) {
                    Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Message Failed to Send", Toast.LENGTH_SHORT).show();
                }
            }
            }
        }
    }



