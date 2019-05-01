package com.murphy.pokotalk.activity.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.Constants.RequestCode;
import com.murphy.pokotalk.PokoTalkApp;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.activity.chat.ChatActivity;
import com.murphy.pokotalk.activity.chat.GroupExitWarningDialog;
import com.murphy.pokotalk.activity.chat.GroupListFragment;
import com.murphy.pokotalk.activity.chat.GroupOptionDialog;
import com.murphy.pokotalk.activity.contact.ContactDetailDialog;
import com.murphy.pokotalk.activity.contact.ContactListFragment;
import com.murphy.pokotalk.activity.contact.ContactOptionDialog;
import com.murphy.pokotalk.activity.event.EventDetailActivity;
import com.murphy.pokotalk.activity.event.EventListFragment;
import com.murphy.pokotalk.activity.event.EventOptionDialog;
import com.murphy.pokotalk.activity.settings.SettingFragment;
import com.murphy.pokotalk.adapter.MainFragmentViewPagerAdapter;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.PokoLock;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupPokoList;
import com.murphy.pokotalk.data.group.MessagePokoList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactPokoList;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.service.PokoTalkService;

import java.util.List;

public class MainActivity extends FragmentActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        ContactListFragment.Listener,
        GroupListFragment.Listener,
        EventListFragment.Listener,
        SettingFragment.Listener,
        ContactDetailDialog.ContactDetailDialogListener,
        ContactOptionDialog.ContactOptionDialogListener,
        GroupOptionDialog.GroupOptionDialogListener,
        GroupExitWarningDialog.Listener,
        EventOptionDialog.Listener,
        ServiceConnection {
    private PokoTalkApp app;
    private PokoServer server;
    private DataCollection collection;
    private RelativeLayout rootView;
    private ViewPager viewPager;
    private MainFragmentViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private Fragment[] fragments =
            {new ContactListFragment(), new GroupListFragment(),
                    new EventListFragment(), new SettingFragment()};
    private ContactListFragment contactListFragment = (ContactListFragment) fragments[0];
    private GroupListFragment groupListFragment = (GroupListFragment) fragments[1];
    private EventListFragment eventListFragment = (EventListFragment) fragments[2];
    private SettingFragment settingFragment = (SettingFragment) fragments[3];
    private Messenger serviceMessenger = null;
    private Messenger myMessenger = new Messenger(new ServiceCallback());

    /* Intent commands */
    public static final int START_GROUP_CHAT = 1;

    public static final int MESSAGE_CUT_THRESHOLD = 20;

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

        // Get server
        server = PokoServer.getInstance();

        // Start view pager(contact, group, event, configuration menu)
        collection = DataCollection.getInstance();

        // Find views
        rootView = findViewById(R.id.mainActivityRoot);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.mainTab);

        /* Bind to service */
        Context context = getApplicationContext();
        PokoTalkService.startPokoTalkService(context);
        PokoTalkService.bindPokoTalkService(context, this);

        // Get application
        app = PokoTalkApp.getInstance();

        // Check if application data is loaded
        if (!app.isAppDataLoaded()) {
            // Wait until app data is loaded
            // Activity will be shown when data loading is done
            /** This scenario may happen when the application is killed by
             *  operating system and user start application again.
             *  Then main activity will be shown but app data is not loaded yet.
             */

            // Do not show main activity
            rootView.setVisibility(View.INVISIBLE);

            // Attach callback
            app.notifyWhenAppDataLoaded(new Runnable() {
                @Override
                public void run() {
                    showMainActivity();
                }
            });

        } else {
            showMainActivity();

            /* Get intent and start operation if given */
            Intent intent = getIntent();
            int opcode = intent.getIntExtra("opcode", -1);
            if (opcode >= 0) {
                startOperation(opcode, intent);
            }
        }

        /* Attach event callbacks */
        server.attachActivityCallback(Constants.sessionLoginName, sessionLoginCallback);
        server.attachActivityCallback(Constants.joinContactChatName, joinContactChatCallback);

        Log.v("POKO", "MainActivity starts, process id " + Process.myPid());
        Log.v("POKO", "POKO ON CREATE");
    }

    // Called when application data loaded, show activity and popup loin activity if necessary
    private void showMainActivity() {
        // Get session
        Session session = Session.getInstance();

        // We should show login activity if session id does not exists
        if (!session.sessionIdExists()) {
            // Set root view invisible
            rootView.setVisibility(View.INVISIBLE);

            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, RequestCode.LOGIN.value);
        } else {
            if (session.hasLogined() && app.shouldRequestUpToDateDataSinceFirstLogin()) {
                /* Get up-to-date contact and group and event list */
                server.sendGetContactList();
                server.sendGetGroupList();
                server.sendGetEventList();
            }

            // Set root view visible
            rootView.setVisibility(View.VISIBLE);

            // Set view pager adapter
            viewPagerAdapter = new MainFragmentViewPagerAdapter(this,
                    getSupportFragmentManager(), fragments);
            viewPager.setAdapter(viewPagerAdapter);

            // Set navigation view selected listener
            tabLayout.setupWithViewPager(viewPager);

            // Set icons of tab
            tabLayout.getTabAt(0).setIcon(R.drawable.user_list);
            tabLayout.getTabAt(1).setIcon(R.drawable.chat_icon);
            tabLayout.getTabAt(2).setIcon(R.drawable.event_icon);
            tabLayout.getTabAt(3).setIcon(R.drawable.settings);
            //navigationMenu.setOnNavigationItemSelectedListener(this);
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
        server.detachActivityCallback(Constants.joinContactChatName, joinContactChatCallback);

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
            case R.id.navigation_settings:
                viewPager.setCurrentItem(3, true);
                break;
        }
        return false;
    }

    /* On activity result callbacks */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.LOGIN.value)
            handleLoginResult(resultCode, data);
        else if (requestCode == RequestCode.GROUP_CHAT.value)
            handleGroupChatResult(resultCode, data);
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    // Activity result handlers
    private void handleLoginResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Get application
            PokoTalkApp app = PokoTalkApp.getInstance();

            // Check if data is loaded
            if (!app.isAppDataLoaded()) {
                // Hide activity view
                rootView.setVisibility(View.INVISIBLE);

                // Wait data to be loaded
                app.notifyWhenAppDataLoaded(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Show activity view
                                showMainActivity();
                            }
                        });
                    }
                });
            } else {
                // Show activity right away
                showMainActivity();
            }
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        } else {
            finish();
        }
    }

    private void handleGroupChatResult(int resultCode, Intent data) {
        Log.v("POKO", "RESULT CODE " + (resultCode == RESULT_OK ? "OK" : "NOT"));
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

            // Get message list of group
            MessagePokoList messageList = group.getMessageList();
            List<PokoMessage> messages = messageList.getList();

            // Cut messages if there are too many many messages
            // to save memory and improve performance
            if (messages.size() > MESSAGE_CUT_THRESHOLD) {
                new messageCutTask().execute(group);
            }

            // Notify group list data set changed
            groupListFragment.notifyDataSetChanged();
        }
    }

    // This job cuts messages if there are too many many messages
    // to save memory and improve performance
    static class messageCutTask extends AsyncTask<Group, Void, Void> {
        @Override
        protected Void doInBackground(Group... groups) {
            // Get group
            Group group = groups[0];
            if (group == null) {
                return null;
            }

            try {
                PokoLock.getDataLockInstance().acquireWriteLock();

                // Get message list of group
                MessagePokoList messageList = group.getMessageList();
                List<PokoMessage> messages = messageList.getList();

                // We allow at most MESSAGE_CUT_THRESHOLD messages persist in memory
                if (messages.size() > MESSAGE_CUT_THRESHOLD) {
                    messageList.cutMessage(MESSAGE_CUT_THRESHOLD);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                PokoLock.getDataLockInstance().releaseWriteLock();
            }

            return null;
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

    private ActivityCallback joinContactChatCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, final Object... args) {
            final Contact contact = (Contact) getData("contact");
            final Group group = (Group) getData("group");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (group != null) {
                        // Start group chat with contact
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

    /* Activity and Dialog open methods */
    @Override
    public void openContactDetailDialog(Contact contact) {
        ContactDetailDialog dialog = new ContactDetailDialog();
        dialog.setContact(contact);
        dialog.show(getSupportFragmentManager(), "친구 정보");
    }

    @Override
    public void openContactOptionDialog(Contact contact) {
        ContactOptionDialog dialog = new ContactOptionDialog();
        dialog.setContact(contact);
        dialog.show(getSupportFragmentManager(), "친구 옵션");
    }

    @Override
    public void openGroupOptionDialog(Group group) {
        GroupOptionDialog dialog = new GroupOptionDialog();
        dialog.setGroup(group);
        dialog.show(getSupportFragmentManager(), "그룹 옵션");
    }

    @Override
    public void openEventOptionDialog(PokoEvent event) {
        EventOptionDialog dialog = new EventOptionDialog();
        dialog.setEvent(event);
        dialog.show(getSupportFragmentManager(), "이벤트 옵션");
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

    @Override
    public void eventOptionClick(PokoEvent event, int option) {
        switch(option) {
            case EventOptionDialog.OPTION_DETAIL: {
                // Start event detail activity
                Intent intent = new Intent(this, EventDetailActivity.class);
                intent.putExtra("eventId", event.getEventId());
                startActivity(intent);
                break;
            }
            case EventOptionDialog.OPTION_EXIT: {
                // Send event exit message
                server.sendEventExit(event.getEventId());
                break;
            }
        }
    }

    @Override
    public void onSettingAction(int action) {
        switch (action) {
            case SettingFragment.ACTION_LOGOUT: {
                // Get application
                PokoTalkApp app = PokoTalkApp.getInstance();

                // Logout user
                app.logoutUser();

                // Restart main activity
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);

                // End this main activity
                finish();

                break;
            }
        }
    }

    /* Chat methods */
    public void startContactChat(Contact contact) {
        Group contactChatGroup = null;
        ContactPokoList contactList = collection.getContactList();
        GroupPokoList groupList = collection.getGroupList();
        ContactPokoList.ContactGroupRelation relation =
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

    @Override
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
