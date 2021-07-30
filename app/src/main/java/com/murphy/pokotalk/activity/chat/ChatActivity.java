package com.murphy.pokotalk.activity.chat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.adapter.chat.MessageListAdapter;
import com.murphy.pokotalk.adapter.group.GroupMemberListAdapter;
import com.murphy.pokotalk.content.ContentManager;
import com.murphy.pokotalk.content.ContentStream;
import com.murphy.pokotalk.content.ContentTransferManager;
import com.murphy.pokotalk.content.image.ImageEncoder;
import com.murphy.pokotalk.data.ChatManager;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.PokoLock;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.db.PokoUserDatabase;
import com.murphy.pokotalk.data.db.json.Parser;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.MessageListUI;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.locationShare.LocationShare;
import com.murphy.pokotalk.data.locationShare.LocationShareHelper;
import com.murphy.pokotalk.data.locationShare.LocationShareRoom;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.service.ContentService;
import com.murphy.pokotalk.view.ListViewDetectable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity
        implements PopupMenu.OnMenuItemClickListener,
        GroupExitWarningDialog.Listener,
        NavigationView.OnNavigationItemSelectedListener,
        ChatAttachOptionFragment.Listener,
        ChatMessageOptionDialog.Listener {
    private Context context;
    private int groupId;
    private PokoServer server;
    private DataCollection collection;
    private TextView groupNameView;
    private Button backspaceButton;
    private ListViewDetectable messageListView;
    private ListView memberListView;
    private Toolbar slideMenuButton;
    private EditText messageInputView;
    private HorizontalScrollView attachOptionLayout;
    private Button attachOptionButton;
    private Button sendMessageButton;
    private Button locationShareButton;
    private SwitchButton locationShareToggleButton;
    private DrawerLayout drawerLayout;
    private LinearLayout slideMenuLayout;
    private FrameLayout locationShareLayout;
    private LocationShareFragment locationShareFragment = null;
    private NavigationView navigationView;
    private ActionBarDrawerToggle slideMenuToggle;
    private MessageListAdapter messageListAdapter;
    private GroupMemberListAdapter memberListAdapter;
    private Group group;
    private PokoEvent event;
    private LocationShareRoom room = null;
    private ChatAttachOptionFragment attachOptionFragment;
    private int sendId;
    private Session session;
    private boolean firstRead = true;
    private Integer lastMessageIdAtFirst = null;
    public static final int slideMenuWidthDP = 250;
    public static final int MESSAGE_LOAD_NUM = 20;

    public static final int locationShareWidthDP = 400;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        context = this;
        collection = DataCollection.getInstance();

        sendId = 0;

        groupNameView = findViewById(R.id.groupName);
        backspaceButton = findViewById(R.id.backspaceButton);
        slideMenuButton = findViewById(R.id.slideMenuButton);
        messageListView =  findViewById(R.id.messageList);
        messageInputView = findViewById(R.id.messageInputText);
        attachOptionLayout = findViewById(R.id.chatAttachOptionLayout);
        attachOptionButton = findViewById(R.id.chatAttachOptionButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        drawerLayout = findViewById(R.id.drawerLayout);
        locationShareButton = findViewById(R.id.chatLocationShareButton);
        locationShareToggleButton = findViewById(R.id.chatLocationShareToggle);
        locationShareLayout = findViewById(R.id.chatLocationShareLayout);
        locationShareLayout.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent == null) {
            creationError("비정상적인 접근입니다.");
            return;
        }

        groupId = intent.getIntExtra("groupId", -1);

        if (groupId < 0) {
            creationError("그룹 ID 오류");
            return;
        }

        group = DataCollection.getInstance().getGroupList().getItemByKey(groupId);
        if (group == null) {
            creationError("해당 그룹이 없습니다.");
            return;
        }

        // Get event list
        EventList eventList = collection.getEventList();

        // Get event information
        EventList.EventGroupRelation relation = eventList.getEventGroupRelationByGroupId(groupId);

        if (relation != null) {
            event = eventList.getItemByKey(relation.getEventId());
        } else {
            event = null;
        }

        /* Start chat for this group */
        openChatting();

        /* Set group name */
        groupNameView.setText(group.getGroupName());

        /* Get server and session */
        server = PokoServer.getInstance();
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
            PokoLock.getDataLockInstance().acquireWriteLock();
            try {
                /* Member list */
                memberListAdapter = new GroupMemberListAdapter(this);
                memberListAdapter.getPokoList().copyFromPokoList(group.getMembers());
                memberListView = drawerLayout.findViewById(R.id.memberList);
                memberListView.setAdapter(memberListAdapter);
            } finally {
                PokoLock.getDataLockInstance().releaseWriteLock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            PokoLock.getDataLockInstance().acquireWriteLock();
            try {
                messageListAdapter = new MessageListAdapter(this);
                messageListAdapter.getPokoList().copyFromPokoList(group.getMessageList());
                messageListAdapter.setViewCreationCallback(messageViewCreationCallback);
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
                PokoLock.getDataLockInstance().releaseWriteLock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Add widget listeners */
        backspaceButton.setOnClickListener(backspaceButtonClickListener);
        sendMessageButton.setOnClickListener(messageSendButtonListener);
        attachOptionButton.setOnClickListener(attachOptionButtonListener);
        locationShareButton.setOnClickListener(locationShareClickListener);
        locationShareToggleButton.setOnCheckedChangeListener(locationShareToggleListener);

        /* Do not show attach option layout at first */
        attachOptionLayout.setVisibility(View.GONE);

        /* Attach server event callbacks */
        server.attachActivityCallback(Constants.sessionLoginName, sessionLoginListener);
        server.attachActivityCallback(Constants.sendMessageName, addMessageListener);
        server.attachActivityCallback(Constants.readMessageName, readMessageListener);
        server.attachActivityCallback(Constants.newMessageName, addMessageListener);
        server.attachActivityCallback(Constants.getMemberJoinHistory, refreshMessageListener);
        server.attachActivityCallback(Constants.readNbreadOfMessages, refreshMessageListener);
        server.attachActivityCallback(Constants.messageAckName, messageAckListener);
        server.attachActivityCallback(Constants.ackMessageName, ackMessageListener);
        server.attachActivityCallback(Constants.membersInvitedName, membersInvitedListener);
        server.attachActivityCallback(Constants.membersExitName, memberExitListener);
        server.attachActivityCallback(Constants.exitGroupName, exitGroupListener);

        if (event != null) {
            // Get room for location share
            room = LocationShareHelper.getInstance()
                    .getRoomOrCreateIfNotExists(event.getEventId());

            if (room != null) {
                // Add listener
                room.addListener(roomStateChangeListener);
            }
        } else {
            // Hide location share menu
            locationShareButton.setVisibility(View.GONE);
            locationShareToggleButton.setVisibility(View.GONE);
            MenuItem item = navigationView.getMenu().findItem(R.id.chat_share_location);
            item.setVisible(false);
        }

        /* Read new messages after last acked message from server */
        requestUnackedMessages();
        /* Ack to last messages */
        sendAckToLastMessage();
        /* Request nbNotRead of messages read */
        requestNbreadOfMessages(group.getMessageList().getList());

        // Get last message
        PokoMessage lastMessage = group.getMessageList().getLastMessage();

        // Set last message id at first
        if (lastMessage != null) {
            lastMessageIdAtFirst = lastMessage.getMessageId();
        }

        firstRead = true;
    }

    @Override
    protected void onDestroy() {
        // Detach server event listeners
        server.detachActivityCallback(Constants.sessionLoginName, sessionLoginListener);
        server.detachActivityCallback(Constants.sendMessageName, addMessageListener);
        server.detachActivityCallback(Constants.readMessageName, readMessageListener);
        server.detachActivityCallback(Constants.newMessageName, addMessageListener);
        server.detachActivityCallback(Constants.getMemberJoinHistory, refreshMessageListener);
        server.detachActivityCallback(Constants.readNbreadOfMessages, refreshMessageListener);
        server.detachActivityCallback(Constants.messageAckName, messageAckListener);
        server.detachActivityCallback(Constants.ackMessageName, ackMessageListener);
        server.detachActivityCallback(Constants.membersInvitedName, membersInvitedListener);
        server.detachActivityCallback(Constants.membersExitName, memberExitListener);
        server.detachActivityCallback(Constants.exitGroupName, exitGroupListener);

        if (room != null) {
            // Remove listener
            room.removeListener(roomStateChangeListener);
        }

        super.onDestroy();
    }

    /* Overriding methods */
    @Override
    public void onBackPressed() {
        /* if drawer is opened, close drawer */
        if (drawerLayout.isDrawerOpen(Gravity.END)) {
            drawerLayout.closeDrawer(Gravity.END);
        } else if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else {
            closeChatting();
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
        if (!ChatManager.startChat(group)) {
            // Another chat is going on, finish this chat
            finish();
        }
    }

    private void closeChatting() {
        // Set intent for result to MainActivity
        Intent intent = new Intent();
        intent.putExtra("groupId", group.getGroupId());
        setResult(RESULT_OK, intent);

        // End chat
        ChatManager.endChat(group);

        // Finish
        finish();
    }

    private void creationError(String errMsg) {
        Toast.makeText(getApplicationContext(), errMsg,
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    private PokoMessage createSentMessage(int sendId, String content,
                                          int messageType, int importanceLevel) {
        PokoMessage message = new PokoMessage();
        message.setWriter(session.getUser());
        message.setSendId(sendId);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setImportanceLevel(importanceLevel);

        return message;
    }

    /* Button listeners */
    private View.OnClickListener backspaceButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            closeChatting();
        }
    };

    private View.OnClickListener messageSendButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                PokoLock.getDataLockInstance().acquireWriteLock();
                try {
                    String content = messageInputView.getText().toString();
                    if (content.length() == 0) {
                        Toast.makeText(getApplicationContext(), "메시지를 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Increment send id
                    sendId++;

                    // Create message to send
                    PokoMessage message = createSentMessage(sendId, content,
                            PokoMessage.TYPE_TEXT_MESSAGE, PokoMessage.IMPORTANCE_NORMAL);

                    // Send message to server
                    server.sendNewMessage(group.getGroupId(), sendId, content, PokoMessage.IMPORTANCE_NORMAL);

                    // Add sent but not completed message list
                    group.getMessageList().addSentMessage(message);

                    // Clear input text edit
                    messageInputView.setText("");
                } finally {
                    PokoLock.getDataLockInstance().releaseWriteLock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener attachOptionButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int visibility = attachOptionLayout.getVisibility();

            if (visibility == View.VISIBLE) {
                // Hide attach option layout
                attachOptionLayout.setVisibility(View.GONE);

                // Check if the fragment exists
                if (attachOptionFragment != null) {
                    // Remove the fragment
                    getSupportFragmentManager()
                            .beginTransaction()
                            .remove(attachOptionFragment)
                            .commit();

                    attachOptionFragment = null;
                }
            } else if (visibility == View.GONE) {
                // Show attach option layout
                attachOptionLayout.setVisibility(View.VISIBLE);

                // Check if there is no fragment
                if (attachOptionFragment == null) {
                    // Create fragment
                    attachOptionFragment = new ChatAttachOptionFragment();

                    // Add the fragment to attach option layout
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.chatAttachOptionLayout, attachOptionFragment)
                            .commit();
                }
            }
        }
    };

    private View.OnClickListener locationShareClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startLocationShareActivity();
        }
    };

    private SwitchButton.OnCheckedChangeListener locationShareToggleListener =
            new SwitchButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (event != null && room != null) {
                if (isChecked) {
                    // Get my location
                    LocationShare myLocation = room.getMyLocation();

                    if (myLocation != null) {
                        server.sendJoinRealtimeLocationShare(event.getEventId(), myLocation.getNumber());
                    } else {
                        server.sendJoinRealtimeLocationShare(event.getEventId(), null);
                    }
                } else {
                    server.sendExitRealtimeLocationShare(event.getEventId());
                }
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

                        // If server gave maximum number of message request next messages,
                        // we request new messages after last message from server
                        if (readMessages.size() >= Constants.nbMessageRead) {
                            PokoMessage lastMessage = group.getMessageList().getLastMessage();

                            if (lastMessage != null) {
                                // Request new messages after last message from server
                                server.sendReadMessage(group.getGroupId(),
                                        lastMessage.getMessageId() + 1,
                                        Constants.nbMessageRead);
                            }
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
                            server.sendAckMessage(group.getGroupId(), messageId, messageId);
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
            final Group ackedGroup = (Group) getData("group");
            final Integer ackStart = (Integer) getData("ackStart");
            final Integer ackEnd = (Integer) getData("ackEnd");
            final User user = (User) getData("user");
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

    private ActivityCallback ackMessageListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group ackedGroup = (Group) getData("group");
            final Integer ackStart = (Integer) getData("ackStart");
            final Integer ackEnd = (Integer) getData("ackEnd");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ackedGroup == null || ackStart == null || ackEnd == null) {
                        return;
                    }

                    if (ackedGroup == group) {
                        // Send NbRead of messages if acked message ids are in
                        // range of first last message
                        if (lastMessageIdAtFirst != null &&
                                ackStart <= lastMessageIdAtFirst) {
                            int endId = Math.min(ackEnd, lastMessageIdAtFirst);
                            endId = Math.min(endId, group.getAck());

                            server.sendReadNbreadOfMessages(group.getGroupId(), ackStart, endId);
                        }

                        /* Refresh NbNotReadUser number */
                        messageListAdapter.notifyDataSetChanged();
                    }
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

    private LocationShareRoom.RoomStateChangeListener roomStateChangeListener =
            new LocationShareRoom.RoomStateChangeListener() {
                @Override
                public void initialState(boolean joined) {
                    if (joined) {
                        Toast.makeText(getApplicationContext(), R.string.location_share_already_joined,
                                Toast.LENGTH_SHORT).show();

                        if (checkPermissionAndRequestIfNot()) {
                            // Make location share fragment if not exist
                            makeLocationShareFragment(false);

                            if (room != null) {
                                room.startMeasure(getApplicationContext(), ChatActivity.this);
                            }
                        }
                    }

                    locationShareToggleButton.setChecked(joined);
                }

                @Override
                public void onStateChange(boolean joined) {
                    if (joined) {
                        Toast.makeText(getApplicationContext(), R.string.location_share_joined,
                                Toast.LENGTH_SHORT).show();

                        if (checkPermissionAndRequestIfNot()) {
                            // Make location share fragment if not exist
                            makeLocationShareFragment(true);

                            if (room != null) {
                                room.startMeasure(getApplicationContext(), ChatActivity.this);
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.location_share_exited,
                                Toast.LENGTH_SHORT).show();

                        // Close drawer if opened
                        if (drawerLayout.isDrawerOpen(Gravity.START)) {
                            drawerLayout.closeDrawer(Gravity.START);
                        }

                        if (room != null) {
                            room.stopMeasure();
                        }
                    }

                    locationShareToggleButton.setChecked(joined);
                }
            };

    /* View creation callbacks */
    private ViewCreationCallback<PokoMessage> messageViewCreationCallback =
            new ViewCreationCallback<PokoMessage>() {
                @Override
                public void run(View view, PokoMessage item) {
                    // View and message should exist
                    if (view == null || item == null) {
                        return;
                    }

                    // Get message type
                    int messageType = item.getMessageType();

                    switch (messageType) {
                        case PokoMessage.TYPE_IMAGE: {
                            // Add click listener for image message
                            view.setOnClickListener(
                                    new ImageMessageClickListener(context, item));
                            break;
                        }
                        case PokoMessage.TYPE_FILE_SHARE: {
                            // Add click listener for file share message
                            view.setOnClickListener(
                                    new FileShareMessageClickListener(context, item));
                            break;
                        }
                    }

                    switch (messageType) {
                        case PokoMessage.TYPE_IMAGE:
                        case PokoMessage.TYPE_TEXT_MESSAGE:
                        case PokoMessage.TYPE_FILE_SHARE:
                        case PokoMessage.TYPE_MEMBER_EXIT:
                        case PokoMessage.TYPE_MEMBER_JOIN: {
                            // Add long click listener
                            view.setOnLongClickListener(
                                    new MessageLongClickListener(ChatActivity.this, item));

                            break;
                        }
                    }
                }
            };

    // Message long click listener
    static class MessageLongClickListener implements View.OnLongClickListener {
        private FragmentActivity activity;
        private PokoMessage message;

        public MessageLongClickListener(FragmentActivity activity, PokoMessage message) {
            this.activity = activity;
            this.message = message;
        }

        @Override
        public boolean onLongClick(View v) {
            // Show menu
            ChatMessageOptionDialog dialog = new ChatMessageOptionDialog();
            dialog.setMessage(message);
            dialog.show(activity.getSupportFragmentManager(), "Message menu");

            return true;
        }
    }

    // Image message click listener
    static class ImageMessageClickListener implements View.OnClickListener {
        private Context context;
        private PokoMessage message;

        public ImageMessageClickListener(Context context, PokoMessage message) {
            this.context = context;
            this.message = message;
        }

        @Override
        public void onClick(View v) {
            // Message should exist and be image message.
            if (message == null || message.getMessageType() != PokoMessage.TYPE_IMAGE) {
                Toast.makeText(context, R.string.chat_not_image_message, Toast.LENGTH_SHORT).show();
                return;
            }

            // Start image show activity
            Intent intent = new Intent(context, ChatImageShowActivity.class);
            intent.putExtra("contentName", message.getContent());
            context.startActivity(intent);
        }
    }

    // File share message click listener
    static class FileShareMessageClickListener implements View.OnClickListener {
        private Context context;
        private PokoMessage message;
        private ChatActivity activity;

        public FileShareMessageClickListener(Context context, PokoMessage message) {
            this.context = context;
            this.message = message;
        }

        @Override
        public void onClick(View v) {
            // Message should exist and be image message.
            if (message == null || message.getMessageType() != PokoMessage.TYPE_FILE_SHARE) {
                Toast.makeText(context, R.string.chat_not_file_message, Toast.LENGTH_SHORT).show();
                return;
            }

            // Get content
            String content = message.getContent();

            if (content == null) {
                return;
            }

            try {
                // Get json content
                JSONObject jsonObject = new JSONObject(content);

                // Get content name and file name
                final String contentName = jsonObject.getString("contentName");
                final String fileName = jsonObject.getString("fileName");

                String fileUriPath;
                if (jsonObject.has("fileUri")) {
                    fileUriPath = jsonObject.getString("fileUri");
                } else {
                    fileUriPath = null;
                }

                if (contentName == null || fileName == null) {
                    return;
                }

                // Get file extension
                final String extension = ContentManager.getExtension(fileName);

                // Get MIME file type
                final String MIMEType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                // Check if file uri exists
                if (fileUriPath != null) {
                    // This case is when the file is uploaded by user,
                    // We know its location so we do not locate it

                    // Parse content uri
                    Uri contentUri = Uri.parse(fileUriPath);

                    // View file
                    viewFileShareContent(contentUri, MIMEType);

                    return;
                }

                // This case is when the file is uploaded by other user,
                // including other sessions of same user
                Toast.makeText(context, content, Toast.LENGTH_SHORT).show();

                // Locate file
                ContentManager.getInstance().locateBinary(context, contentName, fileName,
                        new ContentManager.BinaryContentLoadCallback() {
                            @Override
                            public void onError() {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Show fail toast message
                                        Toast.makeText(context, R.string.chat_file_load_fail,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onLoadBinary(Uri uri) {
                                // Get content string
                                String content = message.getContent();

                                if (content == null) {
                                    return;
                                }

                                // Make uri
                                Uri binaryUri = FileProvider.getUriForFile(
                                        context,
                                        context.getApplicationContext()
                                                .getPackageName() + ".provider", new File(uri.getPath()));

                                // View file
                                viewFileShareContent(binaryUri, MIMEType);
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void viewFileShareContent(Uri uri, String mimeType) {
            // Make intent to start file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                // Execute binary file
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();

                // Show message
                Toast.makeText(context,
                        R.string.chat_file_share_message_activity_not_found,
                        Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                e.printStackTrace();

                // Show message
                Toast.makeText(context,
                        R.string.chat_file_share_security_error,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* Popup menu code */
    private void open_menu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.chat_menu);
        popup.show();
    }

    private void open_exit_warning() {
        GroupExitWarningDialog warningDialog = new GroupExitWarningDialog();
        warningDialog.setGroup(group);
        warningDialog.show(getSupportFragmentManager(), "채팅방 나가기 경고");
    }

    @Override
    public void onImageAttachedToMessage(final Bitmap bitmap) {
        // Encode to jpeg format
        final byte[] binary = ImageEncoder.encodeToJPEG(bitmap);

        // Increment send id
        sendId++;

        // Create message to send
        final PokoMessage message = createSentMessage(sendId, null,
                PokoMessage.TYPE_IMAGE, PokoMessage.IMPORTANCE_NORMAL);

        // Send image message
        int imageId = ContentTransferManager.getInstance().addUploadJob(binary,
                ContentManager.EXT_JPG,
                new ContentTransferManager.UploadJobCallback() {
                    @Override
                    public void onSuccess(String contentName) {
                        // Put content in cache
                        ContentManager.getInstance().putImageContentInCache(contentName, bitmap);

                        // Put image binary data to store as file
                        ContentService.putImageBinary(contentName, binary);

                        // Start service to save content as a file
                        Intent intent = new Intent(getApplicationContext(), ContentService.class);
                        intent.putExtra("command", ContentService.CMD_STORE_CONTENT);
                        intent.putExtra("contentName", contentName);
                        intent.putExtra("contentType", ContentTransferManager.TYPE_IMAGE);
                        startService(intent);

                        // Save content name
                        message.setContent(contentName);
                    }

                    @Override
                    public void onError() {
                        // Remove message

                    }
                });

        Log.v("POKO", "SEND IMAGE MESSAGE upload send id " + imageId + ", send id " + sendId);
        // Send image message
        server.sendNewImageMessage(group.getGroupId(), sendId, imageId, PokoMessage.IMPORTANCE_NORMAL);

        // Add sent but not completed message list
        group.getMessageList().addSentMessage(message);
    }

    @Override
    public void onBinaryAttachedToMessage(final Uri fileUri,
                                          final String fileName,
                                          final ContentStream contentStream) {
        // Get file size
        int fileSize = contentStream.getSize();

        // Check if the file size is too big
        if (fileSize > Constants.uploadFileSizeLimit) {
            // Show toast message and return
            Toast.makeText(this, R.string.chat_file_share_message_size_too_big,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Increment send id
        sendId++;

        // Create message to send
        final PokoMessage message = createSentMessage(sendId, null,
                PokoMessage.TYPE_FILE_SHARE, PokoMessage.IMPORTANCE_NORMAL);

        // Send file share message
        int fileId = ContentTransferManager.getInstance().addUploadJob(contentStream,
                "binary",
                new ContentTransferManager.UploadJobCallback() {
                    @Override
                    public void onSuccess(String contentName) {
                        try {
                            // Make json content
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("fileUri", fileUri.toString());
                            jsonObject.put("fileName", fileName);
                            jsonObject.put("contentName", contentName);

                            // Stringify json data and set content
                            message.setContent(jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError() {
                        // Remove message
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatActivity.this,
                                        "Sorry, failed to upload file...",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

        Log.v("POKO", "SEND BINARY MESSAGE upload send id " + fileId + ", send id " + sendId);

        // Send binary message
        server.sendNewFileShareMessage(group.getGroupId(), sendId, fileId,
                PokoMessage.IMPORTANCE_NORMAL, fileName);

        // Add sent but not completed message list
        group.getMessageList().addSentMessage(message);
    }

    // Message long click option selected
    @Override
    public void onMessageOptionSelected(PokoMessage message, int option) {
        switch (option) {
            case ChatMessageOptionDialog.OPTION_COPY: {
                String copyContent = null;

                switch (message.getMessageType()) {
                    case PokoMessage.TYPE_TEXT_MESSAGE: {
                        // Copy text message
                        copyContent = message.getContent();
                        break;
                    }

                    case PokoMessage.TYPE_MEMBER_JOIN:
                    case PokoMessage.TYPE_MEMBER_EXIT: {
                        // Copy special content
                        copyContent = message.getSpecialContent();
                        break;
                    }

                    case PokoMessage.TYPE_FILE_SHARE: {
                        try {
                            JSONObject jsonObject = new JSONObject(message.getContent());

                            // Copy file name
                            copyContent = jsonObject.getString("fileName");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    case PokoMessage.TYPE_IMAGE: {
                        try {
                            JSONObject jsonObject = new JSONObject(message.getContent());

                            // Copy contet name
                            // TODO: Copy image binary content
                            copyContent = jsonObject.getString("contentName");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    default: {
                        // Message type is unknown
                        copyContent = "Unknown";

                        break;
                    }
                }

                // Copy copy content
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("PokoTalk", copyContent);
                clipboard.setPrimaryClip(clip);

                // Show toast message
                Toast.makeText(getApplicationContext(),
                        R.string.chat_message_copy, Toast.LENGTH_SHORT).show();
            }
            default: {

            }
        }
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
            case R.id.invite_contacts: {
                Intent intent = new Intent(this, GroupMemberInvitationActivity.class);
                intent.putExtra("groupId", group.getGroupId());
                startActivity(intent);
                break;
            }
            case R.id.exit_group: {
                open_exit_warning();
                break;
            }
            case R.id.chat_share_location: {
                startLocationShareActivity();
                if (drawerLayout.isDrawerOpen(Gravity.END)) {
                    drawerLayout.closeDrawer(Gravity.END);
                }
                break;
            }
            default:
                Toast.makeText(this, "문제가 발생했습니다.",
                        Toast.LENGTH_SHORT).show();
                return false;
        }

        return true;
    }

    private boolean checkPermissionAndRequestIfNot() {
        // Check for permissions
        boolean locationFinePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        boolean locationCoarsePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!locationFinePermission || !locationCoarsePermission) {
            // Request for permission
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSION);

            return false;
        } else {
            return true;
        }
    }

    // Show location share fragment
    private void makeLocationShareFragment(final boolean show) {
        if (event == null || locationShareLayout == null) {
            return;
        }

        if (locationShareFragment != null) {
            // Show location share
            if (!drawerLayout.isDrawerOpen(Gravity.START)) {
                drawerLayout.openDrawer(Gravity.START);
            }
            return;
        }

        // Create container layout
        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams
                (DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.START;
        locationShareLayout.setLayoutParams(params);

        // Disable closing drawer by dragging
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                locationShareLayout);

        // Create fragment
        locationShareFragment = new LocationShareFragment();
        locationShareFragment.setEventId(event.getEventId());

        // Add fragment to container
        getSupportFragmentManager().beginTransaction()
                .add(R.id.chatLocationShareLayout, locationShareFragment)
                .commit();

        // Make layout visible
        locationShareLayout.setVisibility(View.VISIBLE);

        // Set layout visible asynchronously,
        // we sleep 0.25 second to give time to drawer is settled down
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Show location share
                if (show && !drawerLayout.isDrawerOpen(Gravity.START)) {
                    drawerLayout.openDrawer(Gravity.START);
                }
            }
        }, 250);
    }

    /* Group exit dialog listener */
    @Override
    public void groupExitOptionApply(Group group, int option) {
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
                server.sendAckMessage(group.getGroupId(), ack + 1, lastMessageId);
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

        // We do not request nbRead of unacked messages
        // because it may cause ack number error
        if (maxId > group.getAck()) {
            maxId = group.getAck();
        }

        // Max id should be greater than min id
        if (maxId < minId) {
            return;
        }

        server.sendReadNbreadOfMessages(group.getGroupId(), minId, maxId);
    }

    private void startLocationShareActivity() {
        if (event != null) {
            if (locationShareFragment != null) {
                drawerLayout.openDrawer(Gravity.START);
            } else if (checkPermissionAndRequestIfNot()) {
                // Make location share fragment if not exist
                makeLocationShareFragment(true);

                if (room != null) {
                    room.startMeasure(getApplicationContext(), ChatActivity.this);
                }
            }
        }
    }

    class ReadMoreMessageTask extends AsyncTask<Object, Void, ArrayList<PokoMessage>> {

        @Override
        protected ArrayList<PokoMessage> doInBackground(Object... args) {
            ArrayList<PokoMessage> readMessages = new ArrayList<>();

            try {
                PokoLock.getDataLockInstance().acquireWriteLock();

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

                    // Get user
                    Contact user = Session.getInstance().getUser();

                    if (user == null) {
                        return null;
                    }

                    // Get a database for reading
                    PokoUserDatabase pokoDatabase = PokoUserDatabase.getInstance(
                            getApplicationContext(), user.getUserId());
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
                                // Parse message
                                PokoMessage message = Parser.parseMessage(cursor);

                                // Add message only if it is new message
                                if (messageList.updateItem(message) == message) {
                                    readMessages.add(message);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } finally {
                        cursor.close();

                        db.releaseReference();
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    // We must scroll to mark after mark when error
                    messageListView.scrollToMark(0);
                } finally {
                    PokoLock.getDataLockInstance().releaseWriteLock();
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
                    PokoLock.getDataLockInstance().acquireWriteLock();
                    try {
                        MessageListUI messageListUI = (MessageListUI) messageListAdapter.getPokoList();
                        // We now count delta of number of date change message.
                        messageListUI.startCountDateChangeMessage();

                        // Update all read messages
                        for (PokoMessage message : readMessages) {
                            messageListUI.updateItem(message);
                        }

                        // Find how many date change messages created or removed
                        int dateChangeMessageNumDelta = messageListUI.endCountDateChangeMessage();

                        // Notify message list has been changed
                        messageListAdapter.notifyDataSetChanged();

                        // Compute changed message number
                        int size = readMessages.size() + dateChangeMessageNumDelta;

                        // Scroll to original position
                        messageListView.scrollToMark(size);

                    } finally {
                        PokoLock.getDataLockInstance().releaseWriteLock();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Request nbNotRead of messages read
                requestNbreadOfMessages(readMessages);
            }

            // Check if it is a first read
            if (ChatActivity.this.firstRead) {
                // Scroll to bottom
                messageListView.postScrollToBottom();

                // Not first read anymore
                ChatActivity.this.firstRead = false;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        boolean locationFinePermission = false;
        boolean locationCoarsePermission = false;

        if (requestCode == Constants.LOCATION_PERMISSION) {
            // Check permission granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        continue;
                    }

                    switch (permissions[i]) {
                        case Manifest.permission.ACCESS_FINE_LOCATION: {
                            locationFinePermission = true;
                            break;
                        }
                        case Manifest.permission.ACCESS_COARSE_LOCATION: {
                            locationCoarsePermission = true;
                            break;
                        }
                    }
                }

                if (locationFinePermission) {
                    Log.v("POKO", "FINE PERMISSSION");
                }

                if (locationCoarsePermission) {
                    Log.v("POKO", "COARSE PERMISSSION");
                }
                if (locationFinePermission && locationCoarsePermission) {
                    Log.v("POKO", "PERMISSION GRANTED, sTART MEASURE");
                    // Make location share fragment and show
                    makeLocationShareFragment(true);

                    // Start measure
                    room.startMeasure(getApplicationContext(), this);
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
