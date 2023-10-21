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
import com.socialcodia.famblah.model.ModelUser;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

    Uri filePath;
    String userId;

    ActionBar actionBar;

    private ImageView userProfileImage;
    private EditText tvUserName, tvUserBio,tvMobileNumber;
    private Button btnUpdateProfile;

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
        setContentView(R.layout.activity_setting);

        //Ui Init

        userProfileImage = findViewById(R.id.userProfileImage);
        tvUserName = findViewById(R.id.inputUserName);
        tvUserBio = findViewById(R.id.inputUserBio);
        tvMobileNumber = findViewById(R.id.inputMobileNumber);
        tvMobileNumber.setEnabled(false);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);


        //Firebase Init
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateData();
            }
        });

        if (mAuth.getCurrentUser()!=null)
        {
            userId = mAuth.getCurrentUser().getUid();
        }

        getUserData();
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

        if (requestCode==100 && resultCode==RESULT_OK && data!=null)
        {
            filePath= data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                userProfileImage.setImageBitmap(bitmap);
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Oops! Something Went Wrong. \n Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getUserData()
    {
        mRef.child(Constants.USERS).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child(Constants.USER_NAME).getValue(String.class);
                String bio = dataSnapshot.child(Constants.USER_BIO).getValue(String.class);
                String mobile = dataSnapshot.child(Constants.USER_MOBILE).getValue(String.class);
                String image = dataSnapshot.child(Constants.USER_IMAGE).getValue(String.class);
                tvUserName.setText(name);
                tvUserBio.setText(bio);
                tvMobileNumber.setText(mobile);
                try {
                    Picasso.get().load(image).into(userProfileImage);
                }
                catch (Exception e)
                {
                    Picasso.get().load(R.drawable.person_male).into(userProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void ValidateData()
    {
        String name = tvUserName.getText().toString().trim();
        String bio = tvUserBio.getText().toString().trim();
        String mobile = tvMobileNumber.getText().toString().trim();

        if (name.isEmpty())
        {
            tvUserName.setError("Enter Your Name");
            tvUserName.requestFocus();
        }
        else if (bio.isEmpty())
        {
            tvUserBio.setError("Enter Bio");
            tvUserBio.requestFocus();
        }
        else if (mobile.isEmpty())
        {
            tvMobileNumber.setError("Enter Mobile Number");
            tvMobileNumber.requestFocus();
        }
        else
        {
            if (filePath!=null)
            {
                uploadProfileImage(name,bio);
            }
            else
            {
                updateProfile(name,bio);
            }
        }
    }

    private void uploadProfileImage(final String name, final String bio)
    {
        Toast.makeText(this, "Updating Profile...", Toast.LENGTH_SHORT).show();
        btnUpdateProfile.setEnabled(false);
        mStorageRef.child("UsersProfileImage").child(userId).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageDownloadUrl = uri.toString();
                        updateProfileWithImage(name,bio,imageDownloadUrl);
                    }
                });
            }
        });
    }

    private void updateProfileWithImage(String name, String bio, String downloadUrl)
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.USER_NAME,name);
        map.put(Constants.USER_BIO,bio);
        map.put(Constants.USER_IMAGE,downloadUrl);
        mRef.child(Constants.USERS).child(userId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                btnUpdateProfile.setEnabled(true);
                Toast.makeText(SettingActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                sendToHome();
            }
        });
    }

    private void updateProfile(String name, String bio)
    {
        btnUpdateProfile.setEnabled(false);
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.USER_NAME,name);
        map.put(Constants.USER_BIO,bio);
        mRef.child(Constants.USERS).child(userId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                btnUpdateProfile.setEnabled(true);
                Toast.makeText(SettingActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                sendToHome();
            }
        });
    }

    private void sendToHome()
    {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }
}
