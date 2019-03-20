package com.murphy.pokotalk.activity.chat;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.GroupMemberListAdapter;
import com.murphy.pokotalk.adapter.MessageListAdapter;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.file.PokoDatabase;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.MessageListUI;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.view.ListViewDetectable;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity
        implements PopupMenu.OnMenuItemClickListener,
        GroupExitWarningDialog.Listener,
        NavigationView.OnNavigationItemSelectedListener {
    private int groupId;
    private PokoServer server;
    private DataCollection collection;
    private TextView groupNameView;
    private Button backspaceButton;
    private ListViewDetectable messageListView;
    private ListView memberListView;
    private Toolbar slideMenuButton;
    private EditText messageInputView;
    private Button sendMessageButton;
    private DrawerLayout drawerLayout;
    private LinearLayout slideMenuLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle slideMenuToggle;
    private MessageListAdapter messageListAdapter;
    private GroupMemberListAdapter memberListAdapter;
    private Group group;
    private int sendId;
    private Session session;
    private boolean firstRead = true;
    public static final int slideMenuWidthDP = 250;
    public static final int MESSAGE_LOAD_NUM = 20;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        collection = DataCollection.getInstance();

        sendId = 0;

        groupNameView = findViewById(R.id.groupName);
        backspaceButton = findViewById(R.id.backspaceButton);
        slideMenuButton = findViewById(R.id.slideMenuButton);
        messageListView =  findViewById(R.id.messageList);
        messageInputView = findViewById(R.id.messageInputText);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        drawerLayout = findViewById(R.id.drawerLayout);

        Intent intent = getIntent();
        if (intent == null) {
            creationError("비정상적인 접근입니다.");
        }

        groupId = intent.getIntExtra("groupId", -1);
        if (groupId < 0) {
            creationError("그룹 ID 오류");
        }

        group = DataCollection.getInstance().getGroupList().getItemByKey(groupId);
        if (group == null) {
            creationError("해당 그룹이 없습니다.");
        }

        openChatting();

        /* Set group name */
        groupNameView.setText(group.getGroupName());

        server = PokoServer.getInstance(this);
        session = Session.getInstance();

        /* Create slide menu */
        LayoutInflater inflater = getLayoutInflater();
        slideMenuLayout = (LinearLayout) inflater.inflate(R.layout.chat_slide_menu_layout,
                null, false);
        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams
                ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, slideMenuWidthDP,
                        getResources().getDisplayMetrics()),
                DrawerLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.END;
        drawerLayout.addView(slideMenuLayout, params);

        /* Add slide menu toggle */
        slideMenuToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        setSupportActionBar(slideMenuButton);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        slideMenuToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(slideMenuToggle);

        /* Add slide menu item click listener */
        navigationView = slideMenuLayout.findViewById(R.id.chatSlideMenu);
        navigationView.setNavigationItemSelectedListener(this);

        try {
            DataLock.getInstance().acquireWriteLock();
            try {
                /* Member list */
                memberListAdapter = new GroupMemberListAdapter(this);
                memberListAdapter.getPokoList().copyFromPokoList(group.getMembers());
                memberListView = drawerLayout.findViewById(R.id.memberList);
                memberListView.setAdapter(memberListAdapter);
            } finally {
                DataLock.getInstance().releaseWriteLock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            DataLock.getInstance().acquireWriteLock();
            try {
                messageListAdapter = new MessageListAdapter(this);
                messageListAdapter.getPokoList().copyFromPokoList(group.getMessageList());
                messageListView.setAdapter(messageListAdapter);
                messageListView.setKeepVerticalPosition(true);
                messageListView.postScrollToBottom();
                messageListView.setReachTopCallback(new Runnable() {
                    @Override
                    public void run() {
                        AsyncTask task = new ReadMoreMessageTask();
                        task.execute(null, null);
                    }
                });
            } finally {
                DataLock.getInstance().releaseWriteLock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Add widget listeners */
        backspaceButton.setOnClickListener(backspaceButtonClickListener);
        sendMessageButton.setOnClickListener(messageSendButtonListener);

        /* Attach server event callbacks */
        server.attachActivityCallback(Constants.sessionLoginName, sessionLoginListener);
        server.attachActivityCallback(Constants.sendMessageName, addMessageListener);
        server.attachActivityCallback(Constants.readMessageName, readMessageListener);
        server.attachActivityCallback(Constants.newMessageName, addMessageListener);
        server.attachActivityCallback(Constants.getMemberJoinHistory, refreshMessageListener);
        server.attachActivityCallback(Constants.readNbreadOfMessages, messageAckListener);
        server.attachActivityCallback(Constants.messageAckName, messageAckListener);
        server.attachActivityCallback(Constants.membersInvitedName, membersInvitedListener);
        server.attachActivityCallback(Constants.membersExitName, memberExitListener);
        server.attachActivityCallback(Constants.exitGroupName, exitGroupListener);

        /* Read new messages after last acked message from server */
        requestUnackedMessages();
        /* Ack to last messages */
        sendAckToLastMessage();
        /* Request nbNotRead of messages read */
        requestNbreadOfMessages(group.getMessageList().getList());

        firstRead = true;
    }

    @Override
    protected void onDestroy() {
        server.detachActivityCallback(Constants.sessionLoginName, sessionLoginListener);
        server.detachActivityCallback(Constants.sendMessageName, addMessageListener);
        server.detachActivityCallback(Constants.readMessageName, readMessageListener);
        server.detachActivityCallback(Constants.newMessageName, addMessageListener);
        server.detachActivityCallback(Constants.getMemberJoinHistory, refreshMessageListener);
        server.detachActivityCallback(Constants.readNbreadOfMessages, messageAckListener);
        server.detachActivityCallback(Constants.messageAckName, messageAckListener);
        server.detachActivityCallback(Constants.membersInvitedName, membersInvitedListener);
        server.detachActivityCallback(Constants.membersExitName, memberExitListener);
        server.detachActivityCallback(Constants.exitGroupName, exitGroupListener);

        closeChatting();

        super.onDestroy();
    }

    /* Overriding methods */
    @Override
    public void onBackPressed() {
        /* if drawer is opened, close drawer */
        if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            drawerLayout.closeDrawer(Gravity.RIGHT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                drawerLayout.closeDrawer(Gravity.RIGHT);
            }
            else {
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        if (slideMenuToggle != null)
            slideMenuToggle.syncState();
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (slideMenuToggle != null)
            slideMenuToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    private void openChatting() {
        if (!collection.startChat(group)) {
            // chat failed to start.
            Toast.makeText(this, "채팅 실행 중 문제가 발생했습니다.",
                    Toast.LENGTH_SHORT);
            finish();
        }
    }

    private void closeChatting() {
        Intent intent = new Intent();
        intent.putExtra("groupId", group.getGroupId());
        setResult(RESULT_OK, intent);

        collection.endChat(group);
    }

    private void creationError(String errMsg) {
        Toast.makeText(getApplicationContext(), errMsg,
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }


    private PokoMessage createSentMessage(int sendId, String content, @Nullable Integer importanceLevel) {
        PokoMessage message = new PokoMessage();
        message.setWriter(session.getUser());
        message.setSendId(sendId);
        message.setContent(content);
        message.setMessageType(importanceLevel);

        return message;
    }

    /* Button listeners */
    private View.OnClickListener backspaceButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private View.OnClickListener messageSendButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                DataLock.getInstance().acquireWriteLock();
                try {
                    String content = messageInputView.getText().toString();
                    if (content.length() == 0) {
                        Toast.makeText(getApplicationContext(), "메시지를 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendId++;
                    PokoMessage message = createSentMessage(sendId, content, PokoMessage.NORMAL);
                    server.sendNewMessage(group.getGroupId(), sendId, content, PokoMessage.NORMAL);
                    group.getMessageList().addSentMessage(message);
                    messageInputView.setText("");
                } finally {
                    DataLock.getInstance().releaseWriteLock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /* Server event listeners */
    // This callback get called when connection to server gets lost and
    // the user reconnects and login. So we request data to be up to date.
    private ActivityCallback sessionLoginListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            /* Read new messages after last acked message from server */
            requestUnackedMessages();
            /* Ack to last messages */
            sendAckToLastMessage();
            /* Request nbNotRead of messages read */
            requestNbreadOfMessages(group.getMessageList().getList());
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback readMessageListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group readGroup = (Group) getData("group");
            final ArrayList<PokoMessage> readMessages =
                    (ArrayList<PokoMessage>) getData("messages");
            /* Refresh message list */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /* Update all messages */
                    if (readGroup == null || readMessages == null)
                        return;

                    if (readGroup.getGroupId() == group.getGroupId()) {
                        MessageListUI adapterList = (MessageListUI) messageListAdapter.getPokoList();
                        for (PokoMessage message : readMessages) {
                            adapterList.updateItem(message);
                        }
                        messageListAdapter.notifyDataSetChanged();
                        messageListView.postScrollToBottom();

                        // Ack to last message.
                        if (readMessages.size() > 0) {
                            sendAckToLastMessage();
                        }

                        // If server gave maximum number of message request next messages
                        if (readMessages.size() >= Constants.nbMessageRead) {
                            requestUnackedMessages();
                        }
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback refreshMessageListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            /* Refresh message list */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback addMessageListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group readGroup = (Group) getData("group");
            final PokoMessage newMessage = (PokoMessage) getData("message");
            /* Refresh message list */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (readGroup == null || newMessage == null)
                        return;

                    if (readGroup.getGroupId() == group.getGroupId()) {
                        boolean myMessage = newMessage.isMyMessage(session);
                        /* Ack if it is a new message */
                        int messageId = newMessage.getMessageId();
                        if (newMessage.getNbNotReadUser() > 0 && !myMessage) {
                            server.sendAckMessage(group.getGroupId(), 0, messageId);
                        }

                        /* If it's my message or fully at bottom, scroll down */
                        if (myMessage || messageListView.isFullyAtBottom()) {
                            messageListView.postScrollToBottom();
                        }
                        messageListAdapter.getPokoList().updateItem(newMessage);
                        messageListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback messageAckListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /* Refresh NbNotReadUser number */
                    messageListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback membersInvitedListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group readGroup = (Group) getData("group");
            final ArrayList<User> members = (ArrayList<User>) getData("members");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (readGroup == null || members == null)
                        return;

                    if (readGroup.getGroupId() == group.getGroupId()) {
                        UserList memberList = (UserList) memberListAdapter.getPokoList();
                        for (User member : members) {
                            memberList.updateItem(member);
                        }
                        memberListAdapter.notifyDataSetChanged();
                    }
                }
           });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback memberExitListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group readGroup = (Group) getData("group");
            final ArrayList<User> members = (ArrayList<User>) getData("members");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (readGroup == null || members == null)
                        return;

                    if (readGroup.getGroupId() == group.getGroupId()) {
                        UserList memberList = (UserList) memberListAdapter.getPokoList();
                        for (User member : members) {
                            memberList.removeItemByKey(member.getUserId());
                        }
                        memberListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback exitGroupListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            Integer groupId = (Integer) getData("groupId");
            if (groupId == null)
                return;

            if (groupId == group.getGroupId()) {
                closeChatting();
                finish();
            }
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    /* Popup menu code */
    private void open_menu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.chat_menu);
        popup.show();
    }

    private void open_exit_warning() {
        GroupExitWarningDialog warningDialog = new GroupExitWarningDialog();
        warningDialog.show(getSupportFragmentManager(), "채팅방 나가기 경고");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return menuItemClick(menuItem);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return menuItemClick(menuItem);
    }

    private boolean menuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.invite_contacts:
                Intent intent = new Intent(this, GroupMemberInvitationActivity.class);
                intent.putExtra("groupId", group.getGroupId());
                startActivity(intent);
                break;
            case R.id.exit_group:
                open_exit_warning();
                break;
            default:
                Toast.makeText(this, "문제가 발생했습니다.",
                        Toast.LENGTH_SHORT).show();
                return false;
        }

        return true;
    }

    /* Group exit dialog listener */
    @Override
    public void groupExitOptionApply(int option) {
        switch(option) {
            case GroupExitWarningDialog.EXIT_GROUP:
                server.sendExitGroup(group.getGroupId());
                break;
            case GroupExitWarningDialog.CANCEL:
                break;
            default:
                return;
        }
    }

    /* Get new messages */
    protected void requestUnackedMessages() {
        /* Read new messages after last message from server */
        server.sendReadMessage(group.getGroupId(),
                group.getAck() + 1,
                Constants.nbMessageRead);
    }

    protected void sendAckToLastMessage() {
        // Get last message
        MessageListUI messageListUI = (MessageListUI) messageListAdapter.getPokoList();
        PokoMessage lastMessage = messageListUI.getLastMessage();
        if (lastMessage != null) {
            int lastMessageId = lastMessage.getMessageId();
            int ack = group.getAck();

            if (lastMessageId > ack) {
                // Ack till last message.
                server.sendAckMessage(group.getGroupId(), 0, lastMessageId);
            }
        }
    }

    /* Request server to nbread of messages with nbread greater than 0.
     * (not seen by every member yet) */
    protected void requestNbreadOfMessages(ArrayList<PokoMessage> messages) {
        int minId, maxId;

        if (messages.size() == 0) {
            return;
        }

        PokoMessage firstMessage = messages.get(0);
        minId = firstMessage.getMessageId();
        maxId = minId;

        /* Find minimum and maximum message id */
        for (int i = 1; i < messages.size(); i++) {
            PokoMessage message = messages.get(i);
            int messageId = message.getMessageId();

            if (message.getNbNotReadUser() > 0) {
                if (minId > messageId) {
                    minId = messageId;
                }

                if (maxId < messageId) {
                    maxId = messageId;
                }
            }
        }

        Log.v("POKO", "request nbread min "+ minId + ", max " + maxId);
        server.sendReadNbreadOfMessages(group.getGroupId(), minId, maxId);
    }

    class ReadMoreMessageTask extends AsyncTask<Object, Void, ArrayList<PokoMessage>> {

        @Override
        protected ArrayList<PokoMessage> doInBackground(Object... args) {
            ArrayList<PokoMessage> readMessages = new ArrayList<>();

            try {
                DataLock.getInstance().acquireWriteLock();

                try {
                    // Find first messageId to start reading message.
                    MessageList messageList = group.getMessageList();
                    int startId;
                    if (messageList.getList().size() == 0) {
                        startId = -1;
                    } else {
                        PokoMessage firstMessage = messageList.getList().get(0);
                        startId = firstMessage.getMessageId() - 1;
                    }

                    // Now we mark current scroll position w.r.t second visible item
                    // because first item is always date change message and it will be removed.
                    // So we mark position of second item that will remain after update.
                    messageListView.markScrollPosition(1);

                    Log.v("POKO", "Mark position");

                    // Get a database for reading
                    PokoDatabase pokoDatabase = PokoDatabase.getInstance(getApplicationContext());
                    SQLiteDatabase db = pokoDatabase.getReadableDatabase();

                    // Query to read a next at most MESSAGE_LOAD_NUM messages
                    Cursor cursor;
                    if (startId < 0) {
                        cursor = PokoDatabaseHelper.readMessageDataFromBack(db,
                                group.getGroupId(), 0, MESSAGE_LOAD_NUM);
                    } else {
                        cursor = PokoDatabaseHelper.readMessageDataFromBackByMessageId(db,
                                group.getGroupId(), startId, MESSAGE_LOAD_NUM);
                    }

                    // Parse all messages and add to message list
                    try {
                        while (cursor.moveToNext()) {
                            try {
                                PokoMessage message = Parser.parseMessage(cursor);
                                if (messageList.updateItem(message) == message) {
                                    readMessages.add(message);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.v("POKO", "Failed to parse message");
                            }
                        }
                    } finally {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // We must scroll to mark after mark
                    messageListView.scrollToMark(0);
                } finally {
                    DataLock.getInstance().releaseWriteLock();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return readMessages;
        }

        @Override
        protected void onPostExecute(ArrayList<PokoMessage> readMessages) {
            if (readMessages != null) {
                try {
                    DataLock.getInstance().acquireWriteLock();
                    try {
                        MessageListUI messageListUI = (MessageListUI) messageListAdapter.getPokoList();
                        // We now count delta of number of date change message.
                        messageListUI.startCountDateChangeMessage();
                        for (PokoMessage message : readMessages) {
                            messageListUI.updateItem(message);
                        }

                        int dateChangeMessageNumDelta = messageListUI.endCountDateChangeMessage();

                        messageListAdapter.notifyDataSetChanged();
                        int size = readMessages.size() + dateChangeMessageNumDelta;
                        Log.v("POKO", "READ " + readMessages.size() + " messages");
                        messageListView.scrollToMark(size);

                    } finally {
                        DataLock.getInstance().releaseWriteLock();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                /* Request nbNotRead of messages read */
                requestNbreadOfMessages(readMessages);
            }

            /* If it is a first read, scrolls to bottom */
            if (ChatActivity.this.firstRead) {
                messageListView.postScrollToBottom();
                ChatActivity.this.firstRead = false;
            }
        }
    }
}
