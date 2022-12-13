package com.sircrrcoeit.crrithandbook;
import static com.sircrrcoeit.crrithandbook.Constants.MAX_SIZE_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
//    public static final String formatTimeStamp(long timestamp){
//        Calendar cal =Calendar.getInstance(Locale.ENGLISH);
//        cal.setTimeInMillis(timestamp);
//        //String date = DateFormat.format("dd/mm/yyyy",cal).toString();
//
//        return null;
//    }





    public static final String formatTimestamp(long timestamp){
        try {
            Date netDate =(new Date(timestamp));
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());
            return sfd.format(netDate);
        }catch (Exception e){
            return "date";
        }
    }







    public static void deleteExp(Context context,String expId,String expUrl,String expTitle) {
        String TAG = "DELETE_EXP_TAG";
        Log.d(TAG, "deleteLab: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait....");
        progressDialog.setMessage("Deleting"+expTitle+".....");
        progressDialog.show();

        Log.d(TAG, "deleteLab: Deleting from storage");



        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(expUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from storage");
                        Log.d(TAG, "onSuccess: Deleting info from DB");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Labs");
                        reference.child(expId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from DB");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from DB due to"+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public static void loadSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
       String TAG = "PDF_SIZE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+pdfTitle+" "+bytes);

                        double kb = bytes/1024;
                        double mb=kb/1024;

                        if (mb>=1){
                            sizeTv.setText(String.format("%.2f",mb)+"MB");
                        }
                        else if (kb>=1){
                            sizeTv.setText(String.format("%.2f",kb)+"KB");
                        }
                        else {
                            sizeTv.setText(String.format("%.2f",bytes)+"bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });
    }


    public static void loadFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar) {
        String TAG = "PDF_LOAD_SINGLE_TAG";

        StorageReference ref =FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_SIZE_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+pdfTitle+"successfully got the file");
                        //set to pdfv
                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {

                                        progressBar.setVisibility(View.INVISIBLE);

                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded");
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: failed getting file from url due to"+e.getMessage());
                    }
                });
    }


    public static void loadCategory(String categoryId,TextView categoryTv) {
        //catid

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();
                        categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public static void downloadPdf(Context context, String expId,String expTitle,String expUrl){
        Log.d(TAG_DOWNLOAD, "downloadPdf: Downloading Pdf");
        String nameWithExtension = expTitle+" "+expId+".pdf";
        Log.d(TAG_DOWNLOAD, "downloadPdf: NAME:"+nameWithExtension);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Downloading"+nameWithExtension+"...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        StorageReference storageReference =FirebaseStorage.getInstance().getReferenceFromUrl(expUrl);
        storageReference.getBytes(MAX_SIZE_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Pdf Downloaded");
                        Log.d(TAG_DOWNLOAD, "onSuccess: Saving PDF...");
                        saveDownloadedPdf(context,progressDialog,bytes,nameWithExtension,expId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to Download due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to Download due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static void saveDownloadedPdf(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String expId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedPdf: Saving Downloaded PDF");
        try {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdirs();
            String filePath = downloadFolder.getPath() + "/"+nameWithExtension;
            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved To Downloads Folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedPdf: Saved to Download Folder");
            progressDialog.dismiss();

        }
        catch (Exception e){
            Log.d(TAG_DOWNLOAD, "saveDownloadedPdf: Failed saving to Download Folder due to"+e.getMessage());
            Toast.makeText(context, "Failed saving to Download Folder due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }


}
