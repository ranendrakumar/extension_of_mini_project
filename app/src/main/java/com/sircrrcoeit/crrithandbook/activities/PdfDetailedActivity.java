package com.sircrrcoeit.crrithandbook.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sircrrcoeit.crrithandbook.MyApplication;
import com.sircrrcoeit.crrithandbook.R;
import com.sircrrcoeit.crrithandbook.databinding.ActivityPdfDetailedBinding;

public class PdfDetailedActivity extends AppCompatActivity {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private ActivityPdfDetailedBinding binding;
    String expId,expTitle,expUrl ;

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
        binding = ActivityPdfDetailedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent
        Intent intent = getIntent();
        expId = intent.getStringExtra("expId");


        binding.downloadBtn.setVisibility(View.GONE);


        loadExpDetails();
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        //on item click
        binding.rlclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(PdfDetailedActivity.this, PdfViewActivity.class);
                intent1.putExtra("expId",expId);
                startActivity(intent1);
            }
        });




        /*//open btn
        binding.readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(PdfDetailedActivity.this, PdfViewActivity.class);
                intent1.putExtra("expId",expId);
                startActivity(intent1);
            }
        });*/


        //Download btn
        binding.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG_DOWNLOAD, "onClick: Checking Permissions");
                if (ContextCompat.checkSelfPermission(PdfDetailedActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG_DOWNLOAD, "onClick:Permission already granted can download pdf");
                    MyApplication.downloadPdf(PdfDetailedActivity.this,""+expId,""+expTitle,""+expUrl);
                }
                else {
                    Log.d(TAG_DOWNLOAD, "onClick: permission not granted request for permiission");
                    requestPermissionsLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

    }
    //request storage permissions
    private ActivityResultLauncher<String> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
                if (isGranted){
                    Log.d(TAG_DOWNLOAD, "Permissions Granted");
                    MyApplication.downloadPdf(
                            this,
                            ""+expId,
                            ""+expTitle,
                            ""+expUrl
                    );
                }

                else {
                    Log.d(TAG_DOWNLOAD, "Permission was Denied....");
                    Toast.makeText(this, "Permission was Denied....", Toast.LENGTH_SHORT).show();
                }

            });

    private void loadExpDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Labs");
        reference.child(expId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        expTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        expUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        binding.downloadBtn.setVisibility(View.VISIBLE);

                        //String date = MyApplication.formatTimeStamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );

                        MyApplication.loadFromUrlSinglePage(""+expUrl,
                                ""+expTitle,
                                binding.pdfView,
                                binding.progressBar
                        );

                        MyApplication.loadSize(""+expUrl,
                                ""+expTitle,
                                binding.sizeTv
                        );



                        String date = "";
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            date = MyApplication.formatTimestamp(Long.parseLong(timestamp));
                        }


                        binding.titleTv.setText(expTitle);
                        binding.descriptionTv.setText(description);
                        binding.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}