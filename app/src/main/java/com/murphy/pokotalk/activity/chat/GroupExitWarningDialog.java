package com.murphy.pokotalk.activity.chat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.group.Group;

public class GroupExitWarningDialog extends AppCompatDialogFragment {
    private Listener listener;
    public static final int EXIT_GROUP = 0;
    public static final int CANCEL = 1;
    protected Group group;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.group_exit_warning_message)
                .setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.groupExitOptionApply(group, EXIT_GROUP);
                        dismiss();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.groupExitOptionApply(group, CANCEL);
                        dismiss();
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
            throw new ClassCastException("Caller activity must implement Listener subclass");
        }
    }

    public interface Listener {
        void groupExitOptionApply(Group group, int option);
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}


