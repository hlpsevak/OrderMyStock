package com.example.ordermystock;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductViewHolder> {

    private final ArrayList<DocumentSnapshot> prodDocsList;
    private final ArrayList<byte[]> prodImagesList;
    public LayoutInflater layoutInflater;

    public ProductListAdapter(Context context, ArrayList<DocumentSnapshot> arrdocs, ArrayList<byte[]> arrimgs){
        prodDocsList = arrdocs;
        prodImagesList = arrimgs;
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
