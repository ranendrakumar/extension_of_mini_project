package com.sircrrcoeit.crrithandbook.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.sircrrcoeit.crrithandbook.R;
import com.sircrrcoeit.crrithandbook.activities.PdfEditActivity;
import com.sircrrcoeit.crrithandbook.databinding.RowCategoryAdminBinding;
import com.sircrrcoeit.crrithandbook.filters.FilterCategoryAdmin;
import com.sircrrcoeit.crrithandbook.models.ModelCategory;
import com.sircrrcoeit.crrithandbook.activities.PdfListAdminActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdapterCategoryAdmin extends RecyclerView.Adapter<AdapterCategoryAdmin.HolderCategory> implements Filterable {

    public Context context;
    public ArrayList<ModelCategory> categoryArrayList;
    ArrayList<ModelCategory> filterList;

    private RowCategoryAdminBinding binding;

    //instance of filter class
    public FilterCategoryAdmin filter;

    public AdapterCategoryAdmin(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind
        binding = RowCategoryAdminBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        //get data
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        //set data
        holder.categoryTv.setText(category);
        //handel delete click

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, ""+category, Toast.LENGTH_SHORT).show();
                //confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this Lab?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //begin delete
                                Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                                deleteCategory(model,holder);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });


        //Edit button

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogPlus dialogPlus = DialogPlus.newDialog(view.getContext())
                        .setContentHolder(new ViewHolder(R.layout.dialogplus))
                        .setExpanded(true,1500)
                        .create();
                //dialogPlus.show();

                View myview = dialogPlus.getHolderView();
                EditText categoryEt = myview.findViewById(R.id.categoryEt);
                Button update = myview.findViewById(R.id.updateBtn);

                categoryEt.setText(model.getCategory());

                dialogPlus.show();

                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Map<String,Object> map = new HashMap<>();
                        map.put("category",categoryEt.getText().toString());

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
                        ref.child(id)
                                .updateChildren(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dialogPlus.dismiss();
                                        Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialogPlus.dismiss();
                                        Toast.makeText(context, "Failed to Update"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }
                });
            }
        });



        //iteam click

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId",id);
                intent.putExtra("categoryTitle",category);
                context.startActivity(intent);
            }
        });

    }

    private void deleteCategory(ModelCategory model, HolderCategory holder) {
        //get the id
        String id = model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter = new FilterCategoryAdmin(filterList,this);
        }
        return filter;
    }

    class HolderCategory extends RecyclerView.ViewHolder{

        TextView categoryTv;
        ImageButton deleteBtn;
        ImageButton editBtn;
        public HolderCategory(@NonNull View itemView) {
            super(itemView);
            //init ui views
            categoryTv = binding.categoryTv;
            deleteBtn = binding.deleteBtn;
            editBtn = binding.editBtn;
        }
    }

}
