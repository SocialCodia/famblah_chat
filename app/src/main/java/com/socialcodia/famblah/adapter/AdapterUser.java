package com.socialcodia.famblah.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.PointerIconCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.socialcodia.famblah.R;
import com.socialcodia.famblah.activity.ChatActivity;
import com.socialcodia.famblah.model.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.ViewHolder> {

    Context context;

    public AdapterUser(Context context, List<ModelUser> modelUserList) {
        this.context = context;
        this.modelUserList = modelUserList;
    }

    List<ModelUser> modelUserList;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_user,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        String name = modelUserList.get(position).getName();
        String bio = modelUserList.get(position).getBio();
        String image = modelUserList.get(position).getImage();

        holder.tvUserName.setText(name);
        holder.tvUserBio.setText(bio);
        try {
            Picasso.get().load(image).into(holder.userProfileImage);
        }
        catch (Exception e)
        {
            Picasso.get().load(R.drawable.person_female);
        }

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToChat(position);
            }
        });

    }

    private void sendToChat(int position)
    {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("hisUid",modelUserList.get(position).getUid());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return modelUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tvUserName,tvUserBio;
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
