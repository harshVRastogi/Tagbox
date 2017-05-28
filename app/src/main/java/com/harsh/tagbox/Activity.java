package com.harsh.tagbox;

import android.app.Notification;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Anjan on 5/27/2017.
 */
public class Activity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        setResult(Activity.RESULT_OK);
        TextView tv = (TextView) findViewById(R.id.text_view);
        Intent i = getIntent();
        String userName = "";
        if(i.hasExtra("user_name")){
            userName = i.getStringExtra("user_name");
            tv.setText("Hello "+userName + "!");
        }
        notifyLogin(userName);
    }

    private void notifyLogin(String userName){
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        Uri soundUri = getRingtoneUri();
        Notification.Builder notificationCompat = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Login Successful")
                .setContentText(userName)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 500});

        managerCompat.notify(1, notificationCompat.build());
    }

    private Uri getRingtoneUri() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }
}
