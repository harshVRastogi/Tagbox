package com.harsh.tagbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout ilUserName;
    private TextInputLayout ilPassword;
    private ArrayList<Call> apiCalls = new ArrayList<>();

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
                hideKeyboard(view);
                showProgress(new Runnable() {
                    @Override
                    public void run() {
                        login(etUserName.getText().toString(), etPassword.getText().toString());
                    }
                });
            }
        });
    }

    private void login(String userName, String password) {

        if (!isValidCreds(userName, password)) {
            return;
        }

        Callback responseCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                hideProgress();
                removeCall(call);
                handleApiFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                hideProgress();
                int resCode = response.code();
                if (resCode == 200) {
                    loginSuccess();
                } else {
                    handleApiFailure(resCode);
                }
                removeCall(call);
            }
        };

        apiCalls.add(ApiController.login(userName, password, responseCallback));

    }

    private void removeCall(Call call) {
        if (apiCalls.contains(call)) {
            apiCalls.remove(call);
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

    @Override
    protected void onStop() {
        super.onStop();
        for (Call apiCall : apiCalls) {
            apiCall.cancel();
        }
    }

    private void handleApiFailure(IOException e) {
        if (e instanceof ConnectException) {
            showToast(R.string.err_connect);
        } else if (e instanceof SocketException || e instanceof SocketTimeoutException) {
            showToast(R.string.err_socket);
        }
    }

    private void handleApiFailure(int code) {
        switch (code) {
            case 400:
                showToast(R.string.err_no_res);
                break;
            case 412:
                showToast(R.string.err_user_password);
                break;
            default:
                showToast(R.string.err_api_others);
                break;
        }
    }

    private void loginSuccess() {
        startActivity(new Intent(this, Activity.class));
    }

    private void showToast(final int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, getString(id), Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void hideKeyboard(final View v) {
        if (v == null) {
            return;
        }
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) v.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
    }

    private static ProgressFragment progressFragment;

    private void showProgress(final Runnable task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressFragment == null) {
                    progressFragment = ProgressFragment.newInstance();
                    progressFragment.setRetainInstance(false);
                }
                progressFragment.setDialogStateListener(new ProgressFragment.DialogStateListener() {
                    @Override
                    public void onResume() {
                        task.run();
                    }
                });
                progressFragment.showProgress(getSupportFragmentManager());
            }
        });
    }

    private void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressFragment == null) {
                    return;
                }
                progressFragment.dismiss();
            }
        });
    }

}

