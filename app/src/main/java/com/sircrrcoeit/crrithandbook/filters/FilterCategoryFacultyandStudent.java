package com.sircrrcoeit.crrithandbook.filters;

import android.widget.Filter;

import com.sircrrcoeit.crrithandbook.adapters.AdapterCategoryFacultyandStudent;
import com.sircrrcoeit.crrithandbook.models.ModelCategory;

import java.util.ArrayList;

public class FilterCategoryFacultyandStudent extends Filter {

    ArrayList<ModelCategory> filterList;

    AdapterCategoryFacultyandStudent adapterCategoryFacultyandStudent;

    public FilterCategoryFacultyandStudent(ArrayList<ModelCategory> filterList, AdapterCategoryFacultyandStudent adapterCategoryFacultyandStudent) {
        this.filterList = filterList;
        this.adapterCategoryFacultyandStudent = adapterCategoryFacultyandStudent;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if(constraint !=null&&constraint.length()>0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filterModels = new ArrayList<>();
            for(int i=0;i<filterList.size();i++){
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)){
                    filterModels.add(filterList.get(i));

                }
            }

            results.count = filterModels.size();
            results.values = filterModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults results) {
        adapterCategoryFacultyandStudent.categoryArrayList1 = (ArrayList<ModelCategory>)results.values;
        adapterCategoryFacultyandStudent.notifyDataSetChanged();
    }
}
