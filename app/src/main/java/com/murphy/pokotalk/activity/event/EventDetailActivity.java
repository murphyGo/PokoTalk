package com.murphy.pokotalk.activity.event;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.event.EventParticipantListAdapter;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.event.EventLocation;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.user.UserList;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import java.util.Calendar;

public class EventDetailActivity extends AppCompatActivity
        implements PopupMenu.OnMenuItemClickListener{
    private PokoServer server;
    private PokoEvent event;
    private EventList.EventGroupRelation relation;
    private Button backButtonView;
    private ImageView topMenuView;
    private TextView eventNameView;
    private TextView eventDateView;
    private TextView eventLocationView;
    private TextView eventDescriptionView;
    private LinearLayout eventStartedLayout;
    private Button eventStartButton;
    private ListView participantListView;

    private EventParticipantListAdapter participantListAdapter;

    public static final String dateFormat = "%04d.%02d.%02d %s %02d:%02d";
    public static final String locationFormat = "%s에서";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail_layout);

        // Get event id
        Intent intent = getIntent();
        Integer eventId = intent.getIntExtra("eventId", -1);

        // Event id should exist
        if (eventId < 0) {
            finish();
            return;
        }

        // Find event
        event = DataCollection.getInstance().getEventList().getItemByKey(eventId);

        // Event should exist
        if (event == null) {
            finish();
            return;
        }

        // Find views
        backButtonView = findViewById(R.id.backspaceButton);
        topMenuView = findViewById(R.id.eventDetailMenuButton);
        eventNameView = findViewById(R.id.eventDetailEventName);
        eventDateView = findViewById(R.id.eventDetailDate);
        eventLocationView = findViewById(R.id.eventDetailLocation);
        eventDescriptionView = findViewById(R.id.eventDetailEventDescription);
        participantListView = findViewById(R.id.eventDetailParticipantList);
        eventStartedLayout = findViewById(R.id.eventDetailStartedLayout);
        eventStartButton = findViewById(R.id.eventDetailStartButton);

        // Get event group relation
        EventList eventList = DataCollection.getInstance().getEventList();
        relation = eventList.getEventGroupRelationByEventId(eventId);

        if (relation != null) {
            // Show start layout
            eventStartedLayout.setVisibility(View.VISIBLE);
        } else {
            // Event is not started, hide start layout
            eventStartedLayout.setVisibility(View.GONE);
        }

        // Set event listeners
        eventStartButton.setOnClickListener(eventStartButtonClickListener);

        backButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish activity
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        eventLocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start event location selection activity
                Intent intent = new Intent(getApplicationContext(),
                        EventLocationSelectionActivity.class);

                // Get location
                EventLocation location = event.getLocation();

                if (location != null) {
                    // Put location data if selected
                    intent.putExtra("selected", true);
                    intent.putExtra("unSelectable", true);
                    intent.putExtra("title", location.getTitle());
                    intent.putExtra("category", location.getCategory());
                    intent.putExtra("address", location.getAddress());
                    intent.putExtra("latLng", location.getLatLng());

                    // Start activity
                    startActivity(intent);
                }
            }
        });

        final EventDetailActivity activity = this;
        topMenuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);
                popup.setOnMenuItemClickListener(activity);
                popup.inflate(R.menu.event_detail_menu);
                popup.show();
            }
        });

        // Set data
        eventNameView.setText(event.getEventName());
        eventDescriptionView.setText(event.getDescription());
        eventDateView.setText(getDateString());
        eventLocationView.setText(getLocationString());

        // Create adapter
        participantListAdapter = new EventParticipantListAdapter(this);
        UserList participantList = (UserList) participantListAdapter.getPokoList();
        participantList.copyFromPokoList(event.getParticipants());
        participantListView.setAdapter(participantListAdapter);

        // Send event exit request to server
        server = PokoServer.getInstance();
        server.attachActivityCallback(Constants.eventExitName, eventExitCallback);
        server.attachActivityCallback(Constants.eventStartedName, eventStartedCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        server.detachActivityCallback(Constants.eventExitName, eventExitCallback);
        server.detachActivityCallback(Constants.eventStartedName, eventStartedCallback);
    }

    // Event exit callback
    private ActivityCallback eventExitCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            Integer eventId = (Integer) getData("eventId");

            // Finish activity if exited event is this event
            if (eventId != null && event.getEventId() == eventId) {
                setResult(RESULT_OK);
                finish();
            }
        }

        @Override
        public void onError(Status status, Object... args) {
            Toast.makeText(getApplicationContext(),
                    "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    };

    // Event start callback
    private ActivityCallback eventStartedCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            PokoEvent startedEvent = (PokoEvent) getData("event");

            // Check if started event is this event
            if (startedEvent != null && startedEvent == event) {
                // Check if the event has started
                if (event.getState() == PokoEvent.EVENT_STARTED
                        && relation != null) {
                    // Show event start layout
                    eventStartedLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onError(Status status, Object... args) {
            Toast.makeText(getApplicationContext(),
                    "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener eventStartButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (event != null && relation != null) {
                // Set result and put group id
                Intent intent = new Intent();
                intent.putExtra("groupId", relation.getGroupId());
                setResult(RESULT_OK, intent);

                // Finish this activity
                finish();
            }
        }
    };

    // Get string representation of event date
    private String getDateString() {
        Calendar date = event.getEventDate();
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH + 1);
        int day = date.get(Calendar.DAY_OF_MONTH);
        int amPm = date.get(Calendar.AM_PM);
        int hour = date.get(Calendar.HOUR);
        int minute = date.get(Calendar.MINUTE);
        String amPmStr = amPm > 0 ? "PM" : "AM";

        return String.format(Constants.locale, dateFormat, year, month, day, amPmStr, hour, minute);
    }

    // Get string representation of event location
    private String getLocationString() {
        EventLocation location = event.getLocation();

        if (location == null) {
            return null;
        }

        return String.format(Constants.locale, locationFormat, location.getTitle());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.event_detail_menu_exit) {

            // Send event exit request to server
            PokoServer server = PokoServer.getInstance();
            if (server != null) {
                server.sendEventExit(event.getEventId());
            }

            return true;
        }

        return false;
    }
}
