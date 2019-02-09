package com.murphy.pokotalk.activity.chat;

import android.content.Intent;
import android.content.res.Configuration;
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
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.Message;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import java.util.ArrayList;
import java.util.Collections;

public class ChatActivity extends AppCompatActivity
        implements PopupMenu.OnMenuItemClickListener,
        GroupExitWarningDialog.Listener,
        NavigationView.OnNavigationItemSelectedListener {
    private int groupId;
    private PokoServer server;
    private DataCollection collection;
    private TextView groupNameView;
    private Button backspaceButton;
    private ListView messageListView;
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

    public static final int slideMenuWidthDP = 250;

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

        messageListAdapter = new MessageListAdapter(this, group.getMessageList().getList());
        messageListView.setAdapter(messageListAdapter);

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

        /* Member list */
        memberListAdapter = new GroupMemberListAdapter(this, group.getMembers().getList());
        memberListView = drawerLayout.findViewById(R.id.memberList);
        memberListView.setAdapter(memberListAdapter);

        /* Add widget listeners */
        backspaceButton.setOnClickListener(backspaceButtonClickListener);
        sendMessageButton.setOnClickListener(messageSendButtonListener);

        /* Attach server event callbacks */
        server.attachActivityCallback(Constants.sendMessageName, sendMessageListener);
        server.attachActivityCallback(Constants.readMessageName, readMessageListener);
        server.attachActivityCallback(Constants.newMessageName, newMessageListener);
        server.attachActivityCallback(Constants.messageAckName, messageAckListener);
        server.attachActivityCallback(Constants.membersInvitedName, membersInvitedListener);
        server.attachActivityCallback(Constants.membersExitName, memberExitListener);
        server.attachActivityCallback(Constants.exitGroupName, exitGroupListener);

        /* Read messages */
        server.sendReadMessage(group.getGroupId(), Constants.nbMessageRead);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        server.detachActivityCallback(Constants.sendMessageName, sendMessageListener);
        server.detachActivityCallback(Constants.readMessageName, readMessageListener);
        server.detachActivityCallback(Constants.newMessageName, newMessageListener);
        server.detachActivityCallback(Constants.messageAckName, messageAckListener);
        server.detachActivityCallback(Constants.membersInvitedName, membersInvitedListener);
        server.detachActivityCallback(Constants.membersExitName, memberExitListener);
        server.detachActivityCallback(Constants.exitGroupName, exitGroupListener);
    }

    @Override
    public void onBackPressed() {
        closeChatting();
        super.onBackPressed();
    }

    private void openChatting() {
        try {
            collection.acquireGroupSemaphore();
            collection.startChat(group);
            collection.releaseGroupSemaphore();
        } catch (InterruptedException e) {
            Toast.makeText(this, "채팅 실행 중 문제가 발생했습니다.",
                    Toast.LENGTH_SHORT);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void closeChatting() {
        Intent intent = new Intent();
        intent.putExtra("groupId", group.getGroupId());
        setResult(RESULT_OK, intent);

        try {
            collection.acquireGroupSemaphore();
            collection.endChat();
            collection.releaseGroupSemaphore();
        } catch (InterruptedException e) {
            Toast.makeText(this, "채팅 종료 중 문제가 발생했습니다.",
                    Toast.LENGTH_SHORT);
        }
    }

    private void ackUnackedMessages() {
        /* If unacked message exists, try ack */
        MessageList messageList = group.getMessageList();
        ArrayList<Message> unackedMessages = messageList.getUnackedMessages();

        if (unackedMessages.size() > 0) {
            ArrayList<Integer> messageIds = new ArrayList<>();
            for (Message message : unackedMessages) {
                messageIds.add(message.getMessageId());
                Log.v("UNACKED", Integer.toString(message.getMessageId()));
            }

            int fromId = Collections.min(messageIds), toId = Collections.max(messageIds);
            server.sendAckMessage(group.getGroupId(), fromId, toId);
        }
    }

    private void creationError(String errMsg) {
        Toast.makeText(getApplicationContext(), errMsg,
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (slideMenuToggle != null)
            slideMenuToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (slideMenuToggle != null)
            slideMenuToggle.onConfigurationChanged(newConfig);
    }

    private Message createSentMessage(int sendId, String content, @Nullable Integer importanceLevel) {
        Message message = new Message();
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
            closeChatting();
            finish();
        }
    };

    private View.OnClickListener messageSendButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String content = messageInputView.getText().toString();
            if (content.length() == 0) {
                Toast.makeText(getApplicationContext(), "메시지를 입력해주세요.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            sendId++;
            Message message = createSentMessage(sendId, content, Message.NORMAL);
            server.sendNewMessage(group.getGroupId(), sendId, content, Message.NORMAL);
            group.getMessageList().addSentMessage(message);
        }
    };

    /* Server event listeners */
    private ActivityCallback sendMessageListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
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

    /* Server event listeners */
    private ActivityCallback readMessageListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            /* Refresh message list */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ackUnackedMessages();
                    messageListAdapter.refreshAllExistingViews();
                    messageListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    /* Server event listeners */
    private ActivityCallback newMessageListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            /* Ack new message */
            Message newMessage = (Message) getData("message");
            int messageId = newMessage.getMessageId();
            if (!newMessage.isAcked()) {
                server.sendAckMessage(group.getGroupId(), messageId, messageId);
            }

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

    private ActivityCallback messageAckListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final int fromId = (Integer) getData("fromId"), toId = (Integer) getData("toId");
            Log.v("MESSAGE ACK", fromId + " to " + toId);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /* Refresh NbNotReadUser number */
                    messageListAdapter.refreshViewsNbNotReadUser(fromId, toId);
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    memberListAdapter.refreshAllExistingViews();
                    memberListAdapter.notifyDataSetChanged();
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    memberListAdapter.refreshAllExistingViews();
                    memberListAdapter.notifyDataSetChanged();
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
}
