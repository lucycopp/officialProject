package com.example.android.fyp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class LogIn extends AppCompatActivity {
    Button logInButtonOperator;
    Button logInButtonGuide;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        logInButtonOperator = (Button) findViewById(R.id.logInButtonOperator);
        logInButtonOperator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogIn.this, OperatorScreen.class));
            }
        });

        logInButtonGuide = (Button) findViewById(R.id.logInButtonGuide);
        logInButtonGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogIn.this, GuideScreen.class));
            }
        });

    }
}
