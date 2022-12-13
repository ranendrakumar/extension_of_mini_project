package com.sircrrcoeit.crrithandbook.filters;

import android.widget.Filter;

import com.sircrrcoeit.crrithandbook.adapters.AdapterCategoryFacultyandStudent;
import com.sircrrcoeit.crrithandbook.adapters.AdapterPdfFacultyandStudent;
import com.sircrrcoeit.crrithandbook.models.ModelCategory;
import com.sircrrcoeit.crrithandbook.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfFacultyandStudent extends Filter {
    //array list
    ArrayList<ModelPdf> filterList;
    //adapter
    AdapterPdfFacultyandStudent adapterPdfFacultyandStudent;
    //constructor

    public FilterPdfFacultyandStudent(ArrayList<ModelPdf> filterList, AdapterPdfFacultyandStudent adapterPdfFacultyandStudent) {
        this.filterList = filterList;
        this.adapterPdfFacultyandStudent = adapterPdfFacultyandStudent;
    }

    public FilterPdfFacultyandStudent(ArrayList<ModelCategory> filterList, AdapterCategoryFacultyandStudent adapterCategoryFacultyandStudent) {
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

        adapterPdfFacultyandStudent.pdfArrayList=(ArrayList<ModelPdf>)results.values;
        //notify changes
        adapterPdfFacultyandStudent.notifyDataSetChanged();

    }
}
