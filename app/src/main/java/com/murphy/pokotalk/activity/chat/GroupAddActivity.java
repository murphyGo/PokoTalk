package com.murphy.pokotalk.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.group.MemberCandidateListAdapter;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactPokoList;
import com.murphy.pokotalk.view.MemberCandidateItem;

import java.util.ArrayList;

public class GroupAddActivity extends AppCompatActivity {
    private EditText groupNameView;
    private ListView memberCandidateListView;
    private Button backspaceButton;
    private Button createGroupButton;
    private MemberCandidateListAdapter adapter;
    private ArrayList<Contact> members;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_add_dialog);

        members = new ArrayList<>();

        groupNameView = findViewById(R.id.groupName);
        memberCandidateListView = findViewById(R.id.memberCandidateList);
        backspaceButton = findViewById(R.id.backspaceButton);
        createGroupButton = findViewById(R.id.createGroupButton);

        createGroupButton.setOnClickListener(createGroupButtonListener);
        backspaceButton.setOnClickListener(backspaceButtonListener);

        try {
            DataLock.getInstance().acquireWriteLock();

            try {
                DataCollection collection = DataCollection.getInstance();
                adapter = new MemberCandidateListAdapter(this);
                adapter.setViewCreationCallback(candidateCallback);
                ContactPokoList contactListUI = (ContactPokoList) adapter.getPokoList();
                contactListUI.copyFromPokoList(collection.getContactList());
                memberCandidateListView.setAdapter(adapter);
            } finally {
                DataLock.getInstance().releaseWriteLock();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ViewCreationCallback candidateCallback = new ViewCreationCallback<Contact>() {
        @Override
        public void run(View view, Contact c) {
            final MemberCandidateItem candidateView = (MemberCandidateItem) view;
            final Contact contact = candidateView.getContact();

            candidateView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (candidateView.toggle()) {
                        members.add(contact);
                    } else {
                        members.remove(contact);
                    }
                }
            });
        }
    };

    private View.OnClickListener createGroupButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String groupName = groupNameView.getText().toString().trim();
            if (groupName.length() == 0) {
                groupName = null;
            }

            ArrayList<String> emails = new ArrayList<>();
            for (Contact contact : members) {
                emails.add(contact.getEmail());
            }
            Intent intent = new Intent();
            intent.putExtra("groupName", groupName);
            intent.putExtra("emails", emails);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private View.OnClickListener backspaceButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setResult(RESULT_CANCELED);
            finish();
        }
    };
}
