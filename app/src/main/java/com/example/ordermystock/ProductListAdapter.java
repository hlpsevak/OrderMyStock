package com.example.ordermystock;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>implements Filterable {

    private final ArrayList<DocumentSnapshot> prodDocsList;
    ArrayList<DocumentSnapshot> prodDocsListCopy;
    private final ArrayList<byte[]> prodImagesList;
    ArrayList<byte[]> prodImagesListCopy;
    public LayoutInflater layoutInflater;
    ArrayList<byte[]> filterePicsList;

    public ProductListAdapter(Context context, ArrayList<DocumentSnapshot> arrdocs, ArrayList<byte[]> arrimgs){
        prodDocsList = new ArrayList<>(arrdocs);
        prodDocsListCopy = new ArrayList<>(arrdocs);
        prodImagesListCopy = new ArrayList<>(arrimgs);
        prodImagesList = new ArrayList<>(arrimgs);
        layoutInflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = layoutInflater.inflate(R.layout.product_list, parent, false);
        return new ProductViewHolder(mItemView,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        byte[] currImage = prodImagesList.get(position);
        holder.mImageView.setImageBitmap(BitmapFactory.decodeByteArray(currImage,0, currImage.length));
        holder.tvProdName.setText("Product name: "+prodDocsList.get(position).getString("prodname"));
        holder.tvProdDesc.setText("Description: "+prodDocsList.get(position).getString("proddesc"));
        holder.tvProdPrice.setText("Product price: "+prodDocsList.get(position).getString("prodprice"));

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
                    else if(doc.get("prodprice").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(doc);
                        filterePicsList.add(prodImagesListCopy.get(prodDocsListCopy.indexOf(doc)));
                    }
                    else if(doc.get("proddesc").toString().toLowerCase().contains(filterPattern)){
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

    public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mImageView;
        TextView tvProdName, tvProdDesc, tvProdPrice;
        ProductListAdapter mProductListAdapter;
        Button btnDelete;

        public ProductViewHolder(@NonNull View itemView, ProductListAdapter productListAdapter) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.iv_productimage);
            tvProdName = itemView.findViewById(R.id.tv_productname);
            tvProdDesc = itemView.findViewById(R.id.tv_productdesc);
            tvProdPrice = itemView.findViewById(R.id.tv_productprice);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
            btnDelete.setOnClickListener(this::onClick);

            mProductListAdapter = productListAdapter;
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnDeleteProduct){
                int position = getLayoutPosition();
                Log.d("position: : ", String.valueOf(position));

                AlertDialog confDelAlert = new AlertDialog.Builder(itemView.getContext()).create();
                confDelAlert.setTitle("Confirm Delete");
                confDelAlert.setMessage("Do you really want to delete the product ?");
                confDelAlert.setButton(AlertDialog.BUTTON_POSITIVE, "delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct(position);
                    }
                });
                confDelAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                confDelAlert.show();
            }
        }

        public void deleteProduct(int position){
            DocumentSnapshot docClicked = prodDocsList.get(position);
            String docId = docClicked.getId();

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("ordermystock").document("userdoc")
                    .collection("companies").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("products").document(docId).delete()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(itemView.getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();

                    AppCompatActivity appCompatActivity = (AppCompatActivity)itemView.getContext();
                    Fragment fragment = new ProductListForCompFrag();
                    FragmentTransaction ft = appCompatActivity.getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.framelayout_main, fragment).commit();
                    ft.addToBackStack(null);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
