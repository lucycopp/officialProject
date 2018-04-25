package com.example.android.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class myFirebaseMessagingService extends FirebaseMessagingService {
    public myFirebaseMessagingService() {
    }

    private static final String TAG = "MyFirebaseMsgService";
    public static String message = "";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        try {
         Bundle bundle = new Bundle();
         bundle.putString("msgBody", remoteMessage.getNotification().getBody()); //add message to bundle

         Intent newIntent = new Intent();
         newIntent.setAction("ACTION_STRING_ACTIVITY"); //action triggered when sent
         newIntent.putExtra("msg", bundle); //add bundle to intent

         sendBroadcast(newIntent); //send broadcase for other activitys to recieve
        } catch (Exception e){
            Log.d(TAG, e.toString()); //log any exceptions
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }


}