package com.socialcodia.famblah.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.socialcodia.famblah.activity.AddStatusActivity;
import com.socialcodia.famblah.adapter.AdapterStatus;
import com.socialcodia.famblah.model.ModelChat;
import com.socialcodia.famblah.model.ModelStatus;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatusFragment extends Fragment {

    FloatingActionButton fabAddStatus;
    private ConstraintLayout addStatusConstraintLayout;
    Uri filePath;

    RecyclerView statusRecyclerView;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    private ImageView userProfileImage, ivUserProfileImage;

    List<ModelStatus> modelStatusList;

    String userId;
    String hiss;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        fabAddStatus = view.findViewById(R.id.fabAddStatus);
        addStatusConstraintLayout = view.findViewById(R.id.AddStatusConstraintsLayout);
        addStatusConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //UI views init

        userProfileImage = view.findViewById(R.id.userProfileImage);
        ivUserProfileImage = view.findViewById(R.id.ivUserProfileImage);

        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

        if (mAuth.getCurrentUser()!=null)
        {
            userId = mAuth.getCurrentUser().getUid();
        }
        else
        {
            Toast.makeText(getContext(), "User is Null", Toast.LENGTH_LONG).show();
        }

        statusRecyclerView = view.findViewById(R.id.statusRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        statusRecyclerView.setLayoutManager(layoutManager);

        modelStatusList = new ArrayList<>();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null)
        {
            hiss = user.getUid();
        }

        fabAddStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        getStatus();
        getUserInfo();

        return view;
    }

    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100 && resultCode== Activity.RESULT_OK)
        {
            filePath = data.getData();
            Intent intent = new Intent(getContext(), AddStatusActivity.class);
            intent.putExtra("filePath",filePath.toString());
            startActivity(intent);
        }
    }

    private void getStatus()
    {
        mRef.child("Status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelStatusList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelStatus modelStatus = ds.getValue(ModelStatus.class);
                    modelStatusList.add(modelStatus);
                }
                AdapterStatus  adapterStatus = new AdapterStatus(modelStatusList,getContext());
                statusRecyclerView.setAdapter(adapterStatus);
                adapterStatus.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo()
    {

        DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = mUserDbRef.orderByChild(Constants.USER_ID).equalTo(hiss);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String userImageData = ds.child(Constants.USER_IMAGE).getValue(String.class);

                    try {
                        Picasso.get().load(userImageData).into(ivUserProfileImage);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.person_male).into(ivUserProfileImage);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Oops!"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
