package com.dcmmoguls.offthejail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private String phonePattern = "^[+]?[0-9]{8,20}$";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private SharedPreferences sharedPref;

    private EditText etName1, etPhone1, etEmail1;
    private Button btnSave;

    public ContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
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
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        sharedPref = getActivity().getSharedPreferences("com.dcmmoguls.offthejail", Context.MODE_PRIVATE);

        btnSave = (Button) view.findViewById(R.id.btnSave);

        etName1 = (EditText) view.findViewById(R.id.etName1);

        etPhone1 = (EditText) view.findViewById(R.id.etPhone1);

        etEmail1 = (EditText) view.findViewById(R.id.etEmail1);

        etName1.setText(sharedPref.getString("name1", ""));

        etEmail1.setText(sharedPref.getString("email1", ""));

        etPhone1.setText(sharedPref.getString("phone1", ""));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });

        return view;
    }

    private void onSave() {
        if(!validateForms())
            return;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference ref = database.getReference("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).child("contacts");
            HashMap<String, String> contacts = new HashMap<String, String>();
            contacts.put("name1", etName1.getText().toString());

            contacts.put("phone1", etPhone1.getText().toString());

            contacts.put("email1", etEmail1.getText().toString());
            ref.setValue(contacts);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("name1", etName1.getText().toString());

            editor.putString("phone1", etPhone1.getText().toString());

            editor.putString("email1", etEmail1.getText().toString());
            editor.commit();
        }
    }

    private boolean validateForms() {
        if (etName1.getText().toString().isEmpty()) {
            etName1.setError( "Family/Friend's name is required!" );
            return false;
        }
        if (etPhone1.getText().toString().isEmpty()) {
            etPhone1.setError( "Family/Friend's phone number is required!" );
            return false;
        }
        if (etEmail1.getText().toString().isEmpty()) {
            etEmail1.setError( "Family/Friend's email is required!" );
            return false;
        }

        if (etEmail1.getText().toString().length() > 0 && !etEmail1.getText().toString().matches(emailPattern)) {
            etEmail1.setError("Invalid email address!");
            return false;
        }

        if (etPhone1.getText().toString().length() > 0 && !etPhone1.getText().toString().matches(phonePattern)) {
            etPhone1.setError( "Invalid phone number!" );
            return false;
        }
        return true;
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
