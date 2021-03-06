package com.example.android.fyp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class OperatorScreen extends AppCompatActivity {
Button editRoomButton;
Button keywordsPageButton;
Button viewCurrentLocationsButton;
Button messagingPageButton;
Button logOutButton;
User currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_screen);

        editRoomButton = (Button) findViewById(R.id.editRoomsButton);

        editRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OperatorScreen.this, EditRoomsMenu.class));
            }
        });

        keywordsPageButton = (Button) findViewById(R.id.keywordsPageButton);

        keywordsPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OperatorScreen.this, KeywordsMenu.class));
            }
        });

        viewCurrentLocationsButton = (Button) findViewById(R.id.currentLocationsButton);
        viewCurrentLocationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OperatorScreen.this, ViewCurrentLocations.class));
            }
        });
        messagingPageButton = (Button) findViewById(R.id.sendMessagePageButton);
        messagingPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OperatorScreen.this, MessagingPage.class));
            }
        });
        currentUser = new User(getIntent().getStringExtra("Email"), (int) getIntent().getIntExtra("ID", 0));

        logOutButton = (Button) findViewById(R.id.logOutButtonOperator);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(OperatorScreen.this, MainMenu.class));
            }
        });

    }
}
