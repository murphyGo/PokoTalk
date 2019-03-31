package com.murphy.pokotalk.activity.event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.group.MemberCandidateListAdapter;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.event.EventLocation;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactPokoList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.parser.PokoParser;
import com.murphy.pokotalk.view.MemberCandidateItem;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EventCreationActivity extends AppCompatActivity
        implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {
    private EditText eventNameEditText;
    private EditText eventDescriptionEditText;
    private EditText eventLocationEditText;
    private ListView eventParticipantListView;
    private EditText yearEditText;
    private EditText monthEditText;
    private EditText dayEditText;
    private EditText hourEditText;
    private EditText minuteEditText;
    private Button eventCreationButton;
    private Toolbar backspaceButton;
    private ImageView locationLayout;
    private ImageView dateLayout;
    private ImageView timeLayout;
    private MemberCandidateListAdapter adapter;
    private EventLocation location;
    private HashMap<Long, Contact> participants;

    public static final int EVENT_LOCATION_ACTIVITY = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_creation_layout);

        participants = new HashMap<>();

        // Find views
        eventNameEditText = findViewById(R.id.eventCreationEventName);
        eventDescriptionEditText = findViewById(R.id.eventCreationDescription);
        eventLocationEditText = findViewById(R.id.eventCreationLocation);
        eventParticipantListView = findViewById(R.id.eventCreationMemberList);
        eventCreationButton = findViewById(R.id.eventCreationCreateButton);
        yearEditText = findViewById(R.id.eventCreationYear);
        monthEditText = findViewById(R.id.eventCreationMonth);
        dayEditText = findViewById(R.id.eventCreationDate);
        hourEditText = findViewById(R.id.eventCreationHour);
        minuteEditText = findViewById(R.id.eventCreationMinute);
        backspaceButton = findViewById(R.id.backspaceButton);
        locationLayout = findViewById(R.id.eventCreationLocationLayout);
        dateLayout = findViewById(R.id.eventCreationDateLayout);
        timeLayout = findViewById(R.id.eventCreationTimeLayout);

        // Set multi choice mode of list view
        eventParticipantListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        eventParticipantListView.setMultiChoiceModeListener(contactListChoiceListener);
        eventParticipantListView.setDrawSelectorOnTop(true);

        // Set adapter
        adapter = new MemberCandidateListAdapter(this);
        ContactPokoList contactListUI = (ContactPokoList) adapter.getPokoList();
        ContactPokoList contactList = DataCollection.getInstance().getContactList();
        contactListUI.copyFromPokoList(contactList);
        eventParticipantListView.setAdapter(adapter);

        eventParticipantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = participants.get(id);

                eventParticipantListView.setItemChecked(position, contact == null);
            }
        });

        // Event date setting listener
        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show date picker dialog
                EventCalendarPickerDialog dialog = new EventCalendarPickerDialog();

                dialog.show(getSupportFragmentManager(), "eventDate");
            }
        });

        // Event time setting listener
        timeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show date picker dialog
                EventTimePickerDialog dialog = new EventTimePickerDialog();

                dialog.show(getSupportFragmentManager(), "eventTime");
            }
        });

        // Event location setting listener
        locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start event location selection activity
                Intent intent = new Intent(getApplicationContext(),
                        EventLocationSelectionActivity.class);

                if (location != null) {
                    // Put location data if selected
                    intent.putExtra("selected", true);
                    intent.putExtra("title", location.getTitle());
                    intent.putExtra("category", location.getCategory());
                    intent.putExtra("address", location.getAddress());
                    intent.putExtra("latLng", location.getLatLng());
                }

                // Start activity
                startActivityForResult(intent, EVENT_LOCATION_ACTIVITY);
            }
        });

        // Event creation button listener
        eventCreationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkValidInput()) {
                    return;
                }

                // Get name
                String name = eventNameEditText.getText().toString();

                // Get description
                String description = eventDescriptionEditText.getText().toString();

                // Get Date
                Calendar date = Calendar.getInstance();
                date.setTimeZone(Constants.timeZone);
                date.set(Calendar.YEAR, Integer.parseInt(yearEditText.getText().toString()));
                date.set(Calendar.MONTH, Integer.parseInt(monthEditText.getText().toString()) - 1);
                date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayEditText.getText().toString()));
                date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourEditText.getText().toString()));
                date.set(Calendar.MINUTE, Integer.parseInt(minuteEditText.getText().toString()));

                Log.v("PKOK", "DATE " + PokoParser.formatCalendar(date));

                // Get emails of participants
                List<String> emails = new ArrayList<>();
                Iterator<Contact> participantsIter = participants.values().iterator();
                while (participantsIter.hasNext()) {
                    Contact participant = participantsIter.next();
                    emails.add(participant.getEmail());
                }

                // Get server
                PokoServer server = PokoServer.getInstance(getApplicationContext());

                // Send event add request
                server.sendCreateEvent(name, description, emails, date, location);

                // Finish activity
                setResult(RESULT_OK);
                finish();
            }
        });

        // Set backspace button
        setSupportActionBar(backspaceButton);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private boolean checkValidInput() {
        if (eventNameEditText.getText().toString().trim().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.event_creation_no_name,
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        if (yearEditText.getText().toString().trim().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.event_creation_no_date,
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        if (hourEditText.getText().toString().trim().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.event_creation_no_time,
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    // Multi choice listener for contact list view
    AbsListView.MultiChoiceModeListener contactListChoiceListener =
            new AbsListView.MultiChoiceModeListener() {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            Contact contact = adapter.getItem(position);

            // Toggle list item
            MemberCandidateItem view = (MemberCandidateItem) adapter.getView((long)contact.getUserId());

            // Add or remove from list
            if (checked) {
                Log.v("POKO", contact.toString() + " selected");
                participants.put((long)contact.getUserId(), contact);
                view.selectItem();
            } else {
                Log.v("POKO", contact.toString() + " unselected");
                participants.remove((long)contact.getUserId());
                view.cancelItem();
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.v("POKO", "onCreateActionMode");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.v("POKO", "onPrepareActionMode");
            menu.clear();
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.v("POKO", "onActionItemClicked");
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.v("POKO", "onDestroyActionMode");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch(requestCode) {
            case EVENT_LOCATION_ACTIVITY: {
                // Parse intent data
                String title = data.getStringExtra("title");
                String category = data.getStringExtra("category");
                String address = data.getStringExtra("address");
                LatLng latLng = data.getParcelableExtra("latLng");

                // Make event location
                location = new EventLocation();
                location.setTitle(title);
                location.setCategory(category);
                location.setAddress(address);
                location.setLatLng(latLng);

                if (title != null) {
                    // Set location text
                    eventLocationEditText.setText(title);
                }

                break;
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        yearEditText.setText(Integer.toString(year));
        monthEditText.setText(Integer.toString(month + 1));
        dayEditText.setText(Integer.toString(dayOfMonth));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        hourEditText.setText(Integer.toString(hourOfDay));
        minuteEditText.setText(Integer.toString(minute));
    }
}
