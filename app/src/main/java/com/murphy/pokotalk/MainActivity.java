package com.murphy.pokotalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.murphy.pokotalk.Constants.RequestCode;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private int[] layouts = {R.layout.contact_list_layout, R.layout.group_list_layout,
            R.layout.event_list_layout};
    private MpagerAdapter mPagerAdapter;

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

        /* Start view pager(contact, group, event, configuration menu) */
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        mPagerAdapter = new MpagerAdapter(this, layouts);
        viewPager.setAdapter(mPagerAdapter);

        /* If application has no session id to login, show login activity */
        Session session = Session.getInstance();
        if (!session.sessionIdExists()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, RequestCode.LOGIN.value);
        }

        PokoServer server = PokoServer.getInstance(this);

        /* Attach event callbacks */
        server.attachActivityCallback(Constants.getContactListName, getContactListCallback);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.LOGIN.value)
            handleLoginResult(resultCode, data);
    }

    private void handleLoginResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

        } else if (resultCode == RESULT_CANCELED) {
            finish();
        } else {
            finish();
        }

    }

    private ActivityCallback getContactListCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {

        }

        @Override
        public void onError(Status status, Object... args) {
            Toast.makeText(getApplicationContext(), "Failed to get contact list",
                    Toast.LENGTH_LONG).show();
        }
    };
}
