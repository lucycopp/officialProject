package com.example.android.fyp;

import android.content.Intent;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EditRoomsMenu extends AppCompatActivity {
Button addRoom;
Button addAccessPoints;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        addRoom = (Button) findViewById(R.id.addRoomButton);
        addAccessPoints = (Button) findViewById(R.id.addAccessPoints);

        addRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditRoomsMenu.this, AddRoomActivity.class));
            }
        });

        addAccessPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditRoomsMenu.this, AddAccessPointsActivity.class));
            }
        });

    }
}
