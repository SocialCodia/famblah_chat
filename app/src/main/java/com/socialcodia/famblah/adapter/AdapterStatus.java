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
import com.socialcodia.famblah.activity.ViewStatusActivity;
import com.socialcodia.famblah.model.ModelStatus;
import com.socialcodia.famblah.model.ModelUser;
import com.socialcodia.famblah.storage.Constants;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

public class AdapterStatus extends RecyclerView.Adapter<AdapterStatus.ViewHolder> {


    List<ModelStatus> modelStatusList;
    Context context;

    String name;

    public AdapterStatus(List<ModelStatus> modelStatusList, Context context) {
        this.modelStatusList = modelStatusList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_status_list,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String statusContent = modelStatusList.get(position).getStatus_content();
        String statusImage = modelStatusList.get(position).getImage();
        final String statusId = modelStatusList.get(position).getStatus_id();
        final String senderId = modelStatusList.get(position).getSender_id();
        String statusSenderName = modelStatusList.get(position).getName();
        String timestamp = modelStatusList.get(position).getTimestamp();

//        holder.tvStatusTimestamp.setText(getTime(timestamp));

        try {
            Picasso.get().load(statusImage).into(holder.userProfileImage);
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Oops! Failed to load status image", Toast.LENGTH_SHORT).show();
        }

        holder.tvUserName.setText(statusSenderName);

        holder.statusListConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToViewStatusActivity(statusId);
            }
        });

    }

    private void sendToViewStatusActivity(String statusId)
    {
        Intent intent = new Intent(context, ViewStatusActivity.class);
        intent.putExtra("statusId",statusId);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return modelStatusList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tvUserName, tvStatusTimestamp;
        private ImageView userProfileImage;
        private ConstraintLayout statusListConstraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvStatusTimestamp = itemView.findViewById(R.id.tvStatusTimestamp);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            statusListConstraintLayout = itemView.findViewById(R.id.statusListConstraintLayout);

        }
    }

    private String getTime(String timestamp)
    {
        Long ts = Long.parseLong(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:a");
        String time = sdf.format(new Date(ts));
        return time;
    }

}
