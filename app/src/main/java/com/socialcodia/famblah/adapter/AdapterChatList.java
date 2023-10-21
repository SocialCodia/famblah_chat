package com.socialcodia.famblah.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.activity.ChatActivity;
import com.socialcodia.famblah.model.ModelChat;
import com.socialcodia.famblah.model.ModelChatList;
import com.socialcodia.famblah.model.ModelGroup;
import com.socialcodia.famblah.model.ModelUser;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.ViewHolder> {

    Context context;
    List<ModelUser> modelUserList;
    List<ModelChat> modelChats;

    public AdapterChatList(List<ModelChat> modelChats) {
        this.modelChats = modelChats;
    }

    public AdapterChatList(Context context, List<ModelUser> modelUserList) {
        this.context = context;
        this.modelUserList = modelUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_chat_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ModelUser model = modelUserList.get(position);
        String name = model.getName();
        String bio = model.getBio();
        String image = model.getImage();
        final String userId = model.getUid();

        holder.tvUserName.setText(name);
        holder.tvUserBio.setText(bio);
        try {
            Picasso.get().load(image).into(holder.userProfileImage);
        }
        catch (Exception e)
        {
            Picasso.get().load(R.drawable.person_female);
        }

        getLastMessage(model,holder);

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToChat(userId);
            }
        });

        holder.constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialogue(model);
                return true;
            }
        });

    }

    private void showDialogue(final ModelUser model)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Remove");
        builder.setMessage("Are you sure want to remove");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteChatList(model);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "cancel", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void deleteChatList(ModelUser model)
    {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("ChatList");
        String userId = FirebaseAuth.getInstance().getUid();
        String hisUserId = model.getUid();
        mRef.child(userId).child(hisUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ds.getRef().removeValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLastMessage(final ModelUser model, final ViewHolder holder)
    {
        modelChats = new ArrayList<>();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.CHATS);
        mRef.limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat.getSender().equals(FirebaseAuth.getInstance().getUid()) && modelChat.getReceiver().equals(model.getUid())
                        ||
                        modelChat.getSender().equals(model.getUid()) && modelChat.getReceiver().equals(FirebaseAuth.getInstance().getUid())
                    )
                    {
                        String message = modelChat.getMessage();
                        holder.tvUserBio.setText(message);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendToChat(String userId)
    {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("hisUid",userId);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return modelUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        private TextView tvUserName, tvUserBio;
        private ImageView userProfileImage;
        private ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserBio = itemView.findViewById(R.id.tvUserBio);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
        }
    }
}
