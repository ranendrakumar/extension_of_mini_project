package com.sircrrcoeit.crrithandbook.filters;

import android.widget.Filter;

import com.sircrrcoeit.crrithandbook.models.ModelPdf;
import com.sircrrcoeit.crrithandbook.adapters.AdapterPdfAdmin;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
    //array list
    ArrayList<ModelPdf> filterList;
    //adapter
    AdapterPdfAdmin adapterPdfAdmin;
    //constructor

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value not null
        if (constraint !=null && constraint.length()>0){
            //avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();
            for (int i=0;i<filterList.size();i++){
                //validate
                if (filterList.get(i).getDescription().toUpperCase().contains(constraint)){
                    //add to filter
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter

        adapterPdfAdmin.pdfArrayList=(ArrayList<ModelPdf>)results.values;
        //notify changes
        adapterPdfAdmin.notifyDataSetChanged();

    }
}
