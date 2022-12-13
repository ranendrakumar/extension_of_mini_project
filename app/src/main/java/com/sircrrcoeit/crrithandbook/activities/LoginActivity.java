package com.sircrrcoeit.crrithandbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sircrrcoeit.crrithandbook.R;
import com.sircrrcoeit.crrithandbook.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private FirebaseAuth firebaseAuth;
    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.primary));
        }




        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//init fireauth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //go to registration
        //binding.noAccount.setOnClickListener(new View.OnClickListener() {
            //@Override
           // public void onClick(View view) {
             //   startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
           // }
        //});

        //handle login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }
    private String email = "",password = "";
    private void validateData() {
        //validation before login
        //get data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        //validate data

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid Email",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Enter Your Password",Toast.LENGTH_SHORT).show();
        }
        else{
            //data validated being login
            loginUser();
        }
    }

    private void loginUser() {
        // show progress
        progressDialog.setMessage("Login..");
        progressDialog.show();
        //login user
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //login success , check if user is user or admin
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //login failed
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void checkUser() {
        progressDialog.setMessage("Checking User...");
        //check if user is user or admin from realtime data base
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        //check in DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        //get user type
                        String userType = ""+snapshot.child("userType").getValue();
                        //check user type
                        if (userType.equals("user")){
                            //this is for student open student dashboard
                            startActivity(new Intent(LoginActivity.this, DashboardStudentActivity.class));
                            //startActivity(new Intent(LoginActivity.this, LabsUserFragment.class));
                            finish();
                        }else if (userType.equals("faculty")){
                            //this is for faculty open faculty dashboard
                            startActivity(new Intent(LoginActivity.this, DashboardFacultyActivity.class));
                            finish();
                        }
                        else if (userType.equals("admin")){
                            //this is for admin open admin dashboard
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }
}