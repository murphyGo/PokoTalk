package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.event.EventLocation;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.user.User;
import com.naver.maps.geometry.LatLng;

import java.util.Calendar;
import java.util.List;

public class EventItem extends FrameLayout {
    private Context context;
    private PokoEvent event;
    private TextView eventNameView;
    private TextView eventTimeView;
    private TextView eventLocationView;
    private TextView eventDescriptionView;
    private TextView eventParticipantsView;

    public static final String timeFormat = "%02d:%02d";
    public static final String participantsFormat = "%s외 %d명";
    public static final String locationFormat = "%s에서";
    public static final String locationFormatLagLng = "%f, %f";

    public EventItem(Context context) {
        super(context);
        this.context = context;
    }

    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.event_item, this, true);
        eventNameView = view.findViewById(R.id.eventItemName);
        eventTimeView = view.findViewById(R.id.eventItemTime);
        eventLocationView = view.findViewById(R.id.eventItemLocation);
        eventDescriptionView = view.findViewById(R.id.eventItemDescription);
        eventParticipantsView = view.findViewById(R.id.eventItemParticipants);
    }

    public PokoEvent getEvent() {
        return event;
    }

    public void setEvent(PokoEvent event) {
        this.event = event;

        eventNameView.setText(event.getEventName());
        eventDescriptionView.setText(event.getDescription());
        eventTimeView.setText(getEventTimeString());
        eventParticipantsView.setText(getParticipantsString());
        eventLocationView.setText(getLocationString());
    }

    public String getEventTimeString() {
        if (event == null) {
            return null;
        }

        Calendar date = event.getEventDate();

        return String.format(Constants.locale,
                timeFormat,
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE));
    }

    public String getParticipantsString() {
        if (event == null) {
            return null;
        }

        List<User> participants = event.getParticipants().getList();

        int size = participants.size();
        if (size == 0) {
            return null;
        } else if (size == 1){
            return participants.get(0).getNickname();
        } else {
            return String.format(Constants.locale,
                    participantsFormat,
                    participants.get(0).getNickname(),
                    size - 1);
        }
    }

    public String getLocationString() {
        if (event == null) {
            return null;
        }

        EventLocation location = event.getLocation();
        if (location == null) {
            return null;
        }

        String title = location.getTitle();
        if (title != null) {
            return String.format(Constants.locale,
                    locationFormat,
                    title);
        }

        LatLng latLng = location.getLatLng();
        if (latLng != null) {
            return String.format(Constants.locale,
                    locationFormatLagLng,
                    latLng.latitude,
                    latLng.longitude);
        }

        return null;
    }


}
