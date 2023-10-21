package com.socialcodia.famblah.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.adapter.AdapterChat;
import com.socialcodia.famblah.model.ModelChat;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    List<ModelChat> modelChatList;
    String hisUid;
    String userId;
    Uri filePath;

    Intent intent;

    Toolbar toolbar;
    ActionBar actionBar;
    RecyclerView chatRecyclerView;
    ConstraintLayout chatConstraintLayout;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    DatabaseReference mChatListRef;
    DatabaseReference mChatListRef1;
    FirebaseUser mUser;

    private EditText inputMessage;
    private TextView tvUserName, tvUserStatus;
    private ImageView ivSendMessage,userProfileImage, ivAttachFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //Init

        inputMessage = findViewById(R.id.inputMessage);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserStatus = findViewById(R.id.userStatus);
        userProfileImage = findViewById(R.id.userProfileImage);
        ivSendMessage = findViewById(R.id.ivSendMessage);
        ivAttachFile = findViewById(R.id.ivAttachFile);
        chatConstraintLayout = findViewById(R.id.toolbarConstraintLayout);

        toolbar = findViewById(R.id.toolbar);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);


        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Users");
        mChatListRef = mDatabase.getReference("ChatList");
        mChatListRef1 = mDatabase.getReference("ChatList");

        //Validate Current User

        if (mAuth.getCurrentUser()!=null)
        {
            userId = mAuth.getCurrentUser().getUid();
        }
        else
        {
            sendToLogin();
        }

        intent = getIntent();

        hisUid = intent.getStringExtra("hisUid");

        //On click listener to attach the file to send message;

        ivAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //set toolbar
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        //On click listener at button to send message
        ivSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateMessage();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        chatRecyclerView.setLayoutManager(layoutManager);

        //Typing status

        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length()==0)
                {
                    checkTypingStatus("fambluh");
                }
                else
                {
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        checkTypingStatus("fambluh");
                    }
                },6000);

            }
        });

        chatConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userId = FirebaseAuth.getInstance().getUid();
                sendToUserInfoActivity(userId);
            }
        });

        getUserDetails();
        getMessage();
        setChatList();
    }

    private void sendToUserInfoActivity(String userId)
    {
        Intent intent = new Intent(getApplicationContext(),UserInfoActivity.class);
        intent.putExtra("userId",hisUid);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100 && resultCode==RESULT_OK)
        {
            filePath = data.getData();
            uploadChatImage(filePath);
        }
    }

    private void uploadChatImage(Uri filePath)
    {
        String pathAndName = "famblah_"+System.currentTimeMillis();
        StorageReference chatImageUploadRef = FirebaseStorage.getInstance().getReference("Chats");
        chatImageUploadRef.child(pathAndName).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadImageUrl = uri.toString();
                            sendImageMessage(downloadImageUrl);
                        }
                    });
                }
            }
        });
    }

    private void sendImageMessage(String downloadImageUrl)
    {

        DatabaseReference chatsRef = mDatabase.getReference("Chats");
        HashMap<String,Object> map= new HashMap<>();
        map.put(Constants.CHAT_IMAGE,downloadImageUrl);
        map.put(Constants.CHAT_STATUS,1);
        map.put(Constants.CHAT_SENDER_ID,userId);
        map.put(Constants.CHAT_RECEIVER_ID,hisUid);
        map.put(Constants.CHAT_MESSAGE_ID,chatsRef.push().getKey());
        map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
        map.put(Constants.CHAT_TYPE,"image");
        chatsRef.push().setValue(map);
        inputMessage.setText("");
    }

    private void checkTypingStatus(String status)
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.TYPING_STATUS,status);
        mRef.child(userId).updateChildren(map);
    }

    private void sendToLogin()
    {
        Intent intent = new Intent(getApplicationContext(),PhoneLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void ValidateMessage() 
    {
        String message = inputMessage.getText().toString().trim();
        if (message.isEmpty())
        {
            inputMessage.setError("Can't Send Empty Message");
            inputMessage.requestFocus();
        }
        else 
        {
            SendMessage(message);
        }
    }

    private void SendMessage(String message)
    {
        DatabaseReference chatsRef = mDatabase.getReference("Chats");
        HashMap<String,Object> map= new HashMap<>();
        map.put(Constants.CHAT_MESSAGE,message);
        map.put(Constants.CHAT_STATUS,1);
        map.put(Constants.CHAT_SENDER_ID,userId);
        map.put(Constants.CHAT_RECEIVER_ID,hisUid);
        map.put(Constants.CHAT_MESSAGE_ID,chatsRef.push().getKey());
        map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
        map.put(Constants.CHAT_TYPE,"text");
        chatsRef.push().setValue(map);
        inputMessage.setText("");
    }

    private void getUserDetails()
    {
        mRef.orderByChild(Constants.USER_ID).equalTo(hisUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String name = ds.child(Constants.USER_NAME).getValue(String.class);
                    String userStatus = ds.child(Constants.USER_STATUS).getValue(String.class);
                    String image = ds.child(Constants.USER_IMAGE).getValue(String.class);
                    String typingStatus = ds.child(Constants.TYPING_STATUS).getValue(String.class);

                    if (typingStatus.equals(userId))
                    {
                        tvUserStatus.setText("Typing...");
                    }
                    else
                    {
                        if (userStatus.equals("online"))
                        {
                            tvUserStatus.setText("Online");
                        }
                        else {
                            tvUserStatus.setText("Last seen "+getTime(userStatus));
                        }
                    }

                    tvUserName.setText(name);
                    try {
                        Picasso.get().load(image).into(userProfileImage);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.person_female).into(userProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getMessage()
    {
        modelChatList = new ArrayList<>();
        DatabaseReference chatRef = mDatabase.getReference("Chats");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelChatList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat.getSender().equals(userId) && modelChat.getReceiver().equals(hisUid) ||
                        modelChat.getReceiver().equals(userId) && modelChat.getSender().equals(hisUid)
                    )
                    {
                        modelChatList.add(modelChat);
                    }

                    AdapterChat adapterChat = new AdapterChat(getApplicationContext(),modelChatList);
                    chatRecyclerView.scrollToPosition(modelChatList.size() - 1);
                    chatRecyclerView.setAdapter(adapterChat);
                    adapterChat.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setUserStatus("online");

    }

    @Override
    protected void onPause()
    {
        setUserStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }

    private void setUserStatus(String status)
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.USER_STATUS,status);
        mRef.child(userId).updateChildren(map);
    }

    private String getTime(String timestamp)
    {
        Long ts = Long.valueOf(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:a");
        String time = sdf.format(new Date(ts));
        return time;
    }

    private void setChatList()
    {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(userId)
                .child(hisUid);
        final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(hisUid)
                .child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                {
                    databaseReference.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.exists())
                {
                    databaseReference1.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
