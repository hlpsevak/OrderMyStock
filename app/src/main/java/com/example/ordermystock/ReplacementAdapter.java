package com.example.ordermystock;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.PreferenceChangeEvent;

public class ReplacementAdapter extends RecyclerView.Adapter<ReplacementAdapter.ReplacementViewHolder> implements Filterable {

    private ArrayList<DocumentSnapshot> prodDocsList;
    private ArrayList<DocumentSnapshot> prodDocsListCopy;
    private ArrayList<byte[]> prodImagesList ;
    ArrayList<byte[]> prodImagesListCopy;
    public LayoutInflater layoutInflater;
    Map<String,String> mapOrderDetails = new HashMap<>();
    static Map<String, Map> mapOrders;
    DocumentSnapshot currComp;
    ViewGroup mparent;
    SharedPreferences prefs;
    String comporshop;
    ArrayList<byte[]> filterePicsList;


    public ReplacementAdapter(Context context, ArrayList<DocumentSnapshot> arrdocs, ArrayList<byte[]> arrimages){
        prodDocsList = new ArrayList<>(arrdocs);
        prodDocsListCopy = new ArrayList<>(arrdocs);
        prodImagesList = new ArrayList<>(arrimages);
        prodImagesListCopy =  new ArrayList<>(arrimages);

        layoutInflater = LayoutInflater.from(context);
        //currComp = currDoc;
        mapOrders = new HashMap<>();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        comporshop = prefs.getString("comporshop","shops");

    }

    public ReplacementAdapter(DocumentSnapshot currCompDoc){
        currComp = currCompDoc;
    }

    @NonNull
    @Override
    public ReplacementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = layoutInflater.inflate(R.layout.replacement_list, parent, false);
        mparent = parent;
        return new ReplacementViewHolder(mItemView, ReplacementAdapter.this);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplacementViewHolder holder, int position) {
        byte[] currImage = prodImagesList.get(position);
        holder.mImageView.setImageBitmap(BitmapFactory.decodeByteArray(currImage,0, currImage.length));
        holder.tvProdName.setText("Product name: "+prodDocsList.get(position).getString("prodname"));
        holder.tvReplaceReason.setText("Reason: "+prodDocsList.get(position).getString("reason"));
        holder.tvProdPrice.setText("Product Price: "+prodDocsList.get(position).getString("prodprice"));
        holder.tvStatus.setText("Request status: "+prodDocsList.get(position).getString("status"));
        if(comporshop.equals("shops")){
            holder.btnReject.setVisibility(View.GONE);
            holder.btnAccept.setVisibility(View.GONE);
            holder.tvShopName.setVisibility(View.GONE);
        }
        else
            holder.tvShopName.setText("Shop name: "+prodDocsList.get(position).getString("shopname") );
        if(prodDocsList.get(position).getString("reject").equals("Mark as Replaced")){
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setText("Mark as Replaced");
        }
        if(prodDocsList.get(position).getString("status").equals("Rejected") ||
                prodDocsList.get(position).getString("status").equals("Replaced")){
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return prodDocsList.size();
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
                filteredList.addAll(prodDocsListCopy);
                filterePicsList.addAll(prodImagesListCopy);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(DocumentSnapshot doc : prodDocsListCopy){
                    if(doc.get("prodname").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(doc);
                        filterePicsList.add(prodImagesListCopy.get(prodDocsListCopy.indexOf(doc)));
                    }
                    else if(doc.get("reason").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(doc);
                        filterePicsList.add(prodImagesListCopy.get(prodDocsListCopy.indexOf(doc)));
                    }
                    else if(doc.get("status").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(doc);
                        filterePicsList.add(prodImagesListCopy.get(prodDocsListCopy.indexOf(doc)));
                    }
                }
            }

            FilterResults Docresults = new FilterResults();
            Docresults.values = filteredList;

            return Docresults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            prodDocsList.clear();
            prodDocsList.addAll((ArrayList)results.values);
            FilterResults imgResults = new FilterResults();
            imgResults.values = filterePicsList;
            prodImagesList.clear();
            prodImagesList.addAll((ArrayList)imgResults.values);

            notifyDataSetChanged();
        }
    };

    public class ReplacementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mImageView;
        TextView tvProdName, tvReplaceReason, tvProdPrice, tvStatus, tvShopName;
        Button btnAccept, btnReject;
        ProductListForShopAdapter productListForShopAdapter;
        int posItemClicked;
        String qnty;
        AlertDialog alertDialog;
        FloatingActionButton fabPlaceOrder;
        int conf = 0;


        public ReplacementViewHolder(@NonNull View itemView, ReplacementAdapter replacementAdapter) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.iv_replace_productimage);
            tvProdName = itemView.findViewById(R.id.tv_replace_productname);
            tvReplaceReason = itemView.findViewById(R.id.tv_replace_reason);
            tvProdPrice = itemView.findViewById(R.id.tv_replace_productprice);
            tvStatus = itemView.findViewById(R.id.tv_replace_status);
            tvShopName = itemView.findViewById(R.id.tv_replace_shopname);
            btnAccept = itemView.findViewById(R.id.btn_btn_replace_accept);
            btnReject = itemView.findViewById(R.id.btn_replace_reject);
            btnAccept.setOnClickListener(this::onClick);
            btnReject.setOnClickListener(this::onClick);

            this.productListForShopAdapter = productListForShopAdapter;
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_btn_replace_accept){
                acceptReplacement();
            }
            else if(v.getId() == R.id.btn_replace_reject){
                if(btnReject.getText().equals("Reject"))
                    RejectReplacement();
                else
                    MarkAsReplaced();
            }
        }

        public void acceptReplacement(){
            int pos = getLayoutPosition();
            btnReject.setText("Mark as Replaced");
            btnAccept.setVisibility(View.GONE);
            tvStatus.setText("Accepted");

            DocumentSnapshot doc = prodDocsList.get(pos);
            Map<String, Object> currMap =  doc.getData();
            currMap.put("status","Accpeted");
            currMap.put("reject","Mark as Replaced");
            String currShopId = currMap.get("shopid").toString();

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("ordermystock").document("userdoc")
                    .collection("companies").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("replacements").document(prodDocsList.get(pos).getId())
                    .set(currMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FirebaseFirestore firebaseFirestore1 =  FirebaseFirestore.getInstance();
                    firebaseFirestore1.collection("ordermystock").document("userdoc")
                            .collection("shops").document(currShopId).collection("replacements")
                            .document(prodDocsList.get(pos).getId())
                            .set(currMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(itemView.getContext(), "Replacement accepted", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        public void RejectReplacement(){
            int pos = getLayoutPosition();
            btnReject.setVisibility(View.GONE);
            btnAccept.setVisibility(View.GONE);
            tvStatus.setText("Rejected");

            DocumentSnapshot doc = prodDocsList.get(pos);
            Map<String, Object> currMap =  doc.getData();
            currMap.put("status","Rejected");
            currMap.put("reject","Mark as Replaced");
            String currShopId = currMap.get("shopid").toString();

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("ordermystock").document("userdoc")
                    .collection("companies").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("replacements").document(prodDocsList.get(pos).getId())
                    .set(currMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FirebaseFirestore firebaseFirestore1 =  FirebaseFirestore.getInstance();
                    firebaseFirestore1.collection("ordermystock").document("userdoc")
                            .collection("shops").document(currShopId).collection("replacements")
                            .document(prodDocsList.get(pos).getId())
                            .set(currMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(itemView.getContext(), "Replacement Rejected", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        public void MarkAsReplaced(){
            int pos = getLayoutPosition();
            btnReject.setVisibility(View.GONE);
            btnAccept.setVisibility(View.GONE);
            tvStatus.setText("Replaced");

            DocumentSnapshot doc = prodDocsList.get(pos);
            Map<String, Object> currMap =  doc.getData();
            currMap.put("status","Replaced");
            currMap.put("reject","Mark as Replaced");
            String currShopId = currMap.get("shopid").toString();

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("ordermystock").document("userdoc")
                    .collection("companies").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("replacements").document(prodDocsList.get(pos).getId())
                    .set(currMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FirebaseFirestore firebaseFirestore1 =  FirebaseFirestore.getInstance();
                    firebaseFirestore1.collection("ordermystock").document("userdoc")
                            .collection("shops").document(currShopId).collection("replacements")
                            .document(prodDocsList.get(pos).getId())
                            .set(currMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(itemView.getContext(), "Replacement completed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}
