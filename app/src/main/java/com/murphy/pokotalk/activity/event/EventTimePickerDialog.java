package com.murphy.pokotalk.activity.event;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.murphy.pokotalk.Constants;

import java.util.Calendar;

public class EventTimePickerDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar now = Calendar.getInstance();
        now.setTimeZone(Constants.timeZone);
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        return new android.app.TimePickerDialog(getActivity(),
                (TimePickerDialog.OnTimeSetListener) getActivity(),
                hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
    }
}
