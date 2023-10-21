package com.socialcodia.famblah.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.contentcapture.DataRemovalRequest;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.activity.MainActivity;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ViewStatusActivity extends AppCompatActivity {

    private TextView tvStatusContent, tvUserName, tvStatusTimestamp, tvStatusViewsCount;
    private EditText inputStatusReply;
    private ImageView userProfileImage,statusImage;
    FloatingActionButton btnSendReply;

    String userId,userName;

    //Firebase

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mUserRef;
    DatabaseReference mStatusRef;
    DatabaseReference mStatusSeenRef;
    FirebaseUser mUser;

    String statusId,hisUid;

    Toolbar mToolbar;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_status);

        tvStatusContent = findViewById(R.id.tvStatusContent);
        tvUserName = findViewById(R.id.tvUserName);
        tvStatusTimestamp = findViewById(R.id.tvStatusTimestamp);
        tvStatusViewsCount = findViewById(R.id.tvStatusViewsCount);
        inputStatusReply = findViewById(R.id.inputReplyStatus);
        btnSendReply = findViewById(R.id.fabReplyStatus);
        userProfileImage = findViewById(R.id.userProfileImage);
        statusImage =findViewById(R.id.statusImage);
        mToolbar = findViewById(R.id.toolbar);
        //Firebase Init

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                onBackPressed();
//            }
//        },5000);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUserRef = mDatabase.getReference("Users");
        mStatusRef = mDatabase.getReference("Status");
        mStatusSeenRef = mDatabase.getReference("Status_Seen");
        mUser = mAuth.getCurrentUser();

        if (mUser!=null)
        {
            userId = mUser.getUid();
        }
        else
        {
            sendToLogin();
        }

        Intent intent = getIntent();
        statusId = intent.getStringExtra("statusId");

        btnSendReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReply();
            }
        });

        setSupportActionBar(mToolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        getStatus(statusId);
        setStatusView();
        getStatusViewsCount();
    }

    private void sendToLogin()
    {
        Intent intent = new Intent(getApplicationContext(),PhoneLoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void sendToMainActivity()
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void getStatus(String statusId)
    {
        Query query = mStatusRef.orderByChild(Constants.STATUS_ID).equalTo(statusId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String statusImageData = ds.child(Constants.STATUS_IMAGE).getValue(String.class);
                    String statusContent = ds.child(Constants.STATUS_CONTENT).getValue(String.class);
                    String statusId = ds.child(Constants.STATUS_ID).getValue(String.class);
                    String statusTimestamp = ds.child(Constants.TIMESTAMP).getValue(String.class);
                    String statusSenderId = ds.child(Constants.STATUS_SENDER_ID).getValue(String.class);

                    //Set Data
                    if (statusContent.equals("famblah"))
                    {
                        tvStatusContent.setVisibility(View.GONE);
                    }
                    else
                    {
                        tvStatusContent.setText(statusContent);
                    }

                    try
                    {
                        Picasso.get().load(statusImageData).into(statusImage);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(ViewStatusActivity.this, "Oops! Failed to load status Image", Toast.LENGTH_SHORT).show();
                    }

                    if (userId.equals(statusSenderId))
                    {
                        inputStatusReply.setVisibility(View.GONE);
                        btnSendReply.setVisibility(View.GONE);
                        tvStatusViewsCount.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        inputStatusReply.setVisibility(View.VISIBLE);
                        btnSendReply.setVisibility(View.VISIBLE);
                        tvStatusViewsCount.setVisibility(View.GONE);
                    }

                    tvStatusTimestamp.setText(getTime(statusTimestamp));

                    getStatusSenderInfo(statusSenderId);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setStatusView()
    {
        Query query = mStatusRef.orderByChild(Constants.STATUS_ID).equalTo(statusId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String statusSender = ds.child(Constants.STATUS_SENDER_ID).getValue(String.class);
                    if (statusSender.equals(userId))
                    {
                        Toast.makeText(ViewStatusActivity.this, "Welcome to your status stats", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        setStatusSeen();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setStatusSeen()
    {
        mStatusRef.child(statusId).child("seen").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {

                }
                else
                {
                    HashMap<String,Object> map = new HashMap<>();
                    map.put(Constants.USER_ID,userId);
                    map.put(Constants.STATUS_ID,statusId);
                    map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
                    mStatusRef.child(statusId).child("seen").child(userId).setValue(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getStatusSenderInfo(String statusSenderId)
    {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = mRef.orderByChild(Constants.USER_ID).equalTo(statusSenderId);
                query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    userName = ds.child(Constants.USER_NAME).getValue(String.class);
                    String userImage = ds.child(Constants.USER_IMAGE).getValue(String.class);
                    hisUid = ds.child(Constants.USER_ID).getValue(String.class);
                    tvUserName.setText(userName);
                    try {
                        Picasso.get().load(userImage).into(userProfileImage);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.person_male).into(userProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        })
;
    }

    private String getTime(String timestamp)
    {
        Long ts = Long.parseLong(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:a");
        String time = sdf.format(new Date(ts));
        return time;
    }

    private void getStatusViewsCount()
    {
        mStatusRef.child(statusId).child("seen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String viewsCount = String.valueOf(dataSnapshot.getChildrenCount());
                    tvStatusViewsCount.setText(viewsCount+" views");
                }
                else
                {
                    tvStatusViewsCount.setText("No views");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendReply()
    {
        String replyMessage = inputStatusReply.getText().toString().trim();
        if (replyMessage.isEmpty())
        {
            Toast.makeText(this, "Can't Send Empty Message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            DatabaseReference chatsRef = mDatabase.getReference("Chats");
            String messageId = chatsRef.push().getKey();
            HashMap<String,Object> map= new HashMap<>();
            map.put(Constants.CHAT_MESSAGE,replyMessage);
            map.put(Constants.CHAT_STATUS,1);
            map.put(Constants.CHAT_SENDER_ID,userId);
            map.put(Constants.CHAT_RECEIVER_ID,hisUid);
            map.put(Constants.CHAT_MESSAGE_ID,messageId);
            map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
            map.put(Constants.CHAT_TYPE,"text");
            chatsRef.child(messageId).setValue(map);
            inputStatusReply.setText("");
            Toast.makeText(this, "Reply has been sent", Toast.LENGTH_SHORT).show();
        }
    }

}
