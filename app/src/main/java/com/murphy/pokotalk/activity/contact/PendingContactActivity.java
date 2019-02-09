package com.murphy.pokotalk.activity.contact;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.PendingContactListAdapter;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.view.PendingContactItem;

public class PendingContactActivity extends AppCompatActivity implements ContactAddDialog.ContactAddDialogListener,
PendingContactOptionDialog.PendingContactOptionDialogListener {
    private PokoServer server;
    private Button addButton;
    private ListView invitedListView;
    private ListView invitingListView;
    private PendingContactList invitedList;
    private PendingContactList invitingList;
    private PendingContactListAdapter invitedListAdapter;
    private PendingContactListAdapter invitingListAdapter;
    private AlertDialog.Builder contactBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pending_contact_list_layout);

        invitedListView = (ListView) findViewById(R.id.invitedList);
        invitingListView = (ListView) findViewById(R.id.invitingList);
        addButton = (Button) findViewById(R.id.contactAddButton);

        /* Get collection and contact lists */
        DataCollection data = DataCollection.getInstance();
        invitedList = data.getInvitedContactList();
        invitingList = data.getInvitingContactList();

        /* Create and set adapters */
        invitedListAdapter = new PendingContactListAdapter(this);
        invitingListAdapter = new PendingContactListAdapter(this);
        invitedListAdapter.setViewCreationCallback(invitedContactCreationCallback);
        invitingListAdapter.setViewCreationCallback(invitingContactCreationCallback);
        invitedListAdapter.getPokoList().copyFromPokoList(invitedList);
        invitingListAdapter.getPokoList().copyFromPokoList(invitingList);
        invitedListView.setAdapter(invitedListAdapter);
        invitingListView.setAdapter(invitingListAdapter);
        invitedListAdapter.setInvited(true);
        invitingListAdapter.setInvited(false);

        /* Get server and attach event callback */
        server = PokoServer.getInstance(this);
        server.attachActivityCallback(Constants.getContactListName, getPendingContactListCallback);
        server.attachActivityCallback(Constants.getPendingContactListName, getPendingContactListCallback);
        server.attachActivityCallback(Constants.newPendingContactName, addPendingContactCallback);
        server.attachActivityCallback(Constants.newContactName, removePendingContactCallback);
        server.attachActivityCallback(Constants.contactRemovedName, removePendingContactCallback);
        server.attachActivityCallback(Constants.contactDeniedName, removePendingContactCallback);
        server.sendGetPendingContactList();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContactAddDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        server.detachActivityCallback(Constants.getContactListName, getPendingContactListCallback);
        server.detachActivityCallback(Constants.getPendingContactListName, getPendingContactListCallback);
        server.detachActivityCallback(Constants.newPendingContactName, addPendingContactCallback);
        server.detachActivityCallback(Constants.newContactName, removePendingContactCallback);
        server.detachActivityCallback(Constants.contactRemovedName, removePendingContactCallback);
        server.detachActivityCallback(Constants.contactDeniedName, removePendingContactCallback);

        super.onDestroy();
    }

    /* Server event callback */
    private ActivityCallback getPendingContactListCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    invitedListAdapter.getPokoList().copyFromPokoList(invitedList);
                    invitingListAdapter.getPokoList().copyFromPokoList(invitingList);
                    invitedListAdapter.notifyDataSetChanged();
                    invitingListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback addPendingContactCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Boolean invited = (Boolean) getData("invited");
                    PendingContact pendingContact = (PendingContact) getData("pendingContact");
                    if (invited == null || pendingContact == null) {
                        return;
                    }
                    if (invited) {
                        invitedListAdapter.getPokoList().updateItem(pendingContact);
                        invitedListAdapter.notifyDataSetChanged();
                    } else {
                        invitingListAdapter.getPokoList().updateItem(pendingContact);
                        invitingListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback removePendingContactCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Integer userId = (Integer) getData("userId");
                    if (userId != null) {
                        invitedListAdapter.getPokoList().removeItemByKey(userId);
                        invitingListAdapter.getPokoList().removeItemByKey(userId);
                    }
                    Contact contact = (Contact) getData("contact");
                    if (contact != null) {
                        invitedListAdapter.getPokoList().removeItemByKey(contact.getUserId());
                        invitingListAdapter.getPokoList().removeItemByKey(contact.getUserId());
                    }
                    invitedListAdapter.notifyDataSetChanged();
                    invitingListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    /* List item creation callback */
    private ViewCreationCallback invitedContactCreationCallback = new ViewCreationCallback() {
        @Override
        public void run(View view) {
            PendingContactItem item = (PendingContactItem) view;
            final PendingContact contact = item.getContact();

            Button acceptButton = item.getAcceptButton();
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    server.sendAcceptContact(contact.getEmail());
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    openContactOptionDialog(contact);
                    return true;
                }
            });
        }
    };

    private ViewCreationCallback invitingContactCreationCallback = new ViewCreationCallback() {
        @Override
        public void run(View view) {
            PendingContactItem item = (PendingContactItem) view;
            PendingContact contact = item.getContact();
        }
    };

    /* Dialog opening functions */
    public void openContactAddDialog() {
        ContactAddDialog dialog = new ContactAddDialog();
        dialog.show(getSupportFragmentManager(), "친구 추가");
    }

    public void openContactOptionDialog(PendingContact contact) {
        PendingContactOptionDialog dialog = new PendingContactOptionDialog();
        dialog.setContact(contact);
        dialog.show(getSupportFragmentManager(), "친구 요청 응답 옵션");
    }

    /* Dialog listeners */
    @Override
    public void contactAdd(String email) {
        server.sendAddContact(email);
        Toast.makeText(this, "친구 추가 요청 보냄", Toast.LENGTH_SHORT);
    }

    @Override
    public void pendingContactOptionClick(int option, PendingContact contact) {
        switch (option) {
            case PendingContactOptionDialog.ACCEPT_CONTACT:
                server.sendAcceptContact(contact.getEmail());
                break;
            case PendingContactOptionDialog.DENY_CONTACT:
                server.sendDenyContact(contact.getEmail());
                break;
        }
    }
}
