package com.dcmmoguls.offthejail;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;

import static com.dcmmoguls.offthejail.R.id.navigation_home;
import static com.dcmmoguls.offthejail.R.id.navigation_police;

public class MainActivity extends AppCompatActivity
        implements MainFragment.OnFragmentInteractionListener, NearbyFragment.OnFragmentInteractionListener, LocationFragment.OnFragmentInteractionListener, ContactsFragment.OnFragmentInteractionListener {

    private TextView tvTitle;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
    };

    public void onFragmentInteraction(Uri uri) {

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            selectFragment(item);
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        BottomNavigationViewHelper.disableShiftMode(navigation);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(navigation_home);

        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.actionbar_layout,
                null);
        actionBarLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Set up your ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);

        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.top_bar));

        if (!canAccessLocation() || !canAccessContacts() || !canSendSMS()) {
            ActivityCompat.requestPermissions(this, INITIAL_PERMS, 0);
        }

        ImageButton btnChat = (ImageButton) actionBarLayout.findViewById(R.id.btnChat);
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
                intent.putExtra("channel", FirebaseAuth.getInstance().getCurrentUser().getUid());
                intent.putExtra("chatting", true);
                startActivity(intent);
            }
        });

        ImageButton btnPush = (ImageButton) actionBarLayout.findViewById(R.id.btnNotification);
        btnPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
                intent.putExtra("chatting", false);
                startActivity(intent);
            }
        });

        tvTitle = (TextView) actionBarLayout.findViewById(R.id.tvTitle);

        tvTitle.setText("Home");
    }

    private boolean canAccessLocation() {
        return(hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) && hasPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    private boolean canAccessContacts() {
        return(hasPermission(Manifest.permission.CALL_PHONE));
    }

    private boolean canSendSMS() {
        return(hasPermission(Manifest.permission.SEND_SMS));
    }

    private boolean hasPermission(String perm) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
        } else {
            return false;
        }
    }

    private void selectFragment(MenuItem item) {
        Fragment frag = null;
        // init corresponding fragment
        switch (item.getItemId()) {
            case R.id.navigation_home:
                frag = MainFragment.newInstance(null, null);
                break;
            case R.id.navigation_bailbonds:
                frag = NearbyFragment.newInstance("bail_bonds", null);
                break;
            case R.id.navigation_police:
                frag = NearbyFragment.newInstance("police", null);
                break;
            case R.id.navigation_lawoffice:
                frag = LocationFragment.newInstance(null, null);
                break;
            case R.id.navigation_contact:
                frag = ContactsFragment.newInstance(null, null);
                break;
        }
        // update selected item
        //mSelectedItem = item.getItemId();
        /*
        // uncheck the other items.
        for (int i = 0; i< mBottomNav.getMenu().size(); i++) {
            MenuItem menuItem = mBottomNav.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == item.getItemId());
        }
        */

        updateToolbarText(item.getTitle());

        if (frag != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content, frag, frag.getTag());
            ft.commit();
        }
    }

    private void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
        if (tvTitle != null) {
            tvTitle.setText(text);
        }
    }
}
