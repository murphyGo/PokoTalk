package com.murphy.pokotalk.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.MemberCandidateListAdapter;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.UserList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.view.MemberCandidateItem;
import com.murphy.pokotalk.view.MemberSelectedItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GroupMemberInvitationActivity extends AppCompatActivity {
    private Group group;
    private LinearLayout selectedMembersLayout;
    private ListView candidateListView;
    private Button inviteMemberButton;
    private Button backsapceButton;
    private MemberCandidateListAdapter candidateListAdapter;
    private HashSet<Contact> memberSet;
    private HashMap<Integer, MemberSelectedItem> selectedViewSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_member_invite_layout);

        /* Get groupId and group */
        Intent intent = getIntent();
        if (intent == null) {
            wrongAccess();
        }

        int groupId = intent.getIntExtra("groupId", -1);
        if (groupId < 0) {
            wrongAccess();
        }

        group = DataCollection.getInstance().getGroupList().getItemByKey(groupId);
        if (group == null) {
            wrongAccess();
        }

        memberSet = new HashSet<>();
        selectedViewSet = new HashMap<>();

        selectedMembersLayout = findViewById(R.id.selectedMemberList);
        candidateListView = findViewById(R.id.memberCandidateList);
        inviteMemberButton = findViewById(R.id.inviteMemberButton);
        backsapceButton = findViewById(R.id.backspaceButton);

        /* Filter contact that is already a member */
        ArrayList<Contact> contactList = DataCollection.getInstance().getContactList().getList();
        ContactList nonMemberContactList = new ContactList();
        UserList groupMemberList = group.getMembers();
        for (Contact contact : contactList) {
            if (groupMemberList.getItemByKey(groupMemberList.getKey(contact)) == null) {
                nonMemberContactList.add(contact);
            }
        }

        /* Create adapter and set to ListView */
        try {
            DataLock.getInstance().acquireWriteLock();

            candidateListAdapter = new MemberCandidateListAdapter(this);
            candidateListAdapter.setViewCreationCallback(candidateCreationCallback);
            ContactList contactListUI = (ContactList) candidateListAdapter.getPokoList();
            contactListUI.copyFromPokoList(nonMemberContactList);
            candidateListView.setAdapter(candidateListAdapter);

            DataLock.getInstance().releaseWriteLock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Button listeners */
        inviteMemberButton.setOnClickListener(inviteButtonClickListener);
        backsapceButton.setOnClickListener(backspaceClickListener);
    }

    private ViewCreationCallback candidateCreationCallback = new ViewCreationCallback() {
        @Override
        public void run(View view) {
            final MemberCandidateItem candidateItem = (MemberCandidateItem) view;

            candidateItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Contact contact = candidateItem.getContact();
                    if (!memberSet.contains(contact)) {
                        selectCandidate(candidateItem);
                    } else {
                        removeCandidate(candidateItem);
                    }
                }
            });
        }
    };

    private View.OnClickListener inviteButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /* Send invite message and return to chat */
            ArrayList<Contact> selectedContacts = new ArrayList<>();
            selectedContacts.addAll(memberSet);
            PokoServer server = PokoServer.getInstance(getApplicationContext());
            ArrayList<String> emails = new ArrayList<>();
            for (Contact contact : selectedContacts) {
                emails.add(contact.getEmail());
            }
            server.sendInviteGroupMembers(group.getGroupId(), emails);
            setResult(RESULT_OK);
            finish();
        }
    };

    private View.OnClickListener backspaceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setResult(RESULT_CANCELED);
            finish();
        }
    };

    private void wrongAccess() {
        Toast.makeText(getApplicationContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT)
                .show();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void selectCandidate(final MemberCandidateItem candidateItem) {
        /* Add contact to the list */
        final Contact contact = candidateItem.getContact();
        memberSet.add(contact);

        /* Create selected member view */
        MemberSelectedItem selectedItem = new MemberSelectedItem(getApplicationContext());
        selectedItem.inflate();
        selectedItem.setContact(contact);
        selectedViewSet.put(contact.getUserId(), selectedItem);
        /* Add click listener to remove button */
        Button removeButton = selectedItem.getRemoveButton();
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCandidate(candidateItem);
            }
        });
        /* Add to linear layout */
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        selectedMembersLayout.addView(selectedItem, params);
    }

    private void removeCandidate(final MemberCandidateItem candidateItem) {
        /* Remove contact from list */
        final Contact contact = candidateItem.getContact();
        memberSet.remove(contact);

        /* Remove selected view from linear layout */
        MemberSelectedItem selectedView = selectedViewSet.get(contact.getUserId());
        selectedMembersLayout.removeView(selectedView);
    }
}
