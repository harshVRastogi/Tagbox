package com.harsh.tagbox;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private TextInputLayout ilUserName;
    private TextInputLayout ilPassword;
    private ArrayList<Call> apiCalls = new ArrayList<>();
    public static final int GPS_REQUEST_CODE = 1223;
    public static final int GPS_ON_REQUEST_CODE = 1224;
    public static final int REQUEST_CODE_SUCCESS_ACTIVITY = 1225;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private EditText etUserName;
    private EditText etPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");

        etUserName = (EditText) findViewById(R.id.et_user_name);
        etPassword = (EditText) findViewById(R.id.et_password);

        ilUserName = (TextInputLayout) findViewById(R.id.il_user_name);
        ilPassword = (TextInputLayout) findViewById(R.id.il_password);

        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);

                if (!isValidCreds(etUserName.getText().toString(), etPassword.getText().toString())) {
                    return;
                }
                googleApiClient.connect();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (!hasGPSPermission()) {
                        askGPSPermission();
                    } else {
                        turnGPSOn();
                    }
                } else {
                    turnGPSOn();
                }

            }
        });
        initGoogleApiClient();

    }

    private void login(final String userName, String password, Location loc) {

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
                    loginSuccess(userName);
                } else {
                    handleApiFailure(resCode);
                }
                removeCall(call);
            }
        };

        apiCalls.add(ApiController.login(userName, password, responseCallback, loc));

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

    private void loginSuccess(String userName) {
        Intent i = new Intent(this, Activity.class);
        i.putExtra("user_name", userName);
        startActivityForResult(i, REQUEST_CODE_SUCCESS_ACTIVITY);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(final Location location) {
        showProgress(new Runnable() {
            @Override
            public void run() {
                login(etUserName.getText().toString(), etPassword.getText().toString(), location);
            }
        });
        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopLocationUpdates();
    }

    private void initGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setFastestInterval(10000);
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private boolean hasGPSPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askGPSPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                GPS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == GPS_REQUEST_CODE) {
                turnGPSOn();
            }
        }
    }

    private void turnGPSOn() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> pendingResult = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());

        pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(LoginActivity.this, GPS_ON_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } catch (SecurityException e) {
            e.getLocalizedMessage();
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK ) {
           switch (requestCode){
               case GPS_ON_REQUEST_CODE:
                   googleApiClient.connect();
                   break;
               case REQUEST_CODE_SUCCESS_ACTIVITY:
                   finish();
                   break;
           }
        }
    }

}

