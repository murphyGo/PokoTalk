package com.murphy.pokotalk.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.MemberCandidateListAdapter;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.Contact;
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

        DataCollection collection = DataCollection.getInstance();
        adapter = new MemberCandidateListAdapter(this, collection.getContactList().getList());
        adapter.setViewCreationCallback(candidateCallback);
        memberCandidateListView.setAdapter(adapter);
    }

    private ViewCreationCallback candidateCallback = new ViewCreationCallback() {
        @Override
        public void run(View view) {
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
            if (members.size() == 0) {
                Toast.makeText(getApplicationContext(),
                        "친구를 1명 이상 선택해 주세요!", Toast.LENGTH_SHORT).show();
                return;
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
