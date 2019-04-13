package com.murphy.pokotalk.activity.contact;

import android.content.Context;
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
import com.murphy.pokotalk.adapter.contact.PendingContactListAdapter;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactPokoList;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.view.PendingContactItem;

public class InvitedPendingContactFragment extends Fragment {
    private PokoServer server;
    private ListView listView;
    private PendingContactPokoList invitedList;
    private PendingContactListAdapter invitedListAdapter;
    private PendingContactPokoList pendingContactList;
    private Listener listener;

    public interface Listener {
        void openContactOptionDialog(PendingContact contact);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement listener interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pending_contact_fragment, container, false);

        // Find list view
        listView = view.findViewById(R.id.pendingContactListView);

        // Get collection and contact lists
        DataCollection data = DataCollection.getInstance();
        invitedList = data.getInvitedContactList();

        // Create adapter and set to ListView */
        try {
            DataLock.getInstance().acquireWriteLock();

            try {
                invitedListAdapter = new PendingContactListAdapter(getContext());
                invitedListAdapter.setViewCreationCallback(invitedContactCreationCallback);
                PendingContactPokoList invitedListUI = (PendingContactPokoList) invitedListAdapter.getPokoList();
                invitedListUI.copyFromPokoList(invitedList);
                listView.setAdapter(invitedListAdapter);
                invitedListAdapter.setInvited(true);
                pendingContactList = (PendingContactPokoList) invitedListAdapter.getPokoList();
            } finally {
                DataLock.getInstance().releaseWriteLock();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Get server and attach event callback */
        server = PokoServer.getInstance();
        server.attachActivityCallback(Constants.getContactListName, getPendingContactListCallback);
        server.attachActivityCallback(Constants.getPendingContactListName, getPendingContactListCallback);
        server.attachActivityCallback(Constants.newPendingContactName, addPendingContactCallback);
        server.attachActivityCallback(Constants.newContactName, removePendingContactCallback);
        server.attachActivityCallback(Constants.contactRemovedName, removePendingContactCallback);
        server.attachActivityCallback(Constants.contactDeniedName, removePendingContactCallback);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        server.detachActivityCallback(Constants.getContactListName, getPendingContactListCallback);
        server.detachActivityCallback(Constants.getPendingContactListName, getPendingContactListCallback);
        server.detachActivityCallback(Constants.newPendingContactName, addPendingContactCallback);
        server.detachActivityCallback(Constants.newContactName, removePendingContactCallback);
        server.detachActivityCallback(Constants.contactRemovedName, removePendingContactCallback);
        server.detachActivityCallback(Constants.contactDeniedName, removePendingContactCallback);
    }

    /* Server event callback */
    private ActivityCallback getPendingContactListCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    invitedListAdapter.getPokoList().copyFromPokoList(invitedList);
                    invitedListAdapter.notifyDataSetChanged();
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
            final Boolean invited = (Boolean) getData("invited");
            final PendingContact pendingContact = (PendingContact) getData("pendingContact");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (invited == null || pendingContact == null) {
                        return;
                    }
                    if (invited) {
                        pendingContactList.updateItem(pendingContact);
                        invitedListAdapter.notifyDataSetChanged();
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
            final Integer userId = (Integer) getData("userId");
            final Contact contact = (Contact) getData("contact");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (userId != null) {
                        pendingContactList.removeItemByKey(userId);
                    }
                    if (contact != null) {
                        pendingContactList.removeItemByKey(contact.getUserId());
                    }
                    invitedListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    // View creation callback
    private ViewCreationCallback invitedContactCreationCallback = new ViewCreationCallback<PendingContact>() {
        @Override
        public void run(View view, PendingContact p) {
            PendingContactItem item = (PendingContactItem) view;
            final PendingContact contact = item.getPendingContact();

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
                    listener.openContactOptionDialog(contact);
                    return true;
                }
            });
        }
    };
}
