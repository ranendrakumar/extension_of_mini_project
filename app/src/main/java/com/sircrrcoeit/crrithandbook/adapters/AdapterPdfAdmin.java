package com.sircrrcoeit.crrithandbook.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.github.barteksc.pdfviewer.PDFView;
import com.sircrrcoeit.crrithandbook.activities.PdfViewActivity;
import com.sircrrcoeit.crrithandbook.filters.FilterPdfAdmin;
import com.sircrrcoeit.crrithandbook.models.ModelPdf;
import com.sircrrcoeit.crrithandbook.MyApplication;
import com.sircrrcoeit.crrithandbook.activities.PdfDetailedActivity;
import com.sircrrcoeit.crrithandbook.activities.PdfEditActivity;
import com.sircrrcoeit.crrithandbook.databinding.RowPdfAdminBinding;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {//

    private Context context;//context
    public ArrayList<ModelPdf> pdfArrayList,filterlist;//array holder
    private RowPdfAdminBinding binding;//vb
    private FilterPdfAdmin filter;
    private static final String TAG ="PDF_ADAPTER_TAG";
    private ProgressDialog progressDialog;

    //con
    public AdapterPdfAdmin(Context context1, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context1;
        this.pdfArrayList = pdfArrayList;
        this.filterlist = pdfArrayList;
        progressDialog = new ProgressDialog(context1);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);


        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {

        //get&set data
        //get data
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        long timestamp = model.getTimestamp();

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);



        String date = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            date = MyApplication.formatTimestamp(Long.parseLong(String.valueOf(timestamp)));
        }


        holder.dateTv.setText(date);

        //load extra

        //loadCategory(model,holder);
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );

        //loadFromUrl(model,holder);
        MyApplication.loadFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar
        );

        //loadSize(model,holder);
        MyApplication.loadSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );



        //more options edit,delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsDalog(model,holder);
            }
        });

        //change to pdf view if possible
        //openpdf
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailedActivity.class);
                intent.putExtra("expId",pdfId);
                context.startActivity(intent);
            }
        });

    }



    private void moreOptionsDalog(ModelPdf model, HolderPdfAdmin holder) {

        String expId = model.getId();
        String expUrl = model.getUrl();
        String expTitle = model.getTitle();

        String[] options = {"Edit","Delete"};

        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //action
                        if (which==0){
                            //edit
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("expId",expId);
                            context.startActivity(intent);
                        }
                        else if (which==1){
                            //delete
                            MyApplication.deleteExp(
                                    context,
                                    ""+expId,
                                    ""+expUrl,
                                    ""+expTitle
                            );
                            //deleteExp(model,holder);
                        }
                    }
                })
                .show();
    }

//code is removed

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    //filter method
    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterlist,this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        //ui views
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv,descriptionTv,categoryTv,sizeTv,dateTv;
        ImageButton moreBtn;
        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            //init
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;
        }
    }
}