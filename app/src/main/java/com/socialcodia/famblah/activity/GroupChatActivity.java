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
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.adapter.AdapterGroupChat;
import com.socialcodia.famblah.model.ModelGroup;
import com.socialcodia.famblah.model.ModelGroupChat;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<ModelGroupChat> modelGroupChatList;
    Uri filePath;

    private TextView tvGroupName, tvGroupStatus;
    private EditText inputGroupMessage;
    private ImageView ivAttachGroupFile, ivSendGroupMessage, groupImageIcon;
    private Toolbar mToolbar;
    private ConstraintLayout groupConstraintLayout;
    ActionBar actionBar;
    Intent intent;
    String groupId,userId;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        //Ui Init
        tvGroupName = findViewById(R.id.tvGroupName);
        tvGroupStatus = findViewById(R.id.tvGroupStatus);
        inputGroupMessage = findViewById(R.id.inputGroupMessage);
        ivAttachGroupFile = findViewById(R.id.ivAttachGroupFile);
        ivSendGroupMessage = findViewById(R.id.ivSendGroupMessage);
        groupImageIcon = findViewById(R.id.groupImageIcon);
        recyclerView = findViewById(R.id.groupChatRecyclerView);
        mToolbar = findViewById(R.id.groupToolbar);
        groupConstraintLayout = findViewById(R.id.groupConstraintLayout);
        //Firebase init

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        mUser = mAuth.getCurrentUser();

        //set toolbar
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Get Data from intent
        intent = getIntent();
        groupId = intent.getStringExtra("gid");

        if (mUser!=null)
        {
            userId = mUser.getUid();
        }

        //LinearLayoutManager for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        modelGroupChatList = new ArrayList<>();

        //Get Group Details
        getGroupsDetails();

        //Get Group Messages

        getChats();

        ivAttachGroupFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        ivSendGroupMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateMessage();
            }
        });

        groupConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToGroupInfo();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setAction(intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode==RESULT_OK && data!=null)
        {
            filePath = data.getData();
            uploadImage(filePath);
        }
    }

    private void uploadImage(Uri filePath)
    {
        Toast.makeText(this, "Sending Image..", Toast.LENGTH_SHORT).show();
        String pathAndImageName = "Group/Chat/famblah_"+System.currentTimeMillis();
        mStorageRef.child(pathAndImageName).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageDownloadUrl = uri.toString();
                            sendImageMessage(imageDownloadUrl);
                        }
                    });
                }
            }
        });
    }

    private void sendImageMessage(String imageDownloadUrl)
    {
        DatabaseReference mChatRef = FirebaseDatabase.getInstance().getReference(Constants.GROUPS).child(groupId).child(Constants.CHATS);
        String messageId = mChatRef.push().getKey();
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.CHAT_SENDER_ID,userId);
        map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
        map.put(Constants.CHAT_TYPE,"image");
        map.put(Constants.GROUP_ID,groupId);
        map.put(Constants.CHAT_IMAGE,imageDownloadUrl);
        map.put(Constants.CHAT_STATUS,1);
        map.put(Constants.CHAT_MESSAGE_ID,messageId);
        mChatRef.child(messageId).setValue(map);
    }

    private void getChats()
    {
        mRef.child(Constants.GROUPS).child(groupId).child(Constants.CHATS)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroupChatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelGroupChat modelGroupChat = ds.getValue(ModelGroupChat.class);
                    modelGroupChatList.add(modelGroupChat);
                }
                AdapterGroupChat adapterGroupChat = new AdapterGroupChat(modelGroupChatList,getApplicationContext());
                recyclerView.scrollToPosition(modelGroupChatList.size() - 1);
                recyclerView.setAdapter(adapterGroupChat);
                adapterGroupChat.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ValidateMessage()
    {
        String message = inputGroupMessage.getText().toString().trim();
        if (message.isEmpty())
        {
            Toast.makeText(this, "Can't sent empty message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            SendMessage(message);
        }
    }

    private void SendMessage(String message)
    {
        mRef.child(Constants.GROUPS).child(groupId).child(Constants.CHATS);

        DatabaseReference mChatRef = FirebaseDatabase.getInstance().getReference(Constants.GROUPS).child(groupId).child(Constants.CHATS);

        String messageId = mChatRef.push().getKey();
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.CHAT_SENDER_ID,userId);
        map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
        map.put(Constants.CHAT_TYPE,"text");
        map.put(Constants.CHAT_MESSAGE,message);
        map.put(Constants.CHAT_STATUS,1);
        map.put(Constants.GROUP_ID,groupId);
        map.put(Constants.CHAT_MESSAGE_ID,messageId);
        mChatRef.child(messageId).setValue(map);
        inputGroupMessage.setText("");
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.group_chat_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();
        switch (id)
        {
            case R.id.miGroupInfo:
                sendToGroupInfo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendToGroupInfo()
    {
        Intent intent= new Intent(getApplicationContext(),GroupInfoActivity.class);
        intent.putExtra("groupId",groupId);
        startActivity(intent);
    }

    private void getGroupsDetails()
    {
        mRef.child(Constants.GROUPS).child(groupId)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String groupName = dataSnapshot.child(Constants.GROUP_NAME).getValue(String.class);
                String groupImage = dataSnapshot.child(Constants.GROUP_IMAGE).getValue(String.class);

                //Set value
                tvGroupName.setText(groupName);
                try {
                    Picasso.get().load(groupImage).into(groupImageIcon);
                }
                catch (Exception e)
                {
                    Picasso.get().load(R.drawable.group_image).into(groupImageIcon);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
