package com.socialcodia.famblah.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

//import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AddStatusActivity extends AppCompatActivity {

    Intent intent;
    Uri filePath;

//    ActionBar actionBar;
    Toolbar toolbar;

    private EditText inputStatusContent;
    private ImageView selectedStatusImage, userProfileImage;
    private TextView tvUserName;
    FloatingActionButton btnAddStatus;

    String statusContent,userName;
    String currentUserId;

    //Firebase

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    DatabaseReference mUserRef;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;
    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_status_activity);

        //init

        inputStatusContent = findViewById(R.id.inputStatusContent);
        selectedStatusImage = findViewById(R.id.selectedStatusImage);
        tvUserName = findViewById(R.id.tvUserName);
        userProfileImage = findViewById(R.id.userProfileImage);
        btnAddStatus = findViewById(R.id.fabAddStatus);
        toolbar = findViewById(R.id.addStatusToolbar);

        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        mUserRef = mDatabase.getReference("Users");
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        firebaseUser = mAuth.getCurrentUser();

        intent = getIntent();
        String stringFilePath = intent.getStringExtra("filePath");
        filePath = Uri.parse(stringFilePath);

//        setSupportActionBar(toolbar);
//        actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);

        if (firebaseUser!=null)
        {
            currentUserId = firebaseUser.getUid();
        }

        btnAddStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImage();
            }
        });

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
            selectedStatusImage.setImageBitmap(bitmap);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Oops !"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        getUserInfo();

    }

    @Override

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void UploadImage()
    {
        Toast.makeText(this, "Please wait... Adding Status", Toast.LENGTH_SHORT).show();
        String fileNameAndPath = "Status/famblah_"+System.currentTimeMillis();
        mStorageRef.child(fileNameAndPath).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageDownloadUrl = uri.toString();
                            AddStatusDatabase(imageDownloadUrl);
                        }
                    });
                }
            }
        });
    }

    private void AddStatusDatabase(String imageDownloadUrl)
    {
        statusContent = inputStatusContent.getText().toString().trim();
        if (statusContent.isEmpty())
        {
            String statusId =  mRef.push().getKey();
            HashMap<String,Object> map = new HashMap<>();
            map.put(Constants.STATUS_IMAGE,imageDownloadUrl);
            map.put(Constants.STATUS_CONTENT,"famblah");
            map.put(Constants.STATUS_ID,statusId);
            map.put(Constants.STATUS_SENDER_NAME,userName);
            map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
            map.put(Constants.STATUS_SENDER_ID,mAuth.getCurrentUser().getUid());
            mRef.child("Status").child(statusId).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(AddStatusActivity.this, "Status Has Been Added", Toast.LENGTH_SHORT).show();
                    sendToMainActivity();
                }
            });
        }
        else
        {
            String statusId =  mRef.push().getKey();
            HashMap<String,Object> map = new HashMap<>();
            map.put(Constants.STATUS_IMAGE,imageDownloadUrl);
            map.put(Constants.STATUS_CONTENT,statusContent);
            map.put(Constants.STATUS_ID,statusId);
            map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
            map.put(Constants.STATUS_SENDER_NAME,userName);
            map.put(Constants.STATUS_SENDER_ID,mAuth.getCurrentUser().getUid());
            mRef.child("Status").child(statusId).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(AddStatusActivity.this, "Status Has Been Added", Toast.LENGTH_SHORT).show();
                    sendToMainActivity();
                }
            });
        }
    }

    private void sendToMainActivity()
    {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void getUserInfo()
    {

        DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = mUserDbRef.orderByChild(Constants.USER_ID).equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String userImageData = ds.child(Constants.USER_IMAGE).getValue(String.class);
                    userName = ds.child(Constants.USER_NAME).getValue(String.class);

                    try {
                        Picasso.get().load(userImageData).into(userProfileImage);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.person_male).into(userProfileImage);
                    }
                    tvUserName.setText(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Oops!"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}
