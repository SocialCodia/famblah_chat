package com.socialcodia.famblah.adapter;

import android.content.Context;
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
import com.socialcodia.famblah.activity.GroupChatActivity;
import com.socialcodia.famblah.model.ModelGroup;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdapterGroup extends RecyclerView.Adapter<AdapterGroup.ViewHolder> {

    List<ModelGroup> modelGroupList;
    Context context;

    public AdapterGroup(List<ModelGroup> modelGroupList, Context context) {
        this.modelGroupList = modelGroupList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_group,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ModelGroup model = modelGroupList.get(position);

        String groupName = model.getName();
        String groupImage = model.getImage();
        final String gid = model.getGroup_id();

        holder.tvGroupName.setText(groupName);
        try {
            Picasso.get().load(groupImage).into(holder.groupImageIcon);
        }
        catch (Exception e)
        {
            Picasso.get().load(R.drawable.group_image).into(holder.groupImageIcon);
        }

        getLastMessage(model,holder);

        holder.groupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToGroupChat(gid);
            }
        });
    }

    private void getLastMessage(ModelGroup model, final ViewHolder holder)
    {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.GROUPS).child(model.getGroup_id()).child(Constants.CHATS);
        mRef.limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String message = ds.child(Constants.CHAT_MESSAGE).getValue(String.class);
                    String timestamp = ds.child(Constants.TIMESTAMP).getValue(String.class);
                    String sender = ds.child(Constants.CHAT_SENDER_ID).getValue(String.class);
                    String type = ds.child(Constants.CHAT_TYPE).getValue(String.class);
                    int status = ds.child(Constants.CHAT_STATUS).getValue(Integer.class);

                    if (status==1 ) // im
                    {
                        if (type.equals("text"))
                        {
                            if (message.length()>30)
                            {
                                message = message.substring(0,30)+"...";
                                holder.tvLastMessage.setText(message);
                            }
                            else
                            {
                                holder.tvLastMessage.setText(message);
                            }
                        }
                        else
                        {
                            if (sender.equals(FirebaseAuth.getInstance().getUid()))
                            {
                                holder.tvLastMessage.setText(" You sent an image");
                            }
                            else
                            {
                                holder.tvLastMessage.setText(" Sent an image");
                            }
                        }
                    }
                    else
                    {
                        if (sender.equals(FirebaseAuth.getInstance().getUid()))
                        {
                            holder.tvLastMessage.setText("You deleted this message");
                        }
                        else
                        {
                            holder.tvLastMessage.setText("This message was deleted");
                        }
                    }
                    holder.tvMessageTimestamp.setText(getTime(timestamp));
                    //Get Sender Information

                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.USERS);
                    Query query = mRef.orderByChild(Constants.USER_ID).equalTo(sender);
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren())
                                    {
                                        String name = ds.child(Constants.USER_NAME).getValue(String.class);
                                        holder.tvLastMessageSenderName.setText(name+" ");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getTime(String timestamp)
    {
        Long ts = Long.parseLong(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:a");
        String time = sdf.format(new Date(ts));
        return time;
    }

    private void sendToGroupChat(String gid)
    {
        Intent intent= new Intent(context, GroupChatActivity.class);
        intent.putExtra("gid",gid);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return modelGroupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tvGroupName,tvLastMessageSenderName,tvLastMessage,tvMessageTimestamp;
        private ImageView groupImageIcon;
        private ConstraintLayout groupLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvLastMessageSenderName = itemView.findViewById(R.id.tvLastMessageSenderName);
            tvMessageTimestamp = itemView.findViewById(R.id.tvMessageTimestamp);
            groupImageIcon = itemView.findViewById(R.id.groupImageIcon);
            groupLayout = itemView.findViewById(R.id.groupLayout);

        }
    }
}
