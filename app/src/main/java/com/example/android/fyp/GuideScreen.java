package com.example.android.fyp;

import android.icu.util.VersionInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GuideScreen extends AppCompatActivity {
Button startTourButton;
TourFunctions currentTour;
TextView display;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_screen);

        startTourButton = (Button) findViewById(R.id.startTourButton);
        display = (TextView) findViewById(R.id.displayRoomName);
        currentTour = new TourFunctions(this, display);
        startTourButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               currentTour.startLocationSearches();
            }
        });
    }


}
