package com.dcmmoguls.offthejail;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dcmmoguls.offthejail.model.Message;
import com.dcmmoguls.offthejail.model.MyMessage;
import com.dcmmoguls.offthejail.model.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 * Created by troy379 on 04.04.17.
 */
public class MessagesActivity extends AppCompatActivity
        implements MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener,
        MessageInput.InputListener,
        MessageInput.AttachmentsListener{

    private static final int TOTAL_MESSAGES_COUNT = 100;

    protected final String senderId = "0";
    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;

    private Menu menu;
    private int selectionCount;
    private Date lastLoadedDate;

    private DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("admin");
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference channelRef = FirebaseDatabase.getInstance().getReference().child("channels");
    private DatabaseReference messageRef = channelRef.child("messages");

    public static FirebaseStorage storage = FirebaseStorage.getInstance();
    public static StorageReference storageRef = storage.getReference();


    private MessagesList messagesList;

    private SharedPreferences sharedPref;
    private String uid;
    private String uname;
    public static String mCurrentPhotoPath = null;
    public static Uri photoURI = null;
    static final int REQUEST_TAKE_PHOTO = 1;

    private Boolean isChatting;
    private List<String> receiversSingalIds = new ArrayList<String>();

    private String channelID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_messages);

        sharedPref = getSharedPreferences("com.dcmmoguls.offthejail", Context.MODE_PRIVATE);
        uid = sharedPref.getString("userid", "");
        uname = sharedPref.getString("name", "exam");

        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.chat_actionbar,
                null);
        actionBarLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        TextView tvTitle = (TextView) actionBarLayout.findViewById(R.id.tvTitle);


        // Set up your ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);

        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.topbarback));

        MessageInput input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
        input.setAttachmentsListener(this);


        isChatting = getIntent().getBooleanExtra("chatting", true);
        if(isChatting)
        {
            channelID = getIntent().getStringExtra("channel");
            channelRef = channelRef.child(channelID);
            messageRef = channelRef.child("messages");
            tvTitle.setText("Messaging");
        } else {
            channelRef = FirebaseDatabase.getInstance().getReference().child("notifications");
            messageRef = channelRef.child("messages");

            input.setVisibility(View.GONE);

            tvTitle.setText("Notifications");
        }


        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(final ImageView imageView, String url) {
                //Picasso.with(MessagesActivity.this).load(url).into(imageView);

                StorageReference gsReference = storage.getReferenceFromUrl(url);

                Glide.with(MessagesActivity.this)
                        .using(new FirebaseImageLoader())
                        .load(gsReference)
                        .into(imageView);
            }
        };

        messagesAdapter = new MessagesListAdapter<>("0", imageLoader);

        this.messagesList = (MessagesList) findViewById(R.id.messagesList);
        initAdapter();

        ImageButton btnback = (ImageButton) findViewById(R.id.btnBack);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        observeUsers();
    }

    private void observeUsers() {
        adminRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                String strOneSignalId = (String) dataSnapshot.child("OneSignalId").getValue();

                if(isChatting) {
                    receiversSingalIds.add(strOneSignalId);
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            }
        }
    }

    public boolean onSubmit(CharSequence input) {
        //messagesAdapter.addToStart(MessagesFixtures.getTextMessage(input.toString()), true);
        Date d = new Date();
        CharSequence s  = DateFormat.format("yyyy-MM-dd HH:mm:ss", d.getTime());

        messageRef.push().setValue(new MyMessage(uid, uname, input.toString(), null, s.toString()));
        sendPush(input.toString());
        return true;
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
            if(isChatting)
                data.put("type", "chat");
            else
                data.put("type", "notification");
            JSONObject contents = new JSONObject();
            if(isChatting)
                contents.put("en", uname + ": " + text );
            else
                contents.put("en", text );
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

    @Override
    public void onAddAttachments() {
        // messagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.dcmmoguls.hardinglaw.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                MessagesActivity.this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private Bitmap getPic(Uri uri){
        if(uri == null)
            return null;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        // Determine how much to scale down the image
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        ExifInterface exif;
        try {
            exif = new ExifInterface(mCurrentPhotoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

            Matrix matrix = new Matrix();
            matrix.setRotate(rotationAngle);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return rotatedBitmap;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK && photoURI != null) {
            Bitmap bitmap = getPic(photoURI);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] bitdata = baos.toByteArray();

            Calendar calendar = Calendar.getInstance();
            long startTime = calendar.getTimeInMillis();
            final StorageReference gsReference = storageRef.child(uid + "/" + startTime);

            UploadTask uploadTask = gsReference.putBytes(bitdata);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    sendPhoto(gsReference.toString());
                }
            });
        }
    }

    private void sendPhoto(String url) {
        Date d = new Date();
        CharSequence s  = DateFormat.format("yyyy-MM-dd HH:mm:ss", d.getTime());

        messageRef.push().setValue(new MyMessage(uid, uname, null, url, s.toString()));
        sendPush("Photo message");
    }

    private void initAdapter() {
        messagesAdapter = new MessagesListAdapter<>(uid, imageLoader);
        messagesAdapter.enableSelectionMode(this);
        //messagesAdapter.setLoadMoreListener(this);
        this.messagesList.setAdapter(messagesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadMessages();
        //messagesAdapter.addToStart(MessagesFixtures.getTextMessage(), true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        finish();
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.chat_actions_menu, menu);
        onSelectionChanged(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                messagesAdapter.deleteSelectedMessages();
                break;
            case R.id.action_copy:
                messagesAdapter.copySelectedMessagesText(this, getMessageStringFormatter(), true);
                AppUtils.showToast(this, R.string.copied_message, true);
                break;
        }
        return true;
    }
*/
    @Override
    public void onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed();
        } else {
            messagesAdapter.unselectAllItems();
        }
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
            loadMessages();
        }
    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
        //menu.findItem(R.id.action_delete).setVisible(count > 0);
        //menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

    protected void loadMessages() {
        /*
        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                //ArrayList<Message> messages = MessagesFixtures.getMessages(lastLoadedDate);
                //lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
                //messagesAdapter.addToEnd(messages, false);
            }
        }, 1000);
        */

        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MyMessage mymessage = dataSnapshot.getValue(MyMessage.class);
                User sender = new User(mymessage.senderId, mymessage.senderName, null, true);
                Message message = new Message(mymessage.senderId, sender, mymessage.text);
                if(mymessage.photoURL != null)
                    message.image = new Message.Image(mymessage.photoURL);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    if(mymessage.createdAt != null) {
                        Date date = format.parse(mymessage.createdAt);
                        message.setCreatedAt(date);
                    }
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                messagesAdapter.addToStart(message, true);
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

    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<Message>() {
            @Override
            public String format(Message message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) text = "[attachment]";

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }
}
