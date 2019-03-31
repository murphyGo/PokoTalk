package com.murphy.pokotalk.data;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.murphy.pokotalk.activity.chat.ChatActivity;
import com.murphy.pokotalk.data.group.Group;

public class ChatManager {
    private static Group chattingGroup = null;   // Group that now chatting

    public static void startChatActivity(Fragment activity, int requestCode, Group group) {
        /* Start group chat */
        Intent intent = new Intent(activity.getContext(), ChatActivity.class);
        intent.putExtra("groupId", group.getGroupId());
        activity.startActivityForResult(intent, requestCode);
        Log.v("POKO", "START GROUP CHAT " + group.getGroupId());
    }

    /* Chat methods */
    // Starts a chat, returns true if started chat.
    // if other chat is going already, it fails and returns false.
    public synchronized static boolean startChat(Group group) {
        if (getChattingGroup() != null) {
            return false;
        }
        chattingGroup = group;
        return true;
    }


    // End a chat so that user can start another chat.
    // It ends chat only when given group is the group holding chat.
    public synchronized static void endChat(Group group) {
        if (getChattingGroup() == group) {
            chattingGroup = null;
        }
    }

    public synchronized static boolean isChatting() {
        return getChattingGroup() != null;
    }

    public synchronized static Group getChattingGroup() {
        return chattingGroup;
    }
}
