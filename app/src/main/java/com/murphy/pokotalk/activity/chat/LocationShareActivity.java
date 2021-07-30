package com.murphy.pokotalk.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.murphy.pokotalk.R;

public class LocationShareActivity extends AppCompatActivity {
    private int eventId;
    private Button backspaceButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_share_activity);

        // Get intent
        Intent intent = getIntent();

        // Get event id
        eventId = intent.getIntExtra("eventId", -1);

        // Event id should exist
        if (eventId < 0) {
            finish();
            return;
        }

        // Find views
        backspaceButton = findViewById(R.id.locationShareBackspaceButton);

        // Add event listeners
        backspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Create fragment
        LocationShareFragment fragment = new LocationShareFragment();
        fragment.setEventId(eventId);

        // Add location share fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.locationShareFragmentLayout, fragment)
                .commit();
    }
}
