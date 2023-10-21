package com.socialcodia.famblah.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.activity.AllUserActivity;
import com.socialcodia.famblah.activity.CreateGroupActivity;
import com.socialcodia.famblah.activity.PhoneLoginActivity;
import com.socialcodia.famblah.activity.SettingActivity;
import com.socialcodia.famblah.adapter.AdapterChatList;
import com.socialcodia.famblah.adapter.AdapterGroup;
import com.socialcodia.famblah.adapter.AdapterUser;
import com.socialcodia.famblah.model.ModelChat;
import com.socialcodia.famblah.model.ModelChatList;
import com.socialcodia.famblah.model.ModelGroup;
import com.socialcodia.famblah.model.ModelUser;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private FloatingActionButton fab;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;



    RecyclerView chatListRecyclerView;

    List<ModelChatList> modelChatListList;
    List<ModelUser> modelUserList;

    String userId;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats,container,false);


        modelChatListList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef =  mDatabase.getReference("Users");

        chatListRecyclerView = view.findViewById(R.id.chatListRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatListRecyclerView.setLayoutManager(layoutManager);

        fab = view.findViewById(R.id.fabShowAllUser);


        if (mAuth.getCurrentUser()!=null)
        {
            userId = mAuth.getCurrentUser().getUid();
        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToAllUserActivity();
            }
        });


        setHasOptionsMenu(true);
        getChatList();
        return view;
    }

    private void sendToAllUserActivity()
    {
        Intent intent = new Intent(getContext(), AllUserActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.main_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.miLogout:
                doLogout();
                break;
            case R.id.miSetting:
                sendToSetting();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendToSetting()
    {
        Intent intent = new Intent(getContext(), SettingActivity.class);
        startActivity(intent);
    }

    private void doLogout()
    {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin()
    {
        Intent intent = new Intent(getContext(), PhoneLoginActivity.class);
        startActivity(intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private void getChatList()
    {
        DatabaseReference mChatListRef = FirebaseDatabase.getInstance().getReference("ChatList").child(userId);
        mChatListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelChatListList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelChatList modelChatList = ds.getValue(ModelChatList.class);
                    modelChatListList.add(modelChatList);
                }
                getUsersByChatListId();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUsersByChatListId()
    {
        modelUserList = new ArrayList<>();
        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference("Users");
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelUserList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    for (ModelChatList modelChatList : modelChatListList)
                    {
                        if (modelUser.getUid()!=null && modelUser.getUid().equals(modelChatList.getId()))
                        {
                            modelUserList.add(modelUser);
                        }
                        AdapterChatList adapterChatList = new AdapterChatList(getContext(), modelUserList);
                        chatListRecyclerView.setAdapter(adapterChatList);
                        adapterChatList.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
