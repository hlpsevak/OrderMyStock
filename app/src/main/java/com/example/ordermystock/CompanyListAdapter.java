package com.example.ordermystock;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.FilterReader;
import java.util.ArrayList;
import java.util.List;

public class CompanyListAdapter extends RecyclerView.Adapter<CompanyListAdapter.CompanyViewHolder> implements Filterable {

    private  ArrayList<DocumentSnapshot> companyDocsList = new ArrayList<>();
    private ArrayList<DocumentSnapshot> companyDocsListCopy = new ArrayList<>();
    private ArrayList<byte[]> companyImagesList = new ArrayList<>();
    private ArrayList<byte[]> CompanyImagesListCopy = new ArrayList<>();
    ArrayList<byte[]> filterePicsList;
    public LayoutInflater layoutInflater;


    public CompanyListAdapter(Context context, ArrayList<DocumentSnapshot> namesList, ArrayList<byte[]> imagesList){
        layoutInflater = LayoutInflater.from(context);
        companyDocsList = namesList;
        companyDocsListCopy = namesList;
        companyImagesList = imagesList;
        CompanyImagesListCopy  = imagesList;
    }

    @NonNull
    @Override
    public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mItemView = layoutInflater.inflate(R.layout.display_list, parent, false);
        return new CompanyViewHolder(mItemView,this);

    }

    @Override
    public void onBindViewHolder(@NonNull CompanyViewHolder holder, int position) {
        String mCurrentCompName = companyDocsList.get(position).getString("name");
        byte []mCurrentCompImage = companyImagesList.get(position);

        holder.textView.setText(mCurrentCompName);
        holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(mCurrentCompImage, 0, mCurrentCompImage.length));
    }

    @Override
    public int getItemCount() {
        return companyDocsList.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {


        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<DocumentSnapshot> filteredList = new ArrayList<>();
            filterePicsList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(companyDocsListCopy);
                filterePicsList.addAll(CompanyImagesListCopy);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(DocumentSnapshot doc : companyDocsListCopy){
                    Log.d("For > "," DOC ENTERED");
                    if(doc.get("name").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(doc);
                        filterePicsList.add(CompanyImagesListCopy.get(companyDocsListCopy.indexOf(doc)));
                        Log.d("Constraint=:: IF :: ",constraint.toString());
                    }
                }
            }

            FilterResults Docresults = new FilterResults();
            Docresults.values = filteredList;


            return Docresults;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            companyDocsList.clear();
            companyDocsList.addAll((ArrayList)results.values);
            FilterResults imgResults = new FilterResults();
            imgResults.values = filterePicsList;
            companyImagesList.clear();
            companyImagesList.addAll((ArrayList)imgResults.values);

            notifyDataSetChanged();
        }
    };

    public class CompanyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView imageView;
        public final TextView textView;
        final CompanyListAdapter companyListAdapter;
        View mItemView;

        public CompanyViewHolder(View itemView, CompanyListAdapter companyListAdapter){
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_companyproflist);
            textView = itemView.findViewById(R.id.tv_companyproflist);
            this.companyListAdapter = companyListAdapter;
            imageView.setOnClickListener((View.OnClickListener) this);
            mItemView = itemView;
        }

        @Override
        public void onClick(View v) {
            int mPosition = getLayoutPosition();
            Log.d("POSITION",mPosition+"");
            DocumentSnapshot docClicked = companyDocsList.get(mPosition);
            AppCompatActivity appCompatActivity = (AppCompatActivity)mItemView.getContext();
            Fragment fragment = new DisplayProductsForShopFrag(docClicked);
            FragmentTransaction ft = appCompatActivity.getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.framelayout_main, fragment).commit();
            ft.addToBackStack(null);
        }
    }
}
