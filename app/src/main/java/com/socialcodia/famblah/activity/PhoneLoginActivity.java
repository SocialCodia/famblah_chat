package com.socialcodia.famblah.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.famblah.R;
import com.socialcodia.famblah.storage.Constants;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    PhoneAuthProvider phoneAuthProvider;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    String sentCode;
    String mobileNumber;

    private EditText inputContact, inputOtp;
    private Button btnGenerateOtp, btnVerifyOtp;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        phoneAuthProvider = PhoneAuthProvider.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Users");
        mUser = mAuth.getCurrentUser();

        //Init
        inputContact = findViewById(R.id.inputContact);
        inputOtp = findViewById(R.id.inputOtp);
        btnGenerateOtp = findViewById(R.id.btnGenerateOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        textView = findViewById(R.id.textView);

        if (mUser!=null)
        {
            sendToHome();
        }

        btnGenerateOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateContact();
            }
        });

        btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateOtp();
            }
        });
    }

    //Validating the otp which is entered by user
    private void ValidateOtp()
    {
        String otp = inputOtp.getText().toString().trim();
        if (otp.isEmpty())
        {
            inputOtp.setError("Enter Otp");
            inputOtp.requestFocus();
        }
        else if (otp.length()<6)
        {
            inputOtp.setError("Enter Valid Otp");
            inputOtp.requestFocus();
        }
        else
        {
            VerifyOtp(otp);
        }
    }

    //verifying the otp entered by user to the the which is sent to the users
    private void VerifyOtp(String otp)
    {
        PhoneAuthCredential phoneAuthCredential = phoneAuthProvider.getCredential(sentCode,otp);
        signInWithPhoneAuthCredential(phoneAuthCredential);
    }


    //Validating the mobile number which is entered by user
    private void ValidateContact()
    {
        mobileNumber = inputContact.getText().toString().trim();
        if (mobileNumber.isEmpty())
        {
            inputContact.setError("Enter Mobile Number");
            inputContact.requestFocus();
        }
        else
            if (mobileNumber.length()<10)
            {
                inputContact.setError("Enter Valid Mobile Number");
                inputContact.requestFocus();
            }
            else
            {
                String fullMobileNumber = "+91"+mobileNumber;
                GenerateOtp(fullMobileNumber);
            }
    }

    //Generating and sending otp to user mobile number
    private void GenerateOtp(String fullMobileNumber)
    {
        btnGenerateOtp.setEnabled(false);
        phoneAuthProvider.verifyPhoneNumber(
                fullMobileNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
    {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential);
            btnGenerateOtp.setEnabled(true);
            Toast.makeText(PhoneLoginActivity.this, "Sign in complete with onVerificationCompleted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(PhoneLoginActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
            btnGenerateOtp.setEnabled(true);
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            sentCode = s;
            btnGenerateOtp.setVisibility(View.GONE);
            inputContact.setVisibility(View.GONE);
            textView.setText("Waiting to automatically detect and SMS sent to +91 "+mobileNumber);
            btnVerifyOtp.setVisibility(View.VISIBLE);
            inputOtp.setVisibility(View.VISIBLE);
        }
    };

    private void updateProfile(final FirebaseUser firebaseUser)
    {
        if (firebaseUser!=null)
        {
            mRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        checkLoginState(firebaseUser);
                    }
                    else
                    {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put(Constants.USER_MOBILE,mobileNumber);
                        map.put(Constants.USER_BIO,"Hi there! I am using Famblah");
                        map.put(Constants.USER_ID,firebaseUser.getUid());
                        map.put(Constants.USER_IMAGE,"");
                        map.put(Constants.USER_STATUS,"offline");
                        map.put(Constants.LOGIN_STATE,0);
                        map.put(Constants.TYPING_STATUS,"fambluh");
                        mRef.child(firebaseUser.getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(PhoneLoginActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(PhoneLoginActivity.this, "Profile Update Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),"Oops Something went wrong",Toast.LENGTH_LONG).show();
                }
            });
        }
        else
        {
            Toast.makeText(this, "Auth is null", Toast.LENGTH_SHORT).show();
        }
    }


    private  void signInWithPhoneAuthCredential( PhoneAuthCredential credential)
    {
        btnGenerateOtp.setEnabled(false);
        btnVerifyOtp.setEnabled(false);
            mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        btnGenerateOtp.setEnabled(true);
                        btnVerifyOtp.setEnabled(true);
                        checkLoginState(firebaseUser);
                        Toast.makeText(PhoneLoginActivity.this, "SignIn Successful", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        btnGenerateOtp.setEnabled(true);
                        btnVerifyOtp.setEnabled(true);
                        Toast.makeText(PhoneLoginActivity.this, "Failed with credential signin", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void sendToHome()
    {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void sendToSecondStepUpdateProfileActivity()
    {
        Intent intent = new Intent(getApplicationContext(),SecondStepUpdateProfile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void checkLoginState(final FirebaseUser firebaseUser)
    {
        if (firebaseUser!=null)
        {
            mRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        int loginState = dataSnapshot.child(Constants.LOGIN_STATE).getValue(int.class);
                        Toast.makeText(PhoneLoginActivity.this, "The current login state is "+loginState, Toast.LENGTH_SHORT).show();
                        if (loginState==0)
                        {
                            sendToSecondStepUpdateProfileActivity();
                        }
                        else
                        {
                            sendToHome();
                        }
                    }
                    else
                    {
                        updateProfile(firebaseUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), "Auth Is Null Check Login State", Toast.LENGTH_SHORT).show();
        }
    }

}
