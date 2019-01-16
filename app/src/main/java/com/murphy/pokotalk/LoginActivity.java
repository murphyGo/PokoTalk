package com.murphy.pokotalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.murphy.pokotalk.data.Session;
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

        server = PokoServer.getInstance(this);

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

                PokoServer server = PokoServer.getInstance(getApplicationContext());
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

        server.attachActivityCallback(Constants.passwordLoginName, new ActivityCallback() {
            @Override
            public void onSuccess(Status status, Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    final String sessionId = data.getString("sessionId");

                    /* Start session */
                    Session session = Session.getInstance();
                    session.setSessionId(sessionId);
                    session.login(getApplicationContext());
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
        });

        server.attachActivityCallback(Constants.sessionLoginName, new ActivityCallback() {
            @Override
            public void onSuccess(Status status, Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    final JSONObject userInfo = data.getJSONObject("data");
                    final String sessionId = data.getString("sessionId");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "세션 로그인 성공 : "  + sessionId +
                                            "\n데이터 : " + userInfo.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                    Intent intent = new Intent();
                    intent.putExtra("login", "success");
                    setResult(RESULT_OK, intent);

                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Status status, Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "로그인 실패...",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /* Handle register results */
        if (requestCode == 0) {

        }
    }
}
