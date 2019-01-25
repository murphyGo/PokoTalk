package com.murphy.pokotalk.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.Constants.RequestCode;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.activity.chat.GroupAddActivity;
import com.murphy.pokotalk.activity.chat.GroupOptionDialog;
import com.murphy.pokotalk.activity.contact.ContactDetailDialog;
import com.murphy.pokotalk.activity.contact.ContactOptionDialog;
import com.murphy.pokotalk.activity.contact.PendingContactActivity;
import com.murphy.pokotalk.adapter.ContactListAdapter;
import com.murphy.pokotalk.adapter.GroupListAdapter;
import com.murphy.pokotalk.adapter.MpagerAdapter;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.view.ContactItem;
import com.murphy.pokotalk.view.GroupItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        ContactDetailDialog.ContactDetailDialogListener,
        ContactOptionDialog.ContactOptionDialogListener,
        GroupOptionDialog.GroupOptionDialogListener {
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

        /* If application has no session id to login, show login activity */
        session = Session.getInstance();
        if (!session.sessionIdExists()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, RequestCode.LOGIN.value);
        }

        server = PokoServer.getInstance(this);

        /* Attach view pager callbacks */
        pagerAdapter.enrollItemCallback(R.layout.contact_list_layout, contactListCreationCallback);
        pagerAdapter.enrollItemCallback(R.layout.group_list_layout, groupListCreationCallback);
        pagerAdapter.enrollItemCallback(R.layout.event_list_layout, eventListCreationCallback);

        /* Attach event callbacks */
        server.attachActivityCallback(Constants.sessionLoginName, sessionLoginCallback);
        server.attachActivityCallback(Constants.getContactListName, refreshContactListCallback);
        server.attachActivityCallback(Constants.getPendingContactListName, refreshContactListCallback);
        server.attachActivityCallback(Constants.newContactName, refreshContactListCallback);
        server.attachActivityCallback(Constants.newPendingContactName, refreshContactListCallback);
        server.attachActivityCallback(Constants.contactDeniedName, refreshContactListCallback);
        server.attachActivityCallback(Constants.contactRemovedName, refreshContactListCallback);
        server.attachActivityCallback(Constants.joinContactChatName, joinContactChatCallback);
        server.attachActivityCallback(Constants.getGroupListName, groupListRefreshCallback);
        server.attachActivityCallback(Constants.addGroupName, groupListRefreshCallback);
        server.attachActivityCallback(Constants.exitGroupName, groupListRefreshCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        server.detachActivityCallback(Constants.sessionLoginName, sessionLoginCallback);
        server.detachActivityCallback(Constants.getContactListName, refreshContactListCallback);
        server.detachActivityCallback(Constants.getPendingContactListName, refreshContactListCallback);
        server.detachActivityCallback(Constants.newContactName, refreshContactListCallback);
        server.detachActivityCallback(Constants.newPendingContactName, refreshContactListCallback);
        server.detachActivityCallback(Constants.contactDeniedName, refreshContactListCallback);
        server.detachActivityCallback(Constants.contactRemovedName, refreshContactListCallback);
        server.detachActivityCallback(Constants.joinContactChatName, joinContactChatCallback);
        server.detachActivityCallback(Constants.getGroupListName, groupListRefreshCallback);
        server.detachActivityCallback(Constants.addGroupName, groupListRefreshCallback);
        server.detachActivityCallback(Constants.exitGroupName, groupListRefreshCallback);
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
    private ViewCreationCallback contactListCreationCallback = new ViewCreationCallback() {
        @Override
        public void run(View view) {
            /* Contact list view settings */
            /* Create contact list adapter */
            ListView contactListLayout = view.findViewById(R.id.contactList);
            ArrayList<Contact> contacts = collection.getContactList().getList();
            contactListAdapter = new ContactListAdapter(getApplicationContext(), contacts);
            contactListAdapter.setViewCreationCallback(contactCreationCallback);
            contactListLayout.setAdapter(contactListAdapter);

            /* Add button listeners */
            Button contactAddButton = view.findViewById(R.id.contactAddButton);
            contactAddButton.setOnClickListener(contactAddButtonClickListener);

            server.sendGetContactList();
        }
    };

    private ViewCreationCallback groupListCreationCallback = new ViewCreationCallback() {
        @Override
        public void run(View view) {
            /* Contact list view settings */
            /* Create contact list adapter */
            ListView groupListLayout = view.findViewById(R.id.groupList);
            ArrayList<Group> groups = collection.getGroupList().getList();
            groupListAdapter = new GroupListAdapter(getApplicationContext(), groups);
            groupListAdapter.setViewCreationCallback(groupCreationCallback);
            groupListLayout.setAdapter(groupListAdapter);

            /* Add button listeners */
            Button groupAddButton = view.findViewById(R.id.groupAddButton);
            groupAddButton.setOnClickListener(groupAddButtonClickListener);

            server.sendGetGroupList();
        }
    };

    private ViewCreationCallback eventListCreationCallback = new ViewCreationCallback() {
        @Override
        public void run(View view) {

        }
    };

    private ViewCreationCallback contactCreationCallback = new ViewCreationCallback() {
        @Override
        public void run(View view) {
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

    private ViewCreationCallback groupCreationCallback = new ViewCreationCallback() {
        @Override
        public void run(View view) {
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

    /* On activity result callbacks */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.LOGIN.value)
            handleLoginResult(resultCode, data);
        else if (requestCode == RequestCode.GROUP_ADD.value)
            handleGroupAddResult(resultCode, data);
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

    /* Server message callbacks */
    private ActivityCallback sessionLoginCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            /* Get up-to-date contact and group list */
            server.sendGetContactList();
            server.sendGetGroupList();
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback refreshContactListCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /* Refresh contact list */
                    if (contactListAdapter != null) {
                        contactListAdapter.refreshAllExistingViews();
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    ContactList contactList = collection.getContactList();
                    try {
                        int contactId = jsonObject.getInt("contactId");
                        Contact contact = contactList.getItemByKey(contactId);
                        /* Start group chat with contact */
                        if (contact != null) {
                            startGroupChat(contact.getChatGroup());
                        }
                    } catch (JSONException e) {

                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback groupListRefreshCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null) {
                        groupListAdapter.refreshAllExistingViews();
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
                break;
        }
    }

    /* Chat methods */
    public void startContactChat(Contact contact) {
        /* Start contact chat */
        if (contact.getGroupId() == null) {
            server.sendJoinContactChat(contact.getEmail());
        } else {
            GroupList groupList = DataCollection.getInstance().getGroupList();
            Group group = groupList.getItemByKey(contact.getGroupId());
            if (group != null) {
                startGroupChat(group);
            }
        }
    }

    public void startGroupChat(final Group group) {
        // do something to do chat
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "그룹 " + group.getGroupId() + " 채팅 시작", Toast.LENGTH_SHORT).show();
            }
        });
        /*
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);*/
    }
}
