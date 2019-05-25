package com.murphy.pokotalk.activity.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.group.PokoMessage;

public class ChatMessageOptionDialog extends AppCompatDialogFragment {
    private Listener listener;
    private PokoMessage message;

    // Options
    public static final int OPTION_COPY = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement " + ChatMessageOptionDialog.class.toString() + " listener");
        }

    }

    public interface Listener {
        void onMessageOptionSelected(PokoMessage message, int option);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.chat_message_option_title)
                .setItems(R.array.chatMessageOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onMessageOptionSelected(message, which);
                        }
                    }
                });

        return builder.create();
    }

    public PokoMessage getMessage() {
        return message;
    }

    public void setMessage(PokoMessage message) {
        this.message = message;
    }
}
