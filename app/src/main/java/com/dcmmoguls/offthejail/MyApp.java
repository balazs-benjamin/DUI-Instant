package com.dcmmoguls.offthejail;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by mobile on 5/15/2017.
 */

public class MyApp extends MultiDexApplication {
    SharedPreferences sharedPref;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPref = getSharedPreferences("com.dcmmoguls.offthejail", Context.MODE_PRIVATE);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationOpenedHandler(new MyNotificationOpenedHandler(this))
                .init();

        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                Log.d("debug", "User:" + userId);
                if (registrationId != null)
                    Log.d("debug", "registrationId:" + registrationId);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("OneSignalId", userId);
                editor.commit();

                if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users/" + uid + "/OneSignalId");
                    ref.setValue(userId);
                }
            }
        });

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/opensans_regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
