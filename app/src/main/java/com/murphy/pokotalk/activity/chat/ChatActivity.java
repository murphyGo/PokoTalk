package com.murphy.pokotalk.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.MessageListAdapter;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.Message;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

public class ChatActivity extends AppCompatActivity
        implements PopupMenu.OnMenuItemClickListener {
    private int groupId;
    private PokoServer server;
    private TextView groupNameView;
    private Button backspaceButton;
    private ImageView menuButton;
    private ListView messageListView;
    private EditText messageInputView;
    private Button sendMessageButton;
    private MessageListAdapter messageListAdapter;
    private Group group;
    private int sendId;
    private Session session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        sendId = 0;

        groupNameView = findViewById(R.id.groupName);
        backspaceButton = findViewById(R.id.backspaceButton);
        menuButton = findViewById(R.id.menuButton);
        messageListView =  findViewById(R.id.messageList);
        messageInputView = findViewById(R.id.messageInputText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        Intent intent = getIntent();
        if (getIntent() == null) {
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

        messageListAdapter = new MessageListAdapter(this, group.getMessageList().getList());
        messageListView.setAdapter(messageListAdapter);

        server = PokoServer.getInstance(this);
        server.sendReadMessage(group.getGroupId(), Constants.nbMessageRead);
        session = Session.getInstance();

        /* Add widget listeners */
        backspaceButton.setOnClickListener(backspaceButtonClickListener);
        menuButton.setOnClickListener(menuButtonClickListener);
        sendMessageButton.setOnClickListener(messageSendButtonListener);

        /* Attach server event callbacks */
        server.attachActivityCallback(Constants.sendMessageName, messageListChangedListener);
        server.attachActivityCallback(Constants.readMessageName, messageListChangedListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        server.detachActivityCallback(Constants.sendMessageName, messageListChangedListener);
        server.detachActivityCallback(Constants.readMessageName, messageListChangedListener);
    }

    private void creationError(String errMsg) {
        Toast.makeText(getApplicationContext(), errMsg,
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
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
            finish();
        }
    };

    private View.OnClickListener menuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            open_menu(v);
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
            server.sendNewMessage(group.getGroupId(), ++sendId, content, null);
            Message message = createSentMessage(sendId, content, null);
            group.getMessageList().addSentMessage(message);
        }
    };

    /* Server event listeners */
    private ActivityCallback messageListChangedListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            messageListAdapter.refreshAllExistingViews();
            messageListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback messageAckListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {

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

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.user_list:
                break;
            case R.id.invite_contacts:
                break;
            case R.id.exit_group:
                break;
            default:
                Toast.makeText(this, "문제가 발생했습니다.",
                        Toast.LENGTH_SHORT).show();
        }

        return true;
    }
}
