package com.socialcodia.famblah.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.adapter.AdapterUser;
import com.socialcodia.famblah.model.ModelUser;
import com.socialcodia.famblah.storage.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AllUserActivity extends AppCompatActivity {

    private RecyclerView allUserRecyclerView;

    List<ModelUser> modelUserList;

    //Firebase

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Choose Contact");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);



        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Users");

        //Init

        allUserRecyclerView = findViewById(R.id.allUserRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        allUserRecyclerView.setLayoutManager(layoutManager);

        if (mAuth.getCurrentUser()!=null)
        {
            userId = mAuth.getCurrentUser().getUid();
        }

        modelUserList = new ArrayList<>();

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelUserList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    assert modelUser != null;
                    if (!modelUser.getUid().equals(userId))
                    {
                        modelUserList.add(modelUser);
                    }
                }
                AdapterUser adapterUser = new AdapterUser(getApplicationContext(),modelUserList);
                allUserRecyclerView.setAdapter(adapterUser);
                adapterUser.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
