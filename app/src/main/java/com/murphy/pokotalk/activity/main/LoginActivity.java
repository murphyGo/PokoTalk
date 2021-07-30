package com.murphy.pokotalk.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.PokoTalkApp;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.db.PokoDatabaseManager;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private Button registerButton;
    private EditText emailText;
    private EditText passwordText;
    private PokoServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText = (EditText) findViewById(R.id.emailText);
        passwordText = (EditText) findViewById(R.id.pwdText);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);

        server = PokoServer.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString().trim();
                String password = passwordText.getText().toString();

                if (email.length() == 0) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (password.length() == 0) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                PokoServer server = PokoServer.getInstance();
                if (!server.isConnected()) {
                    server.connect(getApplicationContext());
                }
                server.sendPasswordLogin(email, password);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        server.attachActivityCallback(Constants.passwordLoginName, passwordLoginListener);
        server.attachActivityCallback(Constants.sessionLoginName, sessionLoginListener);
    }

    @Override
    protected void onDestroy() {
        server.detachActivityCallback(Constants.passwordLoginName, passwordLoginListener);
        server.detachActivityCallback(Constants.sessionLoginName, sessionLoginListener);

        super.onDestroy();
    }

    private ActivityCallback passwordLoginListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            try {
                final String sessionId = jsonObject.getString("sessionId");

                // Set session id
                Session session = Session.getInstance();
                session.setSessionId(sessionId);

                // Enable database job
                PokoDatabaseManager.getInstance().enable();

                // Try to login
                server.sendSessionLogin(session.getSessionId());
            } catch (JSONException e) {
                return;
            }
        }

        @Override
        public void onError(Status status, Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "이메일 또는 비밀번호가 틀립니다.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private ActivityCallback sessionLoginListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, final Object... args) {
            // Get application
            PokoTalkApp app = PokoTalkApp.getInstance();

            // Start loading application data for the user
            app.startLoadingApplicationData();

            // Make intent and put login results
            Intent intent = new Intent();
            intent.putExtra("login", "success");
            setResult(RESULT_OK, intent);

            finish();
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };
}

