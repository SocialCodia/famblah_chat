package com.socialcodia.famblah.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

public class UserInfoActivity extends AppCompatActivity {

    String userId;
    ActionBar actionBar;
    Intent intent;

    private ImageView userProfileImage;
    private TextView tvUserName;

    //Firebase

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        userProfileImage = findViewById(R.id.userProfileImage);
        tvUserName = findViewById(R.id.tvUserName);

        intent = getIntent();
        userId = intent.getStringExtra("userId");

        //Firebase Init
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

       getUserDetails(userId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void getUserDetails(String userId)
    {
        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference(Constants.USERS);
        mUserRef.orderByChild(Constants.USER_ID).equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String name = ds.child(Constants.USER_NAME).getValue(String.class);
                    String image = ds.child(Constants.USER_IMAGE).getValue(String.class);

                    //Set Data
                    actionBar.setTitle(name);
                    tvUserName.setText(name);
                    try {
                        Picasso.get().load(image).into(userProfileImage);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(UserInfoActivity.this, "Image Error " +e.getMessage(), Toast.LENGTH_SHORT).show();
                        Picasso.get().load(R.drawable.person_male).into(userProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
