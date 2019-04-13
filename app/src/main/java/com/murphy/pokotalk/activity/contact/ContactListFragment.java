package com.murphy.pokotalk.activity.contact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.adapter.contact.ContactListAdapter;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactPokoList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.view.ContactItem;

public class ContactListFragment extends Fragment {
    private ContactListAdapter contactListAdapter;
    private PokoServer server;
    private ListView contactListView;
    private Button contactAddButton;
    private Listener listener;

    public interface Listener {
        void openContactDetailDialog(Contact contact);
        void openContactOptionDialog(Contact contact);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " should implement listener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_list_layout, null, false);

        // Get views
        contactListView = view.findViewById(R.id.contactList);
        contactAddButton = view.findViewById(R.id.contactAddButton);

        // Add listeners
        contactAddButton.setOnClickListener(contactAddButtonClickListener);

        try {
            DataLock.getInstance().acquireWriteLock();

            try {
                // Get contact list
                ContactPokoList contactList = DataCollection.getInstance().getContactList();

                // Create contact list adapter
                contactListAdapter = new ContactListAdapter(getContext());
                contactListAdapter.setViewCreationCallback(contactCreationCallback);

                // Copy contact list
                ContactPokoList contactListUI = (ContactPokoList) contactListAdapter.getPokoList();
                contactListUI.copyFromPokoList(contactList);

                // Set adapter
                contactListView.setAdapter(contactListAdapter);
            } finally {
                DataLock.getInstance().releaseWriteLock();

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Get server
        server = PokoServer.getInstance();

        server.attachActivityCallback(Constants.getContactListName, getContactListCallback);
        server.attachActivityCallback(Constants.getPendingContactListName, getContactListCallback);
        server.attachActivityCallback(Constants.newContactName, addContactCallback);
        server.attachActivityCallback(Constants.newPendingContactName, removeContactCallback);
        server.attachActivityCallback(Constants.contactDeniedName, removeContactCallback);
        server.attachActivityCallback(Constants.contactRemovedName, removeContactCallback);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (server != null) {
            server.detachActivityCallback(Constants.getContactListName, getContactListCallback);
            server.detachActivityCallback(Constants.getPendingContactListName, getContactListCallback);
            server.detachActivityCallback(Constants.newContactName, addContactCallback);
            server.detachActivityCallback(Constants.newPendingContactName, removeContactCallback);
            server.detachActivityCallback(Constants.contactDeniedName, removeContactCallback);
            server.detachActivityCallback(Constants.contactRemovedName, removeContactCallback);
        }
    }

    private View.OnClickListener contactAddButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), PendingContactActivity.class);
            startActivity(intent);
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
                    listener.openContactDetailDialog(contact);
                }
            });
            contactView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.openContactOptionDialog(contact);
                    return true;
                }
            });
        }
    };

    private ActivityCallback getContactListCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /* Refresh contact list */
                    ContactPokoList contactList = DataCollection.getInstance().getContactList();
                    ContactPokoList adapterList = (ContactPokoList) contactListAdapter.getPokoList();
                    adapterList.copyFromPokoList(contactList);
                    contactListAdapter.notifyDataSetChanged();
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /* Refresh contact list */
                    if (contact != null) {
                        ContactPokoList contactList = (ContactPokoList) contactListAdapter.getPokoList();
                        contactList.updateItem(contact);
                        contactListAdapter.notifyDataSetChanged();
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /* Refresh contact list */
                    if (userId != null) {
                        ContactPokoList contactList = (ContactPokoList) contactListAdapter.getPokoList();
                        contactList.removeItemByKey(userId);
                    }
                    if (pendingContact != null) {
                        ContactPokoList contactList = (ContactPokoList) contactListAdapter.getPokoList();
                        contactList.removeItemByKey(pendingContact.getUserId());
                    }

                    contactListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };
}
