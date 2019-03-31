package com.murphy.pokotalk.activity.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.activity.event.EventCreationActivity;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.adapter.event.EventListAdapter;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.event.EventListUI;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.view.EventItem;

import static com.murphy.pokotalk.server.parser.PokoParser.collection;

public class EventListFragment extends Fragment {
    private PokoServer server;
    private ListView eventListView;
    private Button eventCreateButton;
    private EventListAdapter eventListAdapter;
    private Listener listener;

    public interface Listener {

    }


    @Override
    public void onAttach(Context context) {
        try {
            listener = (EventListFragment.Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " should implement listener");
        }
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_list_layout, null, false);

        // Find views
        eventListView = view.findViewById(R.id.eventList);
        eventCreateButton = view.findViewById(R.id.eventAddButton);

        // Set event listeners
        eventCreateButton.setOnClickListener(eventAddButtonClickListener);

        try {
            DataLock.getInstance().acquireWriteLock();

            try {
                // Get event list
                EventList eventList = collection.getEventList();

                // Create event list adapter
                eventListAdapter = new EventListAdapter(getContext());
                eventListAdapter.setViewCreationCallback(eventCreationCallback);

                // Copy events
                EventListUI eventListUI = (EventListUI) eventListAdapter.getPokoList();
                eventListUI.copyFromPokoList(eventList);

                // Set adapter
                eventListView.setAdapter(eventListAdapter);
            } finally {
                DataLock.getInstance().releaseWriteLock();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Get server
        server = PokoServer.getInstance(getContext());

        // Attach callbacks
        server.attachActivityCallback(Constants.getEventListName, getEventListListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get server
        PokoServer server = PokoServer.getInstance(getContext());

        // Detach callbacks
        server.detachActivityCallback(Constants.getEventListName, getEventListListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == Constants.RequestCode.EVENT_CREATE.value) {

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // User event listeners
    private View.OnClickListener eventAddButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), EventCreationActivity.class);
            startActivityForResult(intent, Constants.RequestCode.EVENT_CREATE.value);
        }
    };

    // Activity callbacks
    private ActivityCallback getEventListListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {

        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    // View creation callbacks
    private ViewCreationCallback eventCreationCallback = new ViewCreationCallback<PokoEvent>() {
        @Override
        public void run(View view, PokoEvent g) {
            EventItem eventView = (EventItem) view;
            final PokoEvent event = eventView.getEvent();

            eventView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            eventView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    return true;
                }
            });
        }
    };
}
