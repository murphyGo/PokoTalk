package com.murphy.pokotalk.activity.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.adapter.locationShare.LocationShareUserSelectAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationShareSelectUserDialog extends AppCompatDialogFragment {
    private Listener listener;
    private HashMap<String, UserPosition> markers;
    private ListView userListView;
    private LocationShareUserSelectAdapter adapter;

    public interface Listener {
        void onSelectUserPosition(String key);
    }

    public void setMarkers(HashMap<String, UserPosition> markers) {
        this.markers = markers;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get activity and context
        Activity activity = getActivity();
        Context context = getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if (activity == null || context == null || markers == null) {
            dismiss();
            return builder.create();
        }

        // Create layout
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.location_share_select_user_dialog,
                null,false);

        // Get view
        userListView = view.findViewById(R.id.locationShareSelectUserListView);

        // Make list of user positions
        List<UserPosition> positions = new ArrayList<>(markers.values());

        // Set adapter
        adapter = new LocationShareUserSelectAdapter(getContext(), positions);
        adapter.setViewCreationCallback(positionViewCreationCallback);
        userListView.setAdapter(adapter);

        // Configure builder
        builder.setTitle(R.string.location_share_select_user_title)
                .setView(view);

        return builder.create();
    }

    private ViewCreationCallback<UserPosition> positionViewCreationCallback =
            new ViewCreationCallback<UserPosition>() {
                @Override
                public void run(View view, final UserPosition item) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Start callback
                            if (listener != null) {
                                listener.onSelectUserPosition(item.getKey());
                            }

                            // Finish dialog
                            dismiss();
                        }
                    });
                }
            };
}
