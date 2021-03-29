package com.example.ordermystock;

import android.app.Dialog;
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
import com.google.android.material.internal.ContextUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProductListForShopAdapter extends RecyclerView.Adapter<ProductListForShopAdapter.ProductListForShopViewHolder> implements Filterable {

        private ArrayList<DocumentSnapshot> prodDocsList = new ArrayList<>();
        private ArrayList<DocumentSnapshot> prodDocsListCopy = new ArrayList<>();
        private ArrayList<byte[]> prodImagesList = new ArrayList<>();
        public LayoutInflater layoutInflater;
        Map<String,String> mapOrderDetails = new HashMap<>();
        static Map<String, Map> mapOrders;
        DocumentSnapshot currComp;
        ViewGroup mparent;
        UUID uuid;
        static String ordId;
        SharedPreferences prefs;
        Context mContext;

        public ProductListForShopAdapter(Context context, ArrayList<DocumentSnapshot> arrdocs, ArrayList<byte[]> arrimages, DocumentSnapshot currDoc){
            prodDocsList = arrdocs;
            prodDocsListCopy = arrdocs;
            prodImagesList = arrimages;
            layoutInflater = LayoutInflater.from(context);
            currComp = currDoc;
            mapOrders = new HashMap<>();

            String ts = String.valueOf(System.currentTimeMillis());
            String rand = uuid.randomUUID().toString();
            ordId = rand+ts;
            Log.d("OrdidGene: ",ordId);

            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("compIdForReplace",currComp.getId());
            editor.commit();
            Log.d("PConstruct> ",currComp.getId());


            MainActivity.mMenu.findItem(R.id.replacements).setVisible(true);
        }

        public ProductListForShopAdapter(Context context,DocumentSnapshot currCompDoc){

            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("compIdForReplace",currCompDoc.getId());
            editor.commit();
            Log.d("WPConstruct> ",currCompDoc.getId());
            currComp = currCompDoc;
        }

        @NonNull
        @Override
        public ProductListForShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View mItemView = layoutInflater.inflate(R.layout.productlist_for_shop, parent, false);
            mparent = parent;
            return new ProductListForShopViewHolder(mItemView, this);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductListForShopViewHolder holder, int position) {
            byte[] currImage = prodImagesList.get(position);
            holder.mImageView.setImageBitmap(BitmapFactory.decodeByteArray(currImage,0, currImage.length));
            holder.tvProdName.setText(prodDocsList.get(position).getString("prodname"));
            holder.tvProdDesc.setText(prodDocsList.get(position).getString("proddesc"));
            holder.tvProdPrice.setText(prodDocsList.get(position).getString("prodprice"));
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
            if(filteredList == null || filteredList.size() == 0){
                filteredList.addAll(prodDocsListCopy);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(DocumentSnapshot doc : prodDocsListCopy){
                    if(doc.get("prodname").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(doc);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            prodDocsList.clear();
            prodDocsList.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };

    public class ProductListForShopViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
            ImageView mImageView;
            TextView tvProdName, tvProdDesc, tvProdPrice;
            Button btnAddCart, btnRemoveCart;
            ProductListForShopAdapter productListForShopAdapter;
            int posItemClicked;
            String qnty;
            AlertDialog alertDialog;
            FloatingActionButton fabPlaceOrder;
            int conf = 0;

            public ProductListForShopViewHolder(@NonNull View itemView, ProductListForShopAdapter productListForShopAdapter) {
                super(itemView);
                //Log.d("VIEWHOLDER",mapOrders.toString());
                mImageView = itemView.findViewById(R.id.iv_productimage_for_shop);
                tvProdName = itemView.findViewById(R.id.tv_productname_for_shop);
                tvProdDesc = itemView.findViewById(R.id.tv_productdesc_for_shop);
                tvProdPrice = itemView.findViewById(R.id.tv_productprice_for_shop);
                btnAddCart = itemView.findViewById(R.id.btn_add_to_cart);
                btnRemoveCart = itemView.findViewById(R.id.btn_remove_from_cart);
                btnAddCart.setOnClickListener(this::onClick);
                btnRemoveCart.setOnClickListener(this::onClick);

                this.productListForShopAdapter = productListForShopAdapter;
            }



            @Override
            public void onClick(View v) {
                Log.d("ONCLICK","CALLED");
                if(v.getId() == R.id.btn_add_to_cart){
                    posItemClicked = getLayoutPosition();
                    askForQuantity();
                }
                else if(v.getId() == R.id.btn_remove_from_cart){
                    posItemClicked = getLayoutPosition();
                    mapOrders.remove(prodDocsList.get(posItemClicked).getId());

                }
                else if(v.getId() == R.id.fab_place_order){
                    if(mapOrders.isEmpty()){
                        Toast.makeText(itemView.getContext(), "Please select products", Toast.LENGTH_SHORT).show();
                    }
                    else
                        placeOrder();
                }
            }

            public void askForQuantity(){
                alertDialog = new AlertDialog.Builder(itemView.getContext()).create();
                alertDialog.setTitle("Enter Quantity");
                EditText et = new EditText(itemView.getContext());
                alertDialog.setView(et);

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        qnty = et.getText().toString();
                        DocumentSnapshot docClicked = prodDocsList.get(posItemClicked);

                        mapOrderDetails.put("orderid", ordId);
                        mapOrderDetails.put("prodname", docClicked.getString("prodname"));
                        mapOrderDetails.put("prodprice", docClicked.getString("prodprice"));
                        mapOrderDetails.put("prodqnty", qnty);
                        mapOrderDetails.put("compName", currComp.getString("name"));
                        mapOrderDetails.put("compid", currComp.getId());
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
                        mapOrderDetails.put("shopname", sharedPreferences.getString("compshopname","Defaultname"));
                        mapOrderDetails.put("shopid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        mapOrderDetails.put("ordstatus","pending");


                        mapOrders.put(docClicked.getString("prodname"), mapOrderDetails);
                        mapOrderDetails = new HashMap<>();


                    }
                });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alertDialog.show();
            }

            public void placeOrder(){

                AlertDialog confAlertDialog = new AlertDialog.Builder(itemView.getContext()).create();
                confAlertDialog.setTitle("Order Summary");
                String summary = "";
                for(Map<String, Map<String,String>> m: mapOrders.values()){
                    summary += "Product name: \t"+m.get("prodname")+"\n";
                    summary += "Product quantity: \t"+m.get("prodqnty")+"\n";
                    summary += "Product price: \t"+m.get("prodprice")+"\n";
                    summary += "\n";
                }

                confAlertDialog.setMessage(summary);
                confAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmPlaceOrder();
                    }
                });
                confAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                confAlertDialog.show();

            }

            public void confirmPlaceOrder(){
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                firebaseFirestore.collection("ordermystock").document("userdoc")
                        .collection("shops").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection("ordersplaced")
                        .document(ordId)
                        .set(mapOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        firebaseFirestore.collection("ordermystock").document("userdoc")
                                .collection("companies").document(currComp.getId())
                                .collection("ordersreceived")
                                .document(ordId).set(mapOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                AlertDialog orderDialogue = new AlertDialog.Builder(itemView.getContext()).create();
                                orderDialogue.setTitle("Order placement");
                                orderDialogue.setMessage("Order placed Successfully");
                                orderDialogue.setButton(AlertDialog.BUTTON_POSITIVE, "ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                orderDialogue.show();
                            }
                        });
                    }
                });
            }
        }
}
