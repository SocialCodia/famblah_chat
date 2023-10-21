package com.socialcodia.famblah.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.storage.Constants;

import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {

    ActionBar actionBar;

    private EditText inputGroupName, inputGroupDescription;
    private ImageView groupImageIcon;
    private Button btnCreateGroup;

    //Firebase

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;
    FirebaseUser mUser;

    Uri filePath;
    String imageDownloadUrl,userId, groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        inputGroupName = findViewById(R.id.inputGroupName);
        inputGroupDescription = findViewById(R.id.inputGroupDescription);
        groupImageIcon = findViewById(R.id.groupImageIcon);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        mUser = mAuth.getCurrentUser();

        if (mUser!=null)
        {
            userId = mUser.getUid();
        }

        actionBar = getSupportActionBar();
        actionBar.setTitle("Create Group");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        groupImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
    }

    private void validateData()
    {
        String groupName = inputGroupName.getText().toString().trim();
        String groupDescription = inputGroupDescription.getText().toString().trim();
        if (groupName.isEmpty())
        {
            inputGroupName.setError("Enter Group Name");
            inputGroupName.requestFocus();
        }
        else if (groupName.length()<3)
        {
            inputGroupName.setError("Group Name Should Be Greater Than 3 Character");
            inputGroupName.requestFocus();
        }
        else if (groupDescription.isEmpty())
        {
            groupDescription="famblah";
            if (filePath==null)
            {
                CreateGroupWithoutIcon(groupName,groupDescription);
            }
            else
            {
                uploadGroupImage(groupName,groupDescription);
            }
        }
        else if (filePath==null)
        {
            CreateGroupWithoutIcon(groupName,groupDescription);
        }
        else
        {
            uploadGroupImage(groupName,groupDescription);
        }
   }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void CreateGroupWithoutIcon(String groupName, String groupDescription)
    {
        groupId = mRef.push().getKey();
        btnCreateGroup.setEnabled(false);
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.GROUP_NAME,groupName);
        map.put(Constants.GROUP_DESCRIPTION,groupDescription);
        map.put(Constants.GROUP_IMAGE,"");
        map.put(Constants.GROUP_ID,groupId);
        map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
        map.put(Constants.GROUP_CREATOR,userId);
        mRef.child(Constants.GROUPS).child(groupId).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                AddParticipant(groupId);
            }
        });
    }


    private void sendToHome()
    {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
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
        if (requestCode==100 && resultCode==RESULT_OK)
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);

                groupImageIcon.setImageBitmap(bitmap);
            }
            catch (Exception e)
            {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadGroupImage(final String groupName, final String groupDescription)
    {
        btnCreateGroup.setEnabled(false);
        String pathAndImageName = "Group/famblah_"+System.currentTimeMillis();
        mStorageRef.child(pathAndImageName).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageDownloadUrl = uri.toString();
                            createGroupWithImageIcon(groupName,groupDescription,imageDownloadUrl);
                        }
                    });
                }
            }
        });
    }

    private void createGroupWithImageIcon(String groupName, String groupDescription, String imageDownloadUrl)
    {
        groupId = mRef.push().getKey();
        btnCreateGroup.setEnabled(false);
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.GROUP_NAME,groupName);
        map.put(Constants.GROUP_DESCRIPTION,groupDescription);
        map.put(Constants.GROUP_IMAGE,imageDownloadUrl);
        map.put(Constants.GROUP_ID,groupId);
        map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
        map.put(Constants.GROUP_CREATOR,userId);
        mRef.child(Constants.GROUPS).child(groupId).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                AddParticipant(groupId);
            }
        });
    }


    private void AddParticipant(final String groupId)
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.GROUP_MEMBER_ID,userId);
        map.put(Constants.GROUP_MEMBER_ROLE,"Admin");
        map.put(Constants.GROUP_MEMBER_JOINING_TIME,String.valueOf(System.currentTimeMillis()));
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.GROUPS).child(groupId).child(Constants.MEMBERS)
                .child(userId);
        mRef.setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {
                sendToHome();
                Toast.makeText(CreateGroupActivity.this, "The group has been created.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateGroupActivity.this, "Failed to add participant", Toast.LENGTH_SHORT).show();
                AddParticipant(groupId);
            }
        });
    }

}
