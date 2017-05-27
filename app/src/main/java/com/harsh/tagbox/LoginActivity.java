package com.harsh.tagbox;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout ilUserName;
    private TextInputLayout ilPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Login");

        final EditText etUserName = (EditText) findViewById(R.id.et_user_name);
        final EditText etPassword = (EditText) findViewById(R.id.et_password);

        ilUserName = (TextInputLayout) findViewById(R.id.il_user_name);
        ilPassword = (TextInputLayout) findViewById(R.id.il_password);

        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(etUserName.getText().toString(), etPassword.getText().toString());
            }
        });
    }

    private void login(String userName, String password) {

        if (!isValidCreds(userName, password)) {
            return;
        }


    }

    private boolean isValidCreds(String userName, String password) {
        ilUserName.setError("");
        ilPassword.setError("");
        boolean valid = true;
        if (TextUtils.isEmpty(userName)) {
            valid = false;
            ilUserName.setError(getString(R.string.err_blank_user_name));
        }

        if (TextUtils.isEmpty(password)) {
            valid = false;
            ilPassword.setError(getString(R.string.err_blank_password));
        }
        return valid;
    }
}

