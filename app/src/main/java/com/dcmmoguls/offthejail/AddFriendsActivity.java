package com.dcmmoguls.offthejail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.StringTokenizer;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddFriendsActivity extends AppCompatActivity {
    private EditText etName1, etPhone1, etEmail1, etName2, etPhone2, etEmail2, etName3, etPhone3, etEmail3;
    private Button btnSave, btnSkip;

    private SharedPreferences sharedPref;

    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private String phonePattern = "^[+]?[0-9]{8,20}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        sharedPref = getSharedPreferences("com.dcmmoguls.offthejail", Context.MODE_PRIVATE);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSkip = (Button) findViewById(R.id.btnSkip);

        etName1 = (EditText) findViewById(R.id.etName1);
        etName2 = (EditText) findViewById(R.id.etName2);
        etName3 = (EditText) findViewById(R.id.etName3);

        etPhone1 = (EditText) findViewById(R.id.etPhone1);
        etPhone2 = (EditText) findViewById(R.id.etPhone2);
        etPhone3 = (EditText) findViewById(R.id.etPhone3);

        etEmail1 = (EditText) findViewById(R.id.etEmail1);
        etEmail2 = (EditText) findViewById(R.id.etEmail2);
        etEmail3 = (EditText) findViewById(R.id.etEmail3);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFriendsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });
    }

    private void onSave() {
        if(!validateForms())
            return;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference ref = database.getReference("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).child("contacts");
            HashMap<String, String> contacts = new HashMap<String, String>();
            contacts.put("name1", etName1.getText().toString());
            contacts.put("name2", etName2.getText().toString());
            contacts.put("name3", etName3.getText().toString());

            contacts.put("phone1", etPhone1.getText().toString());
            contacts.put("phone2", etPhone2.getText().toString());
            contacts.put("phone3", etPhone3.getText().toString());

            contacts.put("email1", etEmail1.getText().toString());
            contacts.put("email2", etEmail2.getText().toString());
            contacts.put("email3", etEmail3.getText().toString());
            ref.setValue(contacts);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("name1", etName1.getText().toString());
            editor.putString("name2", etName2.getText().toString());
            editor.putString("name3", etName3.getText().toString());

            editor.putString("phone1", etPhone1.getText().toString());
            editor.putString("phone2", etPhone2.getText().toString());
            editor.putString("phone3", etPhone3.getText().toString());

            editor.putString("email1", etEmail1.getText().toString());
            editor.putString("email2", etEmail2.getText().toString());
            editor.putString("email3", etEmail3.getText().toString());
            editor.commit();

            Intent intent = new Intent(AddFriendsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean validateForms() {
        if (etEmail1.getText().toString().length() > 0 && !etEmail1.getText().toString().matches(emailPattern))
            etEmail1.setError( "Invalid email address!" );
        else if (etEmail2.getText().toString().length() > 0 && !etEmail2.getText().toString().matches(emailPattern))
            etEmail2.setError( "Invalid email address!" );
        else if (etEmail3.getText().toString().length() > 0 && !etEmail3.getText().toString().matches(emailPattern))
            etEmail3.setError( "Invalid email address!" );

        if (etPhone1.getText().toString().length() > 0 && !etPhone1.getText().toString().matches(phonePattern))
            etPhone1.setError( "Invalid phone number!" );
        else if (etPhone2.getText().toString().length() > 0 && !etPhone2.getText().toString().matches(phonePattern))
            etPhone2.setError( "Invalid phone number!" );
        else if (etPhone3.getText().toString().length() > 0 && !etPhone3.getText().toString().matches(phonePattern))
            etPhone3.setError( "Invalid phone number!" );

        else
            return true;
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
