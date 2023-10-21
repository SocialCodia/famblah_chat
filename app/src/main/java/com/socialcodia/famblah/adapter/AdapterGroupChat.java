package com.socialcodia.famblah.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.activity.ViewImageActivity;
import com.socialcodia.famblah.model.ModelGroupChat;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.security.AccessController.getContext;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.ViewHolder>
{

    private List<ModelGroupChat> modelGroupChatList;
    private Context context;

    private FirebaseAuth mAuth;

    public AdapterGroupChat(List<ModelGroupChat> modelGroupChatList, Context context) {
        this.modelGroupChatList = modelGroupChatList;
        this.context = context;

        mAuth = FirebaseAuth.getInstance();

    }

    private static final int MESSAGE_TYPE_LEFT = 0;
    private static final int MESSAGE_TYPE_RIGHT = 1;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==MESSAGE_TYPE_LEFT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_left,parent,false);
            return new ViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_right,parent,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final ModelGroupChat model = modelGroupChatList.get(position);

        String message = model.getMessage();
        String messageId = model.getMid();
        String senderId = model.getSender();
        String chatImage = model.getImage();
        String type = model.getType();
        String timestamp = model.getTimestamp();
        int chatStatus = model.getStatus();

        if (chatStatus == 1)
        {
            if (type.equals("text"))
            {
                holder.tvChatMessage.setVisibility(View.VISIBLE);
                holder.ivChatImage.setVisibility(View.GONE);
                holder.tvChatMessage.setText(message);
            }
            else
            {
                holder.tvChatMessage.setVisibility(View.GONE);
                holder.ivChatImage.setVisibility(View.VISIBLE);
                try {
                    Picasso.get().load(chatImage).into(holder.ivChatImage);
                }
                catch (Exception e)
                {
                    Toast.makeText(context, "Oops! Failed to load the image", Toast.LENGTH_SHORT).show();
                    Picasso.get().load(R.drawable.person_female).into(holder.ivChatImage);
                }
            }
        }
        else if (chatStatus ==0)
        {

            holder.tvChatMessage.setVisibility(View.VISIBLE);
            holder.ivChatImage.setVisibility(View.GONE);
            if (senderId.equals(FirebaseAuth.getInstance().getUid()))
            {
                holder.tvChatMessage.setText("You have deleted this message");
            }
            else
            {
                holder.tvChatMessage.setText("This message was deleted");
            }
        }

        if (model.getType().equals("image"))
        {
            holder.ivChatImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendToViewImage(model);
                }
            });
        }

        holder.chat_layout.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                builder.setTitle("Delete");
                builder.setMessage("Are you sure want to delete");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(model);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "No Clicked", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create().show();
                return true;
            }
        });

        holder.tvChatTimestamp.setText(getTime(timestamp));

        setSenderName(model,holder);
    }

    private void deleteMessage(final ModelGroupChat model)
    {
         final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.GROUPS).child(model.getGroup_id()).child(Constants.CHATS).child(model.getMid());
         mRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if (model.getSender().equals(mAuth.getUid()))
                 {
                     HashMap<String, Object> map = new HashMap<>();
                     map.put(Constants.CHAT_STATUS,0);
                     mRef.updateChildren(map);
                 }
                 else
                 {
                     Toast.makeText(context, "You can delete only your own messages", Toast.LENGTH_SHORT).show();
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
    }

    private void sendToViewImage(ModelGroupChat model)
    {
        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.putExtra("image",model.getImage());
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void setSenderName(final ModelGroupChat model, final ViewHolder holder)
    {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.USERS);
        Query query = mRef.orderByChild(Constants.USER_ID).equalTo(model.getSender());
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren())
                        {
                            String name = ds.child(Constants.USER_NAME).getValue(String.class);
                            if (FirebaseAuth.getInstance().getCurrentUser()!=null)
                            {
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                if (userId.equals(model.getSender()))
                                {

                                }
                                else
                                {
                                    holder.tvMessageSenderName.setText(name);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        return modelGroupChatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        private TextView tvChatTimestamp, tvChatMessage,tvMessageSenderName;
        private ImageView ivChatImage;
        private LinearLayout chat_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMessageSenderName = itemView.findViewById(R.id.tvMessageSenderName);
            tvChatMessage = itemView.findViewById(R.id.tvChatMessage);
            tvChatTimestamp = itemView.findViewById(R.id.tvChatTimestamp);
            ivChatImage = itemView.findViewById(R.id.ivChatImage);
            chat_layout = itemView.findViewById(R.id.chat_layout);

        }
    }

    private String getTime(String timestamp)
    {
        Long ts = Long.valueOf(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:a");
        String time = sdf.format(new Date(ts));
        return time;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (modelGroupChatList.get(position).getSender().equals(mAuth.getUid()))
        {
            return MESSAGE_TYPE_RIGHT;
        }
        else
        {
            return MESSAGE_TYPE_LEFT;
        }
    }
}
