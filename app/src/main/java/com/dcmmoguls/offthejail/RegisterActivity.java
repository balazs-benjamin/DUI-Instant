package com.dcmmoguls.offthejail;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static String TAG = "User Signin";
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String phonePattern = "^[+]?[0-9]{8,20}$";


    private EditText etName, etCity, etPhone, etEmail;
    private Button btnSubmit;

    private SharedPreferences sharedPref;

    private AVLoadingIndicatorView avLoadingIndicatorView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPref = getSharedPreferences("com.dcmmoguls.offthejail", Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avi);

        avLoadingIndicatorView.hide();

        Typeface opensansRegular = Typeface.createFromAsset(getAssets(), "fonts/opensans_regular.ttf");
        Typeface opensansSemiBold = Typeface.createFromAsset(getAssets(), "fonts/opensans_semibold.ttf");

        etName = (EditText) findViewById(R.id.etName);
        etCity = (EditText) findViewById(R.id.etCity);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etEmail = (EditText) findViewById(R.id.etEmail);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        etName.setTypeface(opensansRegular);
        etCity.setTypeface(opensansRegular);
        etPhone.setTypeface(opensansRegular);
        etEmail.setTypeface(opensansRegular);

        btnSubmit.setTypeface(opensansSemiBold);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForms())
                    register();
            }
        });

        if(sharedPref.contains("userid")) {
            Intent myIntent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(myIntent);
            finish();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    private boolean validateForms() {
        if( etName.getText().toString().length() == 0 )
            etName.setError( "Name is required!" );
        else if (!etEmail.getText().toString().matches(emailPattern))
            etEmail.setError( "Invalid email address!" );
        else if( etCity.getText().toString().length() == 0 )
            etCity.setError( "Your Address is required!" );
        else if( etPhone.getText().toString().length() == 0 )
            etPhone.setError( "Phone Number is required!" );
        else if (!etPhone.getText().toString().matches(phonePattern))
            etPhone.setError( "Invalid phone number!" );
        else if( etEmail.getText().toString().length() == 0 )
            etEmail.setError( "Email is required!" );
        else
            return true;
        return false;
    }

    private void register() {
        avLoadingIndicatorView.show();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                        avLoadingIndicatorView.hide();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else if(task.getResult().getUser() != null) {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(task.getResult().getUser().getUid());
                            HashMap<String, Object> data = new HashMap<String, Object>();
                            data.put("name", etName.getText().toString());
                            data.put("city", etCity.getText().toString());
                            data.put("phone", etPhone.getText().toString());
                            data.put("email", etEmail.getText().toString());
                            data.put("OneSignalId", sharedPref.getString("OneSignalId", ""));
                            ref.setValue(data);

                            DatabaseReference channelRef = FirebaseDatabase.getInstance().getReference().child("channels").child(task.getResult().getUser().getUid());
                            HashMap<String, Object> channelData = new HashMap<String, Object>();
                            channelData.put("name", etName.getText().toString());
                            channelData.put("city", etCity.getText().toString());
                            channelData.put("phone", etPhone.getText().toString());
                            channelData.put("email", etEmail.getText().toString());
                            channelRef.setValue(channelData);

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("userid", task.getResult().getUser().getUid());
                            editor.putString("name", etName.getText().toString());
                            editor.putString("city", etCity.getText().toString());
                            editor.putString("phone", etPhone.getText().toString());
                            editor.putString("email", etEmail.getText().toString());
                            editor.commit();

                            OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                                @Override
                                public void idsAvailable(String userId, String registrationId) {
                                    Log.d("debug", "User:" + userId);
                                    if (registrationId != null)
                                        Log.d("debug", "registrationId:" + registrationId);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("OneSignalId", userId);
                                    editor.commit();

                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users/" + uid + "/OneSignalId");
                                    ref.setValue(userId);
                                }
                            });

                            Intent myIntent = new Intent(RegisterActivity.this, AddFriendsActivity.class);
                            startActivity(myIntent);
                            finish();
                        }

                        // ...
                    }
                });
    }
}
