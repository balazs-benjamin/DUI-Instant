package com.dcmmoguls.offthejail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

/**
 * Created by mobile on 4/25/2017.
 */

public class MyNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {

    private MyApp application;

    private SharedPreferences sharedPref;


    public MyNotificationOpenedHandler(MyApp application) {
        this.application = application;

        sharedPref = application.getSharedPreferences("com.dcmmoguls.offthejailadmin", Context.MODE_PRIVATE);
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;
        String type = "";
        String senderId = "";
        String senderName = "";

        if (data != null) {
            type = data.optString("type", null);
            senderId = data.optString("uid", null);
            senderName = data.optString("name", null);
            if (type != null)
                Log.i("OneSignalExample", "customkey set with value: " + type);

            if (actionType == OSNotificationAction.ActionType.Opened ) {
                Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);
                Intent intent = new Intent(application, MessagesActivity.class);
                intent.putExtra("channel", FirebaseAuth.getInstance().getCurrentUser().getUid());
                intent.putExtra("chatting", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                application.startActivity(intent);

            }
        }


    }
}
