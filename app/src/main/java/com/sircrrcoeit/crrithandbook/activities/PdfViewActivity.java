package com.sircrrcoeit.crrithandbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sircrrcoeit.crrithandbook.Constants;
import com.sircrrcoeit.crrithandbook.R;
import com.sircrrcoeit.crrithandbook.databinding.ActivityPdfViewBinding;

public class PdfViewActivity extends AppCompatActivity {

    private ActivityPdfViewBinding binding;

    private String expId;

    private static final String TAG = "PDF_VIEW_TAG";

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
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        expId = intent.getStringExtra("expId");
        Log.d(TAG, "onCreate: ExpId:"+expId);

        loadBookDetails();

        //back btn
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get Pdf URL...");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Labs");
        ref.child(expId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get pdf url

                        String pdfUrl =""+snapshot.child("url").getValue();
                        Log.d(TAG, "onDataChange: PDF URL:"+pdfUrl);
                        //Load
                        loadBookFromUrl(pdfUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBookFromUrl(String pdfUrl) {
        Log.d(TAG, "loadBookFromUrl: Get PDF from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_SIZE_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        binding.pdfView.fromBytes(bytes)
                                .swipeHorizontal(false)
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        int currentPage =(page+1);
                                        binding.toolbarSubtitleTv.setText(currentPage+"/"+pageCount);// toolbartitletv
                                        Log.d(TAG, "onPageChanged: "+currentPage+"/"+pageCount);
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.d(TAG, "onError: "+t.getMessage());
                                        Toast.makeText(PdfViewActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                        Toast.makeText(PdfViewActivity.this, "Error on page"+page+" "+t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .load();
                        binding.progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        //fail
                        binding.progressBar.setVisibility(View.GONE);

                    }
                });
    }
}