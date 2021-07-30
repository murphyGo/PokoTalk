package com.murphy.pokotalk.activity.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

import com.murphy.pokotalk.data.group.Group;

public class GroupOptionDialog extends AppCompatDialogFragment {
    private Group group;
    private GroupOptionDialogListener listener;
    private String[] optionStrings = {"채팅", "친구 초대", "나가기"};

    public static final int CHAT = 0;
    public static final int INVITE_CONTACT = 1;
    public static final int EXIT_GROUP = 2;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("채팅 옵션").
                setItems(optionStrings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.groupOptionClick(group, which);
                        dismiss();
                    }
                });

        return builder.create();
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (GroupOptionDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement GroupOptionDialogListener");
        }
    }

    public interface GroupOptionDialogListener {
        void groupOptionClick(Group group, int option);
    }
}
