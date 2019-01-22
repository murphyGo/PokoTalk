package com.murphy.pokotalk.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.PendingContactListAdapter;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.data.Contact;
import com.murphy.pokotalk.data.ContactList;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.view.PendingContactItem;

public class PendingContactActivity extends AppCompatActivity implements ContactAddDialog.ContactAddDialogListener {
    private PokoServer server;
    private Button addButton;
    private ListView invitedListView;
    private ListView invitingListView;
    private ContactList invitedList;
    private ContactList invitingList;
    private PendingContactListAdapter invitedListAdapter;
    private PendingContactListAdapter invitingListAdapter;

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
        invitedListAdapter = new PendingContactListAdapter(this, invitedList.getContactList());
        invitingListAdapter = new PendingContactListAdapter(this, invitingList.getContactList());
        invitedListAdapter.setViewCreationCallback(invitedContactCreationCallback);
        invitingListAdapter.setViewCreationCallback(invitingContactCreationCallback);
        invitedListView.setAdapter(invitedListAdapter);
        invitingListView.setAdapter(invitingListAdapter);
        invitedListAdapter.setInvited(true);
        invitingListAdapter.setInvited(false);

        /* Get server and attach event callback */
        server = PokoServer.getInstance(this);
        server.attachActivityCallback(Constants.getPendingContactListName, refreshListCallback);
        server.attachActivityCallback(Constants.newPendingContactName, refreshListCallback);
        server.attachActivityCallback(Constants.newContactName, refreshListCallback);
        server.attachActivityCallback(Constants.contactDeniedName, refreshListCallback);
        server.sendGetPendingContactList();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContactAddDialog();
            }
        });
    }

    /* Server event callback */
    private ActivityCallback refreshListCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            invitedListAdapter.notifyDataSetChanged();
            invitingListAdapter.notifyDataSetChanged();
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
            final Contact contact = item.getContact();

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


                    return true;
                }
            });
        }
    };

    private ViewCreationCallback invitingContactCreationCallback = new ViewCreationCallback() {
        @Override
        public void run(View view) {
            PendingContactItem item = (PendingContactItem) view;
            Contact contact = item.getContact();
        }
    };

    /* Contact add dialog functions */
    public void openContactAddDialog() {
        ContactAddDialog dialog = new ContactAddDialog();
        dialog.show(getSupportFragmentManager(), "친구 추가");
    }

    @Override
    public void applyTexts(String email) {
        server.sendAddContact(email);
        Toast.makeText(this, "친구 추가 요청 보냄", Toast.LENGTH_SHORT);
    }
}
