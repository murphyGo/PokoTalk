package com.murphy.pokotalk.activity.event;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.adapter.event.EventListAdapter;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.PokoLock;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.event.EventListUI;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

public class EventListFragment extends Fragment {
    private PokoServer server;
    private ListView eventListView;
    private Button eventCreateButton;
    private EventListAdapter eventListAdapter;
    private Listener listener;

    public interface Listener {
        void openEventOptionDialog(PokoEvent event);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (EventListFragment.Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " should implement listener");
        }
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
            PokoLock.getDataLockInstance().acquireWriteLock();

            try {
                // Get event list
                EventList eventList = DataCollection.getInstance().getEventList();

                // Create event list adapter
                eventListAdapter = new EventListAdapter(getContext());
                eventListAdapter.setViewCreationCallback(eventViewCreationCallback);

                // Copy events
                EventListUI eventListUI = (EventListUI) eventListAdapter.getPokoList();
                eventListUI.copyFromPokoList(eventList);

                // Set adapter
                eventListView.setAdapter(eventListAdapter);
            } finally {
                PokoLock.getDataLockInstance().releaseWriteLock();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Get server
        server = PokoServer.getInstance();

        // Attach callbacks
        server.attachActivityCallback(Constants.getEventListName, getEventListListener);
        server.attachActivityCallback(Constants.eventCreatedName, addEventListener);
        server.attachActivityCallback(Constants.eventExitName, removeEventListener);
        server.attachActivityCallback(Constants.eventAckName, refreshEventListListener);
        server.attachActivityCallback(Constants.eventParticipantExitedName, refreshEventListListener);
        server.attachActivityCallback(Constants.eventStartedName, refreshEventListListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Detach callbacks
        if (server != null) {
            server.detachActivityCallback(Constants.getEventListName, getEventListListener);
            server.detachActivityCallback(Constants.eventCreatedName, addEventListener);
            server.detachActivityCallback(Constants.eventExitName, removeEventListener);
            server.detachActivityCallback(Constants.eventAckName, refreshEventListListener);
            server.detachActivityCallback(Constants.eventParticipantExitedName, refreshEventListListener);
            server.detachActivityCallback(Constants.eventStartedName, refreshEventListListener);
        }
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventList eventList = DataCollection.getInstance().getEventList();
                    EventListUI eventListUI = (EventListUI) eventListAdapter.getPokoList();
                    eventListUI.copyFromPokoList(eventList);
                    eventListUI.sortItemsByKey();
                    eventListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback addEventListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final PokoEvent event = (PokoEvent) getData("event");

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (event != null) {
                        EventListUI eventListUI = (EventListUI) eventListAdapter.getPokoList();
                        eventListUI.updateItem(event);
                        eventListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback removeEventListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Integer eventId = (Integer) getData("eventId");
            
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (eventId != null) {
                        Log.v("POKO", "REMOVE EVENT " + Integer.toString(eventId));
                        EventListUI eventListUI = (EventListUI) eventListAdapter.getPokoList();
                        eventListUI.removeItemByKey(eventId);
                        eventListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback refreshEventListListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    eventListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    // View creation callbacks
    private ViewCreationCallback eventViewCreationCallback = new ViewCreationCallback<PokoEvent>() {
        @Override
        public void run(View view, final PokoEvent event) {
            // Set on click listener
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start event detail activity
                    Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                    intent.putExtra("eventId", event.getEventId());
                    startActivity(intent);
                }
            });

            // Set long click listener
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.openEventOptionDialog(event);
                    return true;
                }
            });
        }
    };
}
