package com.dcmmoguls.offthejail;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;
import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;
import android.Manifest;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String STRING = "Mr. H. Craig Skinner has been announced of your arrest. He will be in touch with you soon!\r\n\r\nThis app will track your routes from now on.\r\n\r\n";
    public static final String STRING1 = "I have been arrested. Please help me!";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Button btnEmergency;

    //private Configuration configuration;
    private SendGrid sendgrid;
    private static final String SENDGRID_APIKEY = "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";

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    private String message = "Your Friends/Family have been announced";
    private String phoneNo = "";

    private SharedPreferences sharedPref;

    private DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("admin");
    private List<String> receiversSingalIds = new ArrayList<String>();

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Typeface railwayBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/raleway_bold.ttf");

        TextView tv = (TextView) view.findViewById(R.id.textView2);
        tv.setTypeface(railwayBold);

        observeUsers();

        sharedPref = getActivity().getSharedPreferences("com.dcmmoguls.offthejail", Context.MODE_PRIVATE);

        /*
        configuration = new Configuration()
                .domain("sandboxcd35af8d255e41289e48d1af09a4381a.mailgun.org")
                .apiKey("key-6edf673700e448d29028e403b252ca3e")
                .from("Test account", "myhopewin@gmail.com");
*/

        btnEmergency = (Button) view.findViewById(R.id.btnEmergency);
        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPref.getBoolean("tracking_started", false)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Alert")
                            .setMessage("Would you stop tracking your routes?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean("tracking_started", false);
                                    editor.commit();

                                    btnEmergency.setBackground(getResources().getDrawable(R.drawable.helpme_btn));

                                    Intent intent = new Intent(getActivity(), LocationService.class);
                                    getActivity().stopService(intent);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.no,  new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })

                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                    return;
                } else {
                    new SendEmail().execute(getResources().getString(R.string.attorney_email), STRING1);
                    sendSMSMessage(getResources().getString(R.string.attorney_phone), STRING1);

                    sendPush(STRING1);

                    Date d = new Date();
                    CharSequence s  = DateFormat.format("yyyy-MM-dd HH:mm:ss", d.getTime());
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference ref = database.getReference("reports/");
                    DatabaseReference newReport = ref.push();
                    newReport.setValue(new Report(
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            sharedPref.getString("name", ""),
                            sharedPref.getString("city", ""),
                            sharedPref.getString("phone", ""),
                            sharedPref.getString("email", ""),
                            s.toString()
                    ));

                    btnEmergency.setBackground(getResources().getDrawable(R.drawable.helpme_stop_btn));

                    Intent intent = new Intent(getActivity(), LocationService.class);
                    String strReportId = newReport.getKey();
                    intent.putExtra("reportID", strReportId);
                    getActivity().startService(intent);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("tracking_started", true);
                    editor.commit();

                    new AlertDialog.Builder(getActivity())
                            .setTitle("Alert")
                            .setMessage(STRING +
                                    "Would you announce your friends/family too?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    showCustomDlg();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })

                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                    return;
                }
            }
        });

        if (sharedPref.getBoolean("tracking_started", false)) {
            btnEmergency.setBackground(getResources().getDrawable(R.drawable.helpme_stop_btn));
        }

        return view;
    }

    private void observeUsers() {
        adminRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                String strOneSignalId = (String) dataSnapshot.child("OneSignalId").getValue();
                receiversSingalIds.add(strOneSignalId);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendPush(String text) {
        JSONObject payload = new JSONObject();
        try {
            if(receiversSingalIds.size() > 0) {
                JSONArray jsArray = new JSONArray(receiversSingalIds);
                payload.put("include_player_ids", jsArray);
            }
            JSONObject data = new JSONObject();
            data.put("name", sharedPref.getString("user_name", ""));
            data.put("uid", sharedPref.getString("userid", ""));
            data.put("type", "sos");
            JSONObject contents = new JSONObject();
            contents.put("en", sharedPref.getString("name", "") + ": " + text );
            payload.put("contents", contents);
            payload.put("content-available", 1);
            payload.put("data", data);
            payload.put("ios_badgeType", "Increase");
            payload.put("ios_badgeCount", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OneSignal.postNotification(payload, new OneSignal.PostNotificationResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("Post Push", "success");
            }

            @Override
            public void onFailure(JSONObject response) {
                Log.d("Post Push", "fail");
            }
        });
    }

    private void showCustomDlg() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_dlg);
        dialog.setTitle("Choose contacts to announce");

        TextView tvNoContact = (TextView) dialog.findViewById(R.id.tvNoContact);
        final CheckBox checkBox1 = (CheckBox) dialog.findViewById(R.id.checkBox);
        final CheckBox checkBox2 = (CheckBox) dialog.findViewById(R.id.checkBox2);
        final CheckBox checkBox3 = (CheckBox) dialog.findViewById(R.id.checkBox3);

        checkBox1.setVisibility(View.GONE);
        checkBox2.setVisibility(View.GONE);
        checkBox3.setVisibility(View.GONE);
        int i =0;
        if (!sharedPref.getString("phone1", "").isEmpty() || !sharedPref.getString("email1", "").isEmpty()) {
            checkBox1.setVisibility(View.VISIBLE);
            i++;
        }
        if (!sharedPref.getString("phone2", "").isEmpty() || !sharedPref.getString("email2", "").isEmpty()) {
            checkBox2.setVisibility(View.VISIBLE);
            i++;
        }
        if (!sharedPref.getString("phone3", "").isEmpty() || !sharedPref.getString("email3", "").isEmpty()) {
            checkBox3.setVisibility(View.VISIBLE);
            i++;
        }
        if (i==0) {
            tvNoContact.setVisibility(View.VISIBLE);
            Button btnAnnounce = (Button) dialog.findViewById(R.id.btnAnnounce);
            btnAnnounce.setEnabled(false);
        }
        String contact1 = sharedPref.getString("name1", "") + "\r\n"
                + sharedPref.getString("phone1", "") + "\n"
                + sharedPref.getString("email1", "");
        String contact2 = sharedPref.getString("name2", "") + "\n"
                + sharedPref.getString("phone2", "") + "\n"
                + sharedPref.getString("email2", "");
        String contact3 = sharedPref.getString("name3", "") + "\n"
                + sharedPref.getString("phone3", "") + "\n"
                + sharedPref.getString("email3", "");
        checkBox1.setText(contact1);
        checkBox2.setText(contact2);
        checkBox3.setText(contact3);

        Button dialogButton = (Button) dialog.findViewById(R.id.btnAnnounce);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox1.isChecked()) {
                    new SendEmail().execute(sharedPref.getString("email1", ""), message);
                    sendSMSMessage(sharedPref.getString("phone1", ""), message);
                }

                if(checkBox2.isChecked()) {
                    new SendEmail().execute(sharedPref.getString("email2", ""), message);
                    sendSMSMessage(sharedPref.getString("phone2", ""), message);
                }

                if(checkBox3.isChecked()) {
                    new SendEmail().execute(sharedPref.getString("email3", ""), message);
                    sendSMSMessage(sharedPref.getString("phone3", ""), message);
                }

                dialog.dismiss();
            }
        });

        Button dialogButton2 = (Button) dialog.findViewById(R.id.btnCancel);
        // if button is clicked, close the custom dialog
        dialogButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    protected void sendSMSMessage(String phoneNumber, String strMessage) {
        phoneNo = phoneNumber;
        /*
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            //smsManager.sendTextMessage(phoneNumber, null, strMessage, null, null);
            Toast.makeText(getActivity().getApplicationContext(), "SMS sent to " + phoneNumber,
                    Toast.LENGTH_LONG).show();
        }
        */

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            //smsManager.sendTextMessage(phoneNumber, null, strMessage, null, null);
            Toast.makeText(getActivity().getApplicationContext(), "SMS sent to " + phoneNumber,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    Toast.makeText(getActivity().getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }

    private class SendEmail extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                sendgrid = new SendGrid(SENDGRID_APIKEY);

                SendGrid.Email email = new SendGrid.Email();

                // Get values from edit text to compose email
                // TODO: Validate edit texts
                email.addTo(params[0]);
                email.setFrom("pwc628@gmail.com");
                email.setSubject("subject");
                email.setText(params[1]);

                /*/ Attach image
                if (mUri != null) {
                    email.addAttachment(mAttachmentName, mAppContext.getContentResolver().openInputStream(mUri));
                }
                */

                // Send email, execute http request
                SendGrid.Response response = sendgrid.send(email);
                String mMsgResponse = response.getMessage();

                Log.d("SendAppExample", mMsgResponse);

                return mMsgResponse;

            } catch (SendGridException e) {
                Log.e("SendAppExample", e.toString());
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getActivity().getApplicationContext(), "eMail send response: " + result,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
