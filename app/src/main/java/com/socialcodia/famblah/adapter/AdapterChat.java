package com.socialcodia.famblah.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.socialcodia.famblah.model.ModelChat;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.ViewHolder> {

    Context context;
    List<ModelChat> modelChatList;

    String userId;

    public static final  int MESSAGE_TYPE_RIGHT = 0;
    public static final int MESSAGE_TYPE_LEFT = 1;

    public AdapterChat(Context context, List<ModelChat> modelChatList) {
        this.context = context;
        this.modelChatList = modelChatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType== MESSAGE_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_right_chat,parent,false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_left_chat,parent,false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position)
    {
        final ModelChat modelChat = modelChatList.get(position);
        String message = modelChatList.get(position).getMessage();
        String timestamp = modelChatList.get(position).getTimestamp();
        String sender = modelChatList.get(position).getSender();
        String chatImage = modelChatList.get(position).getImage();
        int messageStatus = modelChatList.get(position).getStatus();

        if (modelChatList.get(position).getType().equals("image") && modelChatList.get(position).getStatus()==1)
        {

            holder.ivChatImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendToViewImage(modelChat);
                }
            });
        }


        if (messageStatus==1)
        {
            if (modelChatList.get(position).getType().equals("text"))
            {
                holder.tvChatMessage.setText(message);
            }
            else if (modelChatList.get(position).getType().equals("image"))
            {
                holder.ivChatImage.setVisibility(View.VISIBLE);
                holder.tvChatMessage.setVisibility(View.GONE);
                try {
                    Picasso.get().load(chatImage).into(holder.ivChatImage);
                }
                catch (Exception e)
                {
                    Picasso.get().load(R.drawable.person_female).into(holder.ivChatImage);
                }
            }
        }
        else
        {
            if(sender.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            {
                holder.tvChatMessage.setText("You deleted this message");
            }
            else
            {
                holder.tvChatMessage.setText("This message was deleted");
            }
        }


        holder.tvChatTimestamp.setText(getTime(timestamp));

        holder.chat_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                builder.setTitle("Delete");
                builder.setMessage("Are you sure want to delete");
                //Delete Button
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });
                //Cancel Button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                builder.create().show();

                return true;
            }
        });

    }

    private void sendToViewImage(ModelChat modelChat)
    {
        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.putExtra("image",modelChat.getImage());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void deleteMessage(int position)
    {
        final DatabaseReference mChatRef = FirebaseDatabase.getInstance().getReference("Chats");
        String messageId = modelChatList.get(position).getMid();
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = mChatRef.orderByChild(Constants.CHAT_MESSAGE_ID).equalTo(messageId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String senderId = ds.child(Constants.CHAT_SENDER_ID).getValue(String.class);

                    if (senderId.equals(userId))
                    {
                        HashMap<String,Object> map = new HashMap<>();
                        map.put(Constants.CHAT_STATUS,0);
                        ds.getRef().updateChildren(map);
                    }
                    else
                    {
                        Toast.makeText(context, "You Can Delete Only Your Messages", Toast.LENGTH_SHORT).show();
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
        return modelChatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tvChatMessage, tvChatTimestamp;
        private ImageView ivChatImage;
        private LinearLayout chat_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatMessage = itemView.findViewById(R.id.tvChatMessage);
            tvChatTimestamp = itemView.findViewById(R.id.tvChatTimestamp);
            chat_layout = itemView.findViewById(R.id.chat_layout);
            ivChatImage = itemView.findViewById(R.id.ivChatImage);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (modelChatList.get(position).getSender().equals(userId))
        {
            return MESSAGE_TYPE_RIGHT;
        }
        else
        {
            return MESSAGE_TYPE_LEFT;
        }
    }

    private String getTime(String timestamp)
    {
        Long ts = Long.valueOf(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:a");
        String time = sdf.format(new Date(ts));
        return time;
    }
}
