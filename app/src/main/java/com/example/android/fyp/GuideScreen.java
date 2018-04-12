package com.example.android.fyp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.icu.util.VersionInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class GuideScreen extends AppCompatActivity {
Button startTourButton, keywordsButton;
TourFunctions currentTour;
TextView display, displayTime;
Boolean tourOccuring;
User currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_screen);
        keywordsButton = (Button) findViewById(R.id.viewKeywordsButton);
        startTourButton = (Button) findViewById(R.id.startTourButton);
        display = (TextView) findViewById(R.id.displayRoomName);
        displayTime = (TextView) findViewById(R.id.displayTimeRemaining);

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


}
