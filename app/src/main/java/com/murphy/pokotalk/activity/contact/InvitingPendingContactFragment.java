package com.murphy.pokotalk.activity.contact;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.adapter.contact.PendingContactListAdapter;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.PokoLock;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.view.PendingContactItem;

public class InvitingPendingContactFragment extends Fragment {
    private PokoServer server;
    private ListView listView;
    private PendingContactList invitingList;
    private PendingContactListAdapter invitingListAdapter;
    private PendingContactList pendingContactList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pending_contact_fragment, container, false);

        // Find list view
        listView = view.findViewById(R.id.pendingContactListView);

        // Get collection and contact lists
        DataCollection data = DataCollection.getInstance();
        invitingList = data.getInvitingContactList();

        // Create adapter and set to ListView */
        try {
            PokoLock.getDataLockInstance().acquireWriteLock();

            try {
                invitingListAdapter = new PendingContactListAdapter(getContext());
                invitingListAdapter.setViewCreationCallback(invitingContactCreationCallback);
                PendingContactList invitedListUI = (PendingContactList) invitingListAdapter.getPokoList();
                invitedListUI.copyFromPokoList(invitingList);
                listView.setAdapter(invitingListAdapter);
                invitingListAdapter.setInvited(false);
                pendingContactList = (PendingContactList) invitingListAdapter.getPokoList();
            } finally {
                PokoLock.getDataLockInstance().releaseWriteLock();
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
                    pendingContactList.copyFromPokoList(invitingList);
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
            final Boolean invited = (Boolean) getData("invited");
            final PendingContact pendingContact = (PendingContact) getData("pendingContact");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (invited == null || pendingContact == null) {
                        return;
                    }
                    if (!invited) {
                        pendingContactList.updateItem(pendingContact);
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
                    invitingListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    // View creation callback
    private ViewCreationCallback invitingContactCreationCallback =
            new ViewCreationCallback<PendingContact>() {
        @Override
        public void run(View view, PendingContact p) {
            PendingContactItem item = (PendingContactItem) view;
            PendingContact contact = item.getPendingContact();
        }
    };
}
