package com.murphy.pokotalk.activity.contact;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.murphy.pokotalk.R;

public class ContactAddDialog extends AppCompatDialogFragment {
    private EditText emailText;
    private ContactAddDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.contact_add_dialog, null, false);

        builder.setView(view)
                .setTitle("친구 추가")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
        .setPositiveButton("추가", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailText.getText().toString();
                listener.contactAdd(email);
            }
        });

        emailText = view.findViewById(R.id.contactAddEmail);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ContactAddDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ContactAddDialogListener");
        }
    }

    public interface ContactAddDialogListener {
        void contactAdd(String email);
    }
}
