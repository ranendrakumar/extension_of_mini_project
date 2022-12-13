package com.sircrrcoeit.crrithandbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sircrrcoeit.crrithandbook.R;
import com.sircrrcoeit.crrithandbook.databinding.ActivityCategoryAddBinding;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {
    private ActivityCategoryAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.primary));
            // window.setNavigationBarColor(this.getResources().getColor(R.color.blue));
        }

        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //configure dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        //handle click go back

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle being upload category
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }
    private String catgory="";

    private void validateData() {

        //Before adding validate data
        //get data
        catgory = binding.categoryEt.getText().toString().trim();
        //validate if not empty
        if (TextUtils.isEmpty(catgory)){
            Toast.makeText(this,"Please Enter the Lab Title",Toast.LENGTH_SHORT).show();
        }
        else {
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
        //show progress
        progressDialog.setMessage("Adding Lab...!");
        progressDialog.show();

        //get Timestamp
        long timestamp = System.currentTimeMillis();
        //setup info to add in firebase
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+catgory);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //add to firebasedb...DB Root Catagories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //lab add success
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this,"Lab Added Successfully",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //lab add failed
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }
}