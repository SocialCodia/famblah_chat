package com.socialcodia.famblah.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.socialcodia.famblah.R;
import com.squareup.picasso.Picasso;

public class ViewImageActivity extends AppCompatActivity {

    Intent intent;
    private ImageView imageView;
    ActionBar actionBar;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        imageView = findViewById(R.id.imageView);

        getSupportActionBar().setTitle("View Image");
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Get data from intent
        intent = getIntent();
        String image = intent.getStringExtra("image");
        try {
            Picasso.get().load(image).into(imageView);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Error Message: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            Picasso.get().load(R.drawable.person_male).into(imageView);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
