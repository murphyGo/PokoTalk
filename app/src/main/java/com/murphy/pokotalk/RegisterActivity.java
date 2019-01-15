package com.murphy.pokotalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    private Button registerButton;
    private EditText nameText, emailText, passwordText, passwordText2;
    private PokoServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerButton = (Button) findViewById(R.id.registerButton);
        nameText = (EditText) findViewById(R.id.name);
        emailText = (EditText) findViewById(R.id.email);
        passwordText = (EditText) findViewById(R.id.password);
        passwordText2 = (EditText) findViewById(R.id.passwordCheck);

        server = PokoServer.getInstance(getApplicationContext());

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameText.getText().toString().trim();
                String email = emailText.getText().toString().trim();
                String password = passwordText.getText().toString();
                String password2 = passwordText2.getText().toString();

                if (!checkDataValid(name, email, password, password2)) {
                    return;
                }

                server.sendAccountRegister(name, email, password);
            }
        });

        server.attachActivityCallback(Constants.accountRegisteredName, new ActivityCallback() {
            @Override
            public void onSuccess(Status status, Object... args) {

                final JSONObject data = (JSONObject) args[0];

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "가입하셨습니다! 로그인 해주세요.",
                                Toast.LENGTH_LONG).show();
                    }
                });

                /* Go back to LoginActivity */
                Intent intent = new Intent();
                intent.putExtra("status", "ok");

                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onError(Status status, Object... args) {
                // show message
                Toast.makeText(getApplicationContext(),
                        "가입에 실패하였습니다. 다시 시도해주세요", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isEmail(String email) {
        return (!TextUtils.isEmpty(email) &&
                email.matches("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$"));
    }

    private boolean isValidPassword(String password) {
        /* Password must be at least 8 characters */
        if (password.length() <= 7)
            return false;

        /* Password must contain at least one number, special character and alphabet */
        if (!password.matches(".*[0123456789].*") ||
                !password.matches(".*[$&~`+{},:;=\\\\?@#|/'<>.^*()%!-].*") ||
                !password.matches((".*[a-zA-Z].*")))
            return false;

        return true;
    }

    private boolean checkDataValid(String name, String email, String password, String password2) {
        if (name.length() <= 0) {
            Toast.makeText(getApplicationContext(), "이름을 잘 입력해 주세요",
                    Toast.LENGTH_LONG).show();
            nameText.requestFocus();
            return false;
        } else if (email.length() <= 0 || !isEmail(email)) {
            Toast.makeText(getApplicationContext(), "이메일을 잘 입력해 주세요",
                    Toast.LENGTH_LONG).show();
            emailText.requestFocus();
            return false;
        } else if (!isValidPassword(password)) {
            Toast.makeText(getApplicationContext(), "비밀번호를 잘 입력해 주세요",
                    Toast.LENGTH_LONG).show();
            passwordText.requestFocus();
            return false;
        } else if (!password.equals(password2)) {
            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.\n" +
                            "다시 확인해 주세요.",
                    Toast.LENGTH_LONG).show();
            passwordText2.requestFocus();
            return false;
        }

        return true;
    }
}
