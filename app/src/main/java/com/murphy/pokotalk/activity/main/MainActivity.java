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
import com.murphy.pokotalk.activity.chat.ChatActivity;
import com.murphy.pokotalk.activity.contact.PendingContactActivity;
import com.murphy.pokotalk.adapter.ContactListAdapter;
import com.murphy.pokotalk.adapter.MpagerAdapter;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        ContactDetailDialog.contactDetailDialogListener {
    private PokoServer server;
    private ViewPager viewPager;
    private BottomNavigationView navigationMenu;
    private int[] layouts = {R.layout.contact_list_layout, R.layout.group_list_layout,
            R.layout.event_list_layout};
    private MpagerAdapter pagerAdapter;
    private ContactListAdapter contactListAdapter;

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
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new MpagerAdapter(this, layouts);
        viewPager.setAdapter(pagerAdapter);

        navigationMenu = (BottomNavigationView) findViewById(R.id.mainNavigation);
        navigationMenu.setOnNavigationItemSelectedListener(this);

        /* If application has no session id to login, show login activity */
        Session session = Session.getInstance();
        if (!session.sessionIdExists()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, RequestCode.LOGIN.value);
        }

        server = PokoServer.getInstance(this);

        /* Attach view pager callbacks */
        pagerAdapter.enrollItemCallback(R.layout.contact_list_layout, contactListCreationCallback);

        /* Attach event callbacks */
        server.attachActivityCallback(Constants.getContactListName, getContactListCallback);
        server.attachActivityCallback(Constants.newContactName, newContactCallback);
        server.attachActivityCallback(Constants.contactRemovedName, contactRemovedCallback);
        server.attachActivityCallback(Constants.joinContactChatName, contactChatJoinCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        server.detachActivityCallback(Constants.getContactListName, getContactListCallback);
        server.detachActivityCallback(Constants.newContactName, newContactCallback);
        server.detachActivityCallback(Constants.contactRemovedName, contactRemovedCallback);
        server.detachActivityCallback(Constants.joinContactChatName, contactChatJoinCallback);
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
            ArrayList<Contact> contacts = DataCollection.getInstance().getContactList().getList();
            contactListAdapter = new ContactListAdapter(getApplicationContext(), contacts);
            contactListLayout.setAdapter(contactListAdapter);

            /* Add button listeners */
            Button contactAddButton = view.findViewById(R.id.contactAddButton);
            contactAddButton.setOnClickListener(contactButtonClickListener);

            server.sendGetContactList();
        }
    };

    /* User touch event listeners */
    private View.OnClickListener contactButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), PendingContactActivity.class);
            startActivity(intent);
        }
    };

    /* On activity result callbacks */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.LOGIN.value)
            handleLoginResult(resultCode, data);
    }

    private void handleLoginResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

        } else if (resultCode == RESULT_CANCELED) {
            finish();
        } else {
            finish();
        }

    }

    /* Server message callbacks */
    private ActivityCallback getContactListCallback = new ActivityCallback() {
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
            Toast.makeText(getApplicationContext(), "Failed to get contact list",
                    Toast.LENGTH_LONG).show();
        }
    };

    private ActivityCallback newContactCallback = new ActivityCallback() {
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
            Toast.makeText(getApplicationContext(), "Failed to get new contact",
                    Toast.LENGTH_LONG).show();
        }
    };

    private ActivityCallback contactRemovedCallback = new ActivityCallback() {
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
            Toast.makeText(getApplicationContext(), "Failed to get removed contact",
                    Toast.LENGTH_LONG).show();
        }
    };

    private ActivityCallback contactChatJoinCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {
            Toast.makeText(getApplicationContext(), "Failed to get removed contact",
                    Toast.LENGTH_LONG).show();
        }
    };

    /* Dialog methods */
    @Override
    public void detailOptionSelect(Contact contact, int option) {
        switch(option) {
            case ContactDetailDialog.CONTACT_CHAT:
                /* Start contact chat */
                if (contact.getGroupId() == null) {
                    server.sendJoinContactChat(contact.getEmail());
                } else {
                    GroupList groupList = DataCollection.getInstance().getGroupList();
                    Group group = groupList.getItemByKey(contact.getGroupId());
                    if (group != null) {
                        // do something to do chat
                    }
                }
                break;
        }
    }

    public void startChat(Group group) {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }
}
