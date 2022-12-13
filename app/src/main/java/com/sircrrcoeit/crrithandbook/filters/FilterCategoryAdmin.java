package com.sircrrcoeit.crrithandbook.filters;

import android.widget.Filter;

import com.sircrrcoeit.crrithandbook.models.ModelCategory;
import com.sircrrcoeit.crrithandbook.adapters.AdapterCategoryAdmin;

import java.util.ArrayList;

public class FilterCategoryAdmin extends Filter {
    //array list
    ArrayList<ModelCategory> filterList;
    //adapter
    AdapterCategoryAdmin adapterCategoryAdmin;
    //constructor

    public FilterCategoryAdmin(ArrayList<ModelCategory> filterList, AdapterCategoryAdmin adapterCategoryAdmin) {
        this.filterList = filterList;
        this.adapterCategoryAdmin = adapterCategoryAdmin;
    }



    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value not null
        if (constraint !=null && constraint.length()>0){
            //avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();
            for (int i=0;i<filterList.size();i++){
                //validate
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)){
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

        adapterCategoryAdmin.categoryArrayList=(ArrayList<ModelCategory>)results.values;
        //notify changes
        adapterCategoryAdmin.notifyDataSetChanged();

    }
}
