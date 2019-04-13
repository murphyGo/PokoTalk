package com.murphy.pokotalk.activity.contact;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.PendingContactListViewPagerAdapter;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.server.PokoServer;

public class PendingContactActivity extends AppCompatActivity
        implements ContactAddDialog.ContactAddDialogListener,
                    PendingContactOptionDialog.PendingContactOptionDialogListener,
                    InvitedPendingContactFragment.Listener {
    private PokoServer server;
    private Button addButton;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Fragment[] fragments = {new InvitedPendingContactFragment(),
            new InvitingPendingContactFragment()};
    private PendingContactListViewPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pending_contact_list_layout);

        addButton = findViewById(R.id.contactAddButton);
        viewPager = findViewById(R.id.pendingContactViewPager);
        tabLayout = findViewById(R.id.pendingContactTabLayout);

        // Create pager adapter
        pagerAdapter = new PendingContactListViewPagerAdapter(
                this, getSupportFragmentManager(), fragments);

        // Set to view pager
        viewPager.setAdapter(pagerAdapter);

        // Set view pager to tab layout
        tabLayout.setupWithViewPager(viewPager);

        // Add button listener
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContactAddDialog();
            }
        });

        // Request for pending contact list
        server = PokoServer.getInstance();
        if (server != null) {
            server.sendGetPendingContactList();
        }
    }

    /* Dialog opening functions */
    public void openContactAddDialog() {
        // Show contact email input dialog
        ContactAddDialog dialog = new ContactAddDialog();
        dialog.show(getSupportFragmentManager(), "친구 추가");
    }

    @Override
    public void openContactOptionDialog(PendingContact contact) {
        // Show invited contact option dialog
        PendingContactOptionDialog dialog = new PendingContactOptionDialog();
        dialog.setContact(contact);
        dialog.show(getSupportFragmentManager(), "친구 요청 응답 옵션");
    }

    /* Dialog listeners */
    @Override
    public void contactAdd(String email) {
        if (server != null) {
            // Send contact add request
            server.sendAddContact(email);

            // Show toast message
            Toast.makeText(this,
                    String.format(Constants.locale,
                            getString(R.string.pending_contact_add_toast),
                            email), Toast.LENGTH_SHORT).show();
        }
    }

    // Pending contact user action listener
    @Override
    public void pendingContactOptionClick(int option, PendingContact contact) {
        switch (option) {
            case PendingContactOptionDialog.ACCEPT_CONTACT:
                if (server != null) {
                    server.sendAcceptContact(contact.getEmail());
                }
                break;
            case PendingContactOptionDialog.DENY_CONTACT:
                if (server != null) {
                    server.sendDenyContact(contact.getEmail());
                }
                break;
        }
    }
}
