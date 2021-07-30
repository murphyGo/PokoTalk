package com.murphy.pokotalk.activity.event;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.murphy.pokotalk.data.event.PokoEvent;

public class EventOptionDialog extends DialogFragment {
    private Listener listener;
    private PokoEvent event;
    private String[] optionStrings = {"자세히", "나가기"};

    public static final int OPTION_DETAIL = 0;
    public static final int OPTION_EXIT = 1;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setItems(optionStrings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.eventOptionClick(event, which);
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ContactOptionDialogListener");
        }
    }

    public PokoEvent getEvent() {
        return event;
    }

    public void setEvent(PokoEvent event) {
        this.event = event;
    }

    public interface Listener {
        void eventOptionClick(PokoEvent event, int option);
    }
}
