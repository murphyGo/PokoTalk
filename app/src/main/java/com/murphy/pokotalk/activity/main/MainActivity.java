package com.murphy.pokotalk.activity.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.Constants.RequestCode;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.activity.chat.ChatActivity;
import com.murphy.pokotalk.activity.chat.GroupAddActivity;
import com.murphy.pokotalk.activity.chat.GroupExitWarningDialog;
import com.murphy.pokotalk.activity.chat.GroupOptionDialog;
import com.murphy.pokotalk.activity.contact.ContactDetailDialog;
import com.murphy.pokotalk.activity.contact.ContactOptionDialog;
import com.murphy.pokotalk.activity.contact.PendingContactActivity;
import com.murphy.pokotalk.activity.event.EventCreationActivity;
import com.murphy.pokotalk.adapter.ContactListAdapter;
import com.murphy.pokotalk.adapter.GroupListAdapter;
import com.murphy.pokotalk.adapter.MpagerAdapter;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.GroupListUI;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.service.PokoTalkService;
import com.murphy.pokotalk.view.ContactItem;
import com.murphy.pokotalk.view.GroupItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        ContactDetailDialog.ContactDetailDialogListener,
        ContactOptionDialog.ContactOptionDialogListener,
        GroupOptionDialog.GroupOptionDialogListener,
        GroupExitWarningDialog.Listener,
        ServiceConnection {
    private PokoServer server;
    private Session session;
    private DataCollection collection;
    private ViewPager viewPager;
    private BottomNavigationView navigationMenu;
    private int[] layouts = {R.layout.contact_list_layout, R.layout.group_list_layout,
            R.layout.event_list_layout};
    private MpagerAdapter pagerAdapter;
    private ContactListAdapter contactListAdapter;
    private GroupListAdapter groupListAdapter;
    private Messenger serviceMessenger = null;
    private Messenger myMessenger = new Messenger(new ServiceCallback());

    /* Intent commands */
    public static final int START_GROUP_CHAT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Make top status bar transparent
        if (Build.VERSION.SDK_INT > 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } */

        /* Start view pager(contact, group, event, configuration menu) */
        collection = DataCollection.getInstance();

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new MpagerAdapter(this, layouts);
        viewPager.setAdapter(pagerAdapter);

        navigationMenu = (BottomNavigationView) findViewById(R.id.mainNavigation);
        navigationMenu.setOnNavigationItemSelectedListener(this);

        /* Get server */
        server = PokoServer.getInstance(this);

        /* If application has no session id to login, show login activity */
        session = Session.getInstance();
        if (!session.sessionIdExists()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, RequestCode.LOGIN.value);
        }

        /* Create adapters */
        try {
            DataLock.getInstance().acquireWriteLock();

            try {
                ContactList contactList = collection.getContactList();
                contactListAdapter = new ContactListAdapter(getApplicationContext());
                contactListAdapter.setViewCreationCallback(contactCreationCallback);
                ContactList contactListUI = (ContactList) contactListAdapter.getPokoList();
                contactListUI.copyFromPokoList(contactList);
            } finally {
                DataLock.getInstance().releaseWriteLock();

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            DataLock.getInstance().acquireWriteLock();

            try {
                GroupList groupList = collection.getGroupList();
                groupListAdapter = new GroupListAdapter(getApplicationContext());
                groupListAdapter.setViewCreationCallback(groupCreationCallback);
                GroupListUI groupListUI = (GroupListUI) groupListAdapter.getPokoList();
                groupListUI.copyFromPokoList(groupList);
            } finally {
                DataLock.getInstance().releaseWriteLock();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Attach view pager callbacks */
        pagerAdapter.enrollItemCallback(R.layout.contact_list_layout, contactListCreationCallback);
        pagerAdapter.enrollItemCallback(R.layout.group_list_layout, groupListCreationCallback);
        pagerAdapter.enrollItemCallback(R.layout.event_list_layout, eventListCreationCallback);

        /* Attach event callbacks */
        server.attachActivityCallback(Constants.sessionLoginName, sessionLoginCallback);
        server.attachActivityCallback(Constants.getContactListName, getContactListCallback);
        server.attachActivityCallback(Constants.getPendingContactListName, getContactListCallback);
        server.attachActivityCallback(Constants.newContactName, addContactCallback);
        server.attachActivityCallback(Constants.newPendingContactName, removeContactCallback);
        server.attachActivityCallback(Constants.contactDeniedName, removeContactCallback);
        server.attachActivityCallback(Constants.contactRemovedName, removeContactCallback);
        server.attachActivityCallback(Constants.joinContactChatName, joinContactChatCallback);
        server.attachActivityCallback(Constants.getGroupListName, getGroupListCallback);
        server.attachActivityCallback(Constants.addGroupName, addGroupCallback);
        server.attachActivityCallback(Constants.exitGroupName, removeGroupCallback);
        server.attachActivityCallback(Constants.readMessageName, readMessageCallback);
        server.attachActivityCallback(Constants.sendMessageName, newMessageCallback);
        server.attachActivityCallback(Constants.newMessageName, newMessageCallback);
        server.attachActivityCallback(Constants.getMemberJoinHistory, newMessageCallback);
        Log.v("POKO", "MainActivity starts, process id " + Process.myPid());
        Log.v("POKO", "POKO ON CREATE");

        /* Bind to service */
        Context context = getApplicationContext();
        PokoTalkService.startPokoTalkService(context);
        PokoTalkService.bindPokoTalkService(context, this);

        /* Get intent and start operation if given */
        Intent intent = getIntent();
        int opcode = intent.getIntExtra("opcode", -1);
        if (opcode >= 0) {
            startOperation(opcode, intent);
        }
    }

    @Override
    protected void onStop() {
        Log.v("POKO", "POKO ON STOP");
        super.onStop();
    }

    @Override
    protected void onStart() {
        Log.v("POKO", "POKO ON START");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.v("POKO", "POKO ON RESUME");
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v("POKO", "POKO ON PAUSE");
    }

    @Override
    protected void onDestroy() {
        Log.v("POKO", "POKO ON DESTROY");

        /* Unbind from service */
        PokoTalkService.unbindPokoTalkService(getApplicationContext(), this);

        server.detachActivityCallback(Constants.sessionLoginName, sessionLoginCallback);
        server.detachActivityCallback(Constants.getContactListName, getContactListCallback);
        server.detachActivityCallback(Constants.getPendingContactListName, getContactListCallback);
        server.detachActivityCallback(Constants.newContactName, addContactCallback);
        server.detachActivityCallback(Constants.newPendingContactName, removeContactCallback);
        server.detachActivityCallback(Constants.contactDeniedName, removeContactCallback);
        server.detachActivityCallback(Constants.contactRemovedName, removeContactCallback);
        server.detachActivityCallback(Constants.joinContactChatName, joinContactChatCallback);
        server.detachActivityCallback(Constants.getGroupListName, getGroupListCallback);
        server.detachActivityCallback(Constants.addGroupName, addGroupCallback);
        server.detachActivityCallback(Constants.exitGroupName, removeGroupCallback);
        server.detachActivityCallback(Constants.readMessageName, readMessageCallback);
        server.detachActivityCallback(Constants.sendMessageName, newMessageCallback);
        server.detachActivityCallback(Constants.newMessageName, newMessageCallback);
        server.detachActivityCallback(Constants.getMemberJoinHistory, newMessageCallback);

        super.onDestroy();
    }

    /* Start operation given to MainActivity */
    public void startOperation(int opcode, Intent intent) {
        switch (opcode) {
            case START_GROUP_CHAT: {
                int groupId = intent.getIntExtra("groupId", -1);
                if (groupId < 0) {
                    return;
                }
                Log.v("POKO", "NEW MESSAGE GROUP ID3 " + groupId);
                Group group = collection.getGroupList().getItemByKey(groupId);
                if (group != null) {
                    startGroupChat(group);
                }
                return;
            }
            default: {
                return;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.navigation_contact:
                viewPager.setCurrentItem(0, true);
                break;
            case R.id.navigation_group:
                viewPager.setCurrentItem(1, true);
                break;
            case R.id.navigation_event:
                viewPager.setCurrentItem(2, true);
                break;
        }
        return false;
    }

    /* View pager callbacks */
    private ViewCreationCallback contactListCreationCallback = new ViewCreationCallback<Contact>() {
        @Override
        public void run(View view, Contact contact) {
            /* Contact list view settings */
            /* Create contact list adapter */
            ListView contactListLayout = view.findViewById(R.id.contactList);
            contactListLayout.setAdapter(contactListAdapter);

            /* Add button listeners */
            Button contactAddButton = view.findViewById(R.id.contactAddButton);
            contactAddButton.setOnClickListener(contactAddButtonClickListener);
        }
    };

    private ViewCreationCallback groupListCreationCallback = new ViewCreationCallback<Group>() {
        @Override
        public void run(View view, Group group) {
            /* Group list view settings */
            /* Create group list adapter */
            ListView groupListLayout = view.findViewById(R.id.groupList);
            groupListLayout.setAdapter(groupListAdapter);

            /* Add button listeners */
            Button groupAddButton = view.findViewById(R.id.groupAddButton);
            groupAddButton.setOnClickListener(groupAddButtonClickListener);
        }
    };

    private ViewCreationCallback eventListCreationCallback = new ViewCreationCallback<PokoEvent>() {
        @Override
        public void run(View view, PokoEvent event) {
            /* PokoEvent list view settings */
            /* Create event list adapter */
            ListView eventListLayout = view.findViewById(R.id.eventList);
            //eventListLayout.setAdapter(groupListAdapter);

            /* Add button listeners */
            Button eventAddButton = view.findViewById(R.id.eventAddButton);
            eventAddButton.setOnClickListener(eventAddButtonClickListener);
        }
    };

    private ViewCreationCallback contactCreationCallback = new ViewCreationCallback<Contact>() {
        @Override
        public void run(View view, Contact c) {
            ContactItem contactView = (ContactItem) view;
            final Contact contact = contactView.getContact();

            contactView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openContactDetailDialog(contact);
                }
            });
            contactView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    openContactOptionDialog(contact);
                    return true;
                }
            });
        }
    };

    private ViewCreationCallback groupCreationCallback = new ViewCreationCallback<Group>() {
        @Override
        public void run(View view, Group g) {
            GroupItem groupView = (GroupItem) view;
            final Group group = groupView.getGroup();

            groupView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startGroupChat(group);
                }
            });
            groupView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    openGroupOptionDialog(group);
                    return true;
                }
            });
        }
    };

    /* User touch event listeners */
    private View.OnClickListener contactAddButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), PendingContactActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener groupAddButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), GroupAddActivity.class);
            startActivityForResult(intent, RequestCode.GROUP_ADD.value);
        }
    };

    private View.OnClickListener eventAddButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), EventCreationActivity.class);
            startActivityForResult(intent, RequestCode.EVENT_CREATE.value);
        }
    };

    /* On activity result callbacks */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.LOGIN.value)
            handleLoginResult(resultCode, data);
        else if (requestCode == RequestCode.GROUP_ADD.value)
            handleGroupAddResult(resultCode, data);
        else if (requestCode == RequestCode.GROUP_CHAT.value)
            handleGroupChatResult(resultCode, data);
    }

    private void handleLoginResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

        } else if (resultCode == RESULT_CANCELED) {
            finish();
        } else {
            finish();
        }
    }

    private void handleGroupAddResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String name = data.getStringExtra("groupName");
            ArrayList<String> emails = data.getStringArrayListExtra("emails");
            server.sendAddGroup(name, emails);
        }
    }

    private void handleGroupChatResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            /* Refresh group item the user chatted */
            if (data == null)
                return;
            int groupId = data.getIntExtra("groupId", -1);
            if (groupId < 0)
                return;
            Group group = collection.getGroupList().getItemByKey(groupId);
            if (group == null)
                return;

            if (groupListAdapter != null) {
                groupListAdapter.notifyDataSetChanged();
            }
        }
    }

    /* Server message callbacks */
    private ActivityCallback sessionLoginCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {

        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback getContactListCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /* Refresh contact list */
                    if (contactListAdapter != null) {
                        ContactList contactList = collection.getContactList();
                        ContactList adapterList = (ContactList) contactListAdapter.getPokoList();
                        adapterList.copyFromPokoList(contactList);
                        contactListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback addContactCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Contact contact = (Contact) getData("contact");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /* Refresh contact list */
                    if (contactListAdapter != null) {
                        if (contact != null) {
                            ContactList contactList = (ContactList) contactListAdapter.getPokoList();
                            contactList.updateItem(contact);
                            contactListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback removeContactCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Integer userId = (Integer) getData("userId");
            final PendingContact pendingContact =
                    (PendingContact) getData("pendingContact");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /* Refresh contact list */
                    if (contactListAdapter != null) {
                        if (userId != null) {
                            ContactList contactList = (ContactList) contactListAdapter.getPokoList();
                            contactList.removeItemByKey(userId);
                        }
                        if (pendingContact != null) {
                            ContactList contactList = (ContactList) contactListAdapter.getPokoList();
                            contactList.removeItemByKey(pendingContact.getUserId());
                        }

                        contactListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback joinContactChatCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, final Object... args) {
            final Contact contact = (Contact) getData("contact");
            final Group group = (Group) getData("group");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (group != null) {
                        if (groupListAdapter != null) {
                            GroupListUI groupListUI = (GroupListUI) groupListAdapter.getPokoList();
                            groupListUI.updateItem(group);
                            groupListAdapter.notifyDataSetChanged();
                        }
                        /* Start group chat with contact */
                        if (contact != null) {
                            startGroupChat(group);
                        }
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback getGroupListCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null) {
                        GroupList groupList = collection.getGroupList();
                        GroupListUI groupListUI = (GroupListUI) groupListAdapter.getPokoList();
                        groupListUI.copyFromPokoList(groupList);
                        groupListUI.addEveryContactChatGroupThatHasMessage();
                        groupListUI.sortItemsByKey();
                        groupListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback addGroupCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group group = (Group) getData("group");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null) {
                        if (group != null) {
                            GroupListUI groupListUI = (GroupListUI) groupListAdapter.getPokoList();
                            groupListUI.updateItem(group);
                            groupListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback removeGroupCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Integer groupId = (Integer) getData("groupId");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null) {
                        GroupListUI groupListUI = (GroupListUI) groupListAdapter.getPokoList();
                        groupListUI.removeItemByKey(groupId);
                        groupListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback readMessageCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group group = (Group) getData("group");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null && group != null) {
                        GroupListUI groupListUI = (GroupListUI) groupListAdapter.getPokoList();
                        groupListUI.addContactChatGroupIfHasMessage(group);
                        groupListUI.moveItemSortedByKey(group);
                        groupListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback newMessageCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group group = (Group) getData("group");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null && group != null) {
                        GroupListUI groupListUI = (GroupListUI) groupListAdapter.getPokoList();
                        groupListUI.addContactChatGroupIfHasMessage(group);
                        groupListUI.moveItemSortedByKey(group);
                        groupListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    /* Activity and Dialog open methods */
    public void openContactDetailDialog(Contact contact) {
        ContactDetailDialog dialog = new ContactDetailDialog();
        dialog.setContact(contact);
        dialog.show(getSupportFragmentManager(), "친구 정보");
    }

    public void openContactOptionDialog(Contact contact) {
        ContactOptionDialog dialog = new ContactOptionDialog();
        dialog.setContact(contact);
        dialog.show(getSupportFragmentManager(), "친구 옵션");
    }

    public void openGroupOptionDialog(Group group) {
        GroupOptionDialog dialog = new GroupOptionDialog();
        dialog.setGroup(group);
        dialog.show(getSupportFragmentManager(), "그룹 옵션");
    }

    /* Dialog result listeners */
    @Override
    public void contactDetailOptionClick(Contact contact, int option) {
        switch(option) {
            case ContactDetailDialog.CONTACT_CHAT:
                startContactChat(contact);
                break;
        }
    }

    @Override
    public void contactOptionClick(Contact contact, int option) {
        switch(option) {
            case ContactOptionDialog.CHAT:
                startContactChat(contact);
                break;
            case ContactOptionDialog.REMOVE_CONTACT:
                server.sendRemoveContact(contact.getEmail());
                break;
        }
    }

    @Override
    public void groupOptionClick(Group group, int option) {
        switch(option) {
            case GroupOptionDialog.CHAT:
                startGroupChat(group);
                break;
            case GroupOptionDialog.INVITE_CONTACT:
                break;
            case GroupOptionDialog.EXIT_GROUP:
                GroupExitWarningDialog dialog = new GroupExitWarningDialog();
                dialog.setGroup(group);
                dialog.show(getSupportFragmentManager(), "Group exit warning");
                break;
        }
    }

    @Override
    public void groupExitOptionApply(Group group, int option) {
        switch (option) {
            case GroupExitWarningDialog.EXIT_GROUP: {
                server.sendExitGroup(group.getGroupId());
                break;
            }
        }
    }

    /* Chat methods */
    public void startContactChat(Contact contact) {
        Group contactChatGroup = null;
        ContactList contactList = collection.getContactList();
        GroupList groupList = collection.getGroupList();
        ContactList.ContactGroupRelation relation =
                contactList.getContactGroupRelationByUserId(contact.getUserId());
        if (relation != null) {
            contactChatGroup = groupList.getItemByKey(relation.getGroupId());
        }

        /* Start contact chat */
        if (relation == null || contactChatGroup == null) {
            server.sendJoinContactChat(contact.getEmail());
        } else {
            startGroupChat(contactChatGroup);
        }
    }

    public void startGroupChat(final Group group) {
        /* Start group chat */
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("groupId", group.getGroupId());
        startActivityForResult(intent, RequestCode.GROUP_CHAT.value);
        Log.v("POKO", "START GROUP CHAT " + group.getGroupId());
    }

    /* Service callbacks */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceMessenger = new Messenger(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Context context = getApplicationContext();
        PokoTalkService.startPokoTalkService(context);
        PokoTalkService.bindPokoTalkService(context, this);
    }

    class ServiceCallback extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PokoTalkService.APP_FOREGROUND: {

                }
            }
        }
    }
}
