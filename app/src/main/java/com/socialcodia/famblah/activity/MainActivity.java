package com.socialcodia.famblah.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.adapter.TabAdapter;
import com.socialcodia.famblah.storage.Constants;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = findViewById(R.id.mViewPager);
        mTabLayout = findViewById(R.id.mTabLayout);
        mToolbar = findViewById(R.id.mToolbar);

        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Users");

        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        if (mAuth.getCurrentUser()!=null)
        {
            userId = mAuth.getCurrentUser().getUid();
        }

        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Famblah");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserStatus("online");

    }

    @Override
    protected void onPause() {
        setUserStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }

    private void setUserStatus(String status)
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.USER_STATUS,status);
        mRef.child(userId).updateChildren(map);
    }
}
