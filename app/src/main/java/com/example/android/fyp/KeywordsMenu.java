package com.example.android.fyp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class KeywordsMenu extends AppCompatActivity {
Button addKeywordsButton;
Button editKeywordsButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keywords_menu);

        addKeywordsButton = (Button) findViewById(R.id.addKeywordsButton);
        editKeywordsButton = (Button) findViewById(R.id.editKeywordsButton);

        addKeywordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(KeywordsMenu.this, AddKeywords.class));
            }
        });

        editKeywordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(KeywordsMenu.this, EditKeywords.class));
            }
        });
    }
}
