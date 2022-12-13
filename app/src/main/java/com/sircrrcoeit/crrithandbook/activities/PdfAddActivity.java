package com.sircrrcoeit.crrithandbook.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sircrrcoeit.crrithandbook.R;
import com.sircrrcoeit.crrithandbook.databinding.ActivityPdfAddBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Uri pdfuri = null;
    //array list holder
    private ArrayList<String> categoryTitleArrayList,categoryIdArrayList;
    private static final int PDF_PICK_CODE = 1000;
    private static final String TAG = "ADD_PDF_TAG";

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
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        //progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCancelable(false);

        //backbtn
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //attach pdf
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        //pick labs
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        //upload pdf click
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    private String title ="",description = "";

    private void validateData() {
        Log.d(TAG, "validateData: validating data...");
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();


        //validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Please enter the lab name...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Please enter the regulation...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Please select the lab...", Toast.LENGTH_SHORT).show();
        }
        else if (pdfuri==null){
            Toast.makeText(this, "Please attach the PDF file...", Toast.LENGTH_SHORT).show();
        }
        else {
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        //upload pdf to FB
        Log.d(TAG, "uploadPdfToStorage: uploading....");

        //show prog
        progressDialog.setMessage("Uploading Pdf...");
        progressDialog.show();

        //ts
        long timestamp = System.currentTimeMillis();

        //path of pdf in fire store
        String filePathAndName = "Labs/"+timestamp;

        //storage ref
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfuri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF uploaded to storage");
                        Log.d(TAG, "onSuccess: getting pdf uri");
                        //pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();

                        //upload to FB
                        uploadPdfInfotoDb(uploadedPdfUrl,timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: PDF upload failed due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "PDF upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void uploadPdfInfotoDb(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG, "uploadPdfToStorage: uploading Pdf info to firebase db...");


        progressDialog.setMessage("uploading Pdf info");

        String uid = firebaseAuth.getUid();

        //data to upload
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+selectedCategoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp",timestamp);

        //db ref
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Labs");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully uploaded...");
                        Toast.makeText(PdfAddActivity.this, "Successfully uploaded...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to db due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload to db due to"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading Labs...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();
        //db reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){

                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    //adding to arraylist
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId,selectedCategoryTitle;

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing lab picker dialog");
        String[] catagoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i< categoryTitleArrayList.size(); i++){
            catagoriesArray[i] = categoryTitleArrayList.get(i);
        }
        //alert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Select Lab")
                .setItems(catagoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //clicking on alert
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);
                        //set to tv
                        binding.categoryTv.setText(selectedCategoryTitle);

                        Log.d(TAG, "onClick: Selected lab:"+selectedCategoryId+""+selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Pdf"),PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF Picked");

                pdfuri = data.getData();

                Log.d(TAG, "onActivityResult: URI: "+pdfuri);
            }
        }
        else {
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}