package com.example.ordermystock;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OrdersPlacedReceivedAdapter extends RecyclerView.Adapter<OrdersPlacedReceivedAdapter.OrdersPlacedViewholder> implements Filterable {

    ArrayList<Object> arrAllOrders;
    ArrayList<Object> arrAllOrdersCopy ;
    LayoutInflater inflater;
    String compshop, comporshop;
    static OrdersPlacedViewholder mholder;
    View mItemView;

    String dateString="";
    Date mDate;

    public OrdersPlacedReceivedAdapter(Context context, ArrayList<Object> arrOrders, String compshop){
        arrAllOrders = new ArrayList<>(arrOrders);
        arrAllOrdersCopy = new ArrayList<>(arrOrders);
        inflater = LayoutInflater.from(context);
        if(compshop.equals("companies")){
            this.compshop = "shopname";
            this.comporshop = "companies";
        }
        else {
            this.compshop = "compName";
            this.comporshop = "shops";
        }
    }

    @NonNull
    @Override
    public OrdersPlacedViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mItemView = inflater.inflate(R.layout.ordersplacedreceived, parent, false);
        return new OrdersPlacedViewholder(mItemView,this);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersPlacedViewholder holder, int position) {
        mholder = holder;
        Map<String, Object> m = (Map<String, Object>) arrAllOrders.get(position);

        holder.tvOrdId.setText("Order id:  "+m.get("orderid"));
        holder.tvOrdCompShop.setText((comporshop.equals("companies")?"shop name":"company name")+":  "+ m.get(compshop));
        holder.tvOrdProdName.setText("Product name:  " + m.get("prodname").toString());
        holder.tvOrdProdQnty.setText("Quantity:  " + m.get("prodqnty").toString());
        holder.tvOrdPrice.setText("Price:  " + m.get("prodprice").toString());
        holder.tvOrderDates.setText("Order Placed: "+m.get("orderplaceddate"));
        if(!m.get("orderdelivereddate").equals(""))
            holder.tvOrderDates.append("\nOrder delivered: "+m.get("orderdelivereddate"));
        if(!m.get("ordercancelleddate").equals(""))
            holder.tvOrderDates.append("\nOrder Cancelled: "+m.get("ordercancelleddate"));
        String status = m.get("ordstatus").toString();
        holder.tvOrdStatusResult.setText(status);

        Log.d("STATUS: ",status);
        if(status.equals("Delivered") || status.equals("Cancelled")){
            holder.btnCancelDeliverOrder.setVisibility(View.GONE);
            Log.d("INSIDE if: ","IN");
        }
        else{
            holder.btnCancelDeliverOrder.setVisibility(View.VISIBLE);
        }
        //Log.d("UPD ORD STATUS",m.get("ordstatus").toString());
    }

    @Override
    public int getItemCount() {
        return arrAllOrders.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Object> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(arrAllOrdersCopy);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(Object obj : arrAllOrdersCopy){
                    Map<String, Object> m = (Map<String, Object>) obj;
                    if(m.get("prodname").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(obj);
                    }
                    else if(m.get("ordstatus").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(obj);
                    }
                    else if(m.get("orderplaceddate").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(obj);
                    }
                    else if(m.get("ordercancelleddate").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(obj);
                    }
                    else if(m.get("orderdelivereddate").toString().toLowerCase().contains(filterPattern)){
                        filteredList.add(obj);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            arrAllOrders.clear();
            arrAllOrders.addAll((ArrayList)results.values);

            notifyDataSetChanged();
        }
    };

    public class OrdersPlacedViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvOrdId, tvOrdCompShop, tvOrdProdName, tvOrdProdQnty, tvOrdPrice, tvOrdStatus,
                tvOrdStatusResult, tvOrderDates;
        OrdersPlacedReceivedAdapter ordersPlacedReceivedAdapter;
        Button btnCancelDeliverOrder;
        int mProdPosition;


        public OrdersPlacedViewholder(@NonNull View itemView, OrdersPlacedReceivedAdapter ordersPlacedReceivedAdapter) {
            super(itemView);

            tvOrdId  = itemView.findViewById(R.id.tv_orderid_placedreceived);
            tvOrdCompShop = itemView.findViewById(R.id.tv_compshop_placedreceived);
            tvOrdProdName = itemView.findViewById(R.id.tv_prodname_placedreceived);
            tvOrdProdQnty = itemView.findViewById(R.id.tv_prodqnty_placedreceived);
            tvOrdPrice = itemView.findViewById(R.id.tv_price_placedreceived);
            tvOrdStatusResult = itemView.findViewById(R.id.tv_ord_status_result);
            tvOrderDates = itemView.findViewById(R.id.tv_order_dates);
            btnCancelDeliverOrder = itemView.findViewById(R.id.btn_cancel_deliver_order);
            mItemView = itemView;

            if(compshop.equals("compName")){
                btnCancelDeliverOrder.setText("Cancel Order");
            }
            else{
                btnCancelDeliverOrder.setText("Mark as Delivered");
            }
            btnCancelDeliverOrder.setOnClickListener(this::onClick);

            this.ordersPlacedReceivedAdapter = ordersPlacedReceivedAdapter;

        }


        @Override
        public void onClick(View v) {
            mProdPosition = getLayoutPosition();
            Map<String, String> m = (Map<String, String>) arrAllOrders.get(mProdPosition);
            String prodName = m.get("prodname");
            String ordid = m.get("orderid");
            String shopid = m.get("shopid");
            String compid = m.get("compid");

            Button btn = (Button)v;
            if(btn.getText().equals("Cancel Order")){
                TextView tv = itemView.findViewById(R.id.tv_ord_status_result);

                if(tv.getText().equals("Delivered") || tv.getText().equals("Cancelled")){
                    Toast.makeText(itemView.getContext(), "Order has already been delivered/cancelled", Toast.LENGTH_SHORT).show();
                }else{
                    AlertDialog alertDialog = new AlertDialog.Builder(itemView.getContext()).create();
                    alertDialog.setTitle("confirm order cancellation");
                    alertDialog.setMessage("Do you really want to cancel order ?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "yes", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelOrder(prodName, ordid, shopid, compid);

                            TextView tv = itemView.findViewById(R.id.tv_ord_status_result);
                            itemView.findViewById(mProdPosition);
                            tv.setText("cancelled");
                            Button btnCancelDeliver = itemView.findViewById(R.id.btn_cancel_deliver_order);
                            btnCancelDeliver.setVisibility(View.GONE);
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            Date date = new Date();
                            dateString = formatter.format(date);
                            tv = itemView.findViewById(R.id.tv_order_dates);
                            itemView.findViewById(mProdPosition);
                            tv.append("\nOrder cancelled: "+dateString);
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                }
            }

            else{
                TextView tv = itemView.findViewById(R.id.tv_ord_status_result);

                if(tv.getText().equals("Cancelled") || tv.getText().equals("Delivered")){
                    Toast.makeText(itemView.getContext(), "Order has already cancelled/delivered", Toast.LENGTH_SHORT).show();
                }else{
                    AlertDialog alertDialog = new AlertDialog.Builder(itemView.getContext()).create();
                    alertDialog.setTitle("confirm Delivery completion");
                    alertDialog.setMessage("delivery successful ?\n" +
                            "mark as delivered ?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "yes", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            markDeliverd(prodName, ordid, shopid, compid);

                            TextView tv = itemView.findViewById(R.id.tv_ord_status_result);
                            Button btnCancelDeliver = itemView.findViewById(R.id.btn_cancel_deliver_order);
                            btnCancelDeliver.setVisibility(View.GONE);
                            itemView.findViewById(mProdPosition);
                            tv.setText("Delivered");
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            Date date = new Date();
                            dateString = formatter.format(date);
                            tv = itemView.findViewById(R.id.tv_order_dates);
                            itemView.findViewById(mProdPosition);
                            tv.append("\nOrder delivered: "+dateString);

                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();
                }
            }
        }
    }

    public void cancelOrder(String prodName, String ordid, String shopid, String compid){

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ordermystock").document("userdoc")
                .collection(comporshop).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("ordersplaced").document(ordid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> maps = documentSnapshot.getData();
                Map<String, String> map = (Map<String, String>) maps.get(prodName);
                Map<String, Map<String, String>> mapmap = new HashMap<>();
                map.put("ordstatus","Cancelled");
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date date = new Date();
                dateString = formatter.format(date);
                //mholder.tvOrderDates.append("\norder Cancelled: "+dateString);

                map.put("ordercancelleddate",dateString);
                maps.put(prodName,map);

                FirebaseFirestore firebaseFirestore1 = FirebaseFirestore.getInstance();
                firebaseFirestore1.collection("ordermystock").document("userdoc")
                        .collection("shops").document(shopid)
                        .collection("ordersplaced").document(ordid)
                        .set(maps).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //mholder.tvOrdStatusResult.setText("Cancelled");



                        FirebaseFirestore firebaseFirestore2 = FirebaseFirestore.getInstance();
                        firebaseFirestore2.collection("ordermystock").document("userdoc")
                                .collection("companies").document(compid)
                                .collection("ordersreceived").document(ordid)
                                .set(maps).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                AlertDialog alertDialog = new AlertDialog.Builder(mItemView.getContext()).create();
                                alertDialog.setTitle("Order Cancellation");
                                alertDialog.setMessage("Order cancelled successfully");
                                alertDialog.show();
                                //Log.d("ORDERED: ","CANCELLED");
                            }
                        });
                    }
                });
            }
        });
    }

    public void markDeliverd(String prodName, String ordid, String shopid, String compid){

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ordermystock").document("userdoc")
                .collection(comporshop).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("ordersreceived").document(ordid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> maps = documentSnapshot.getData();
                Map<String, String> map = (Map<String, String>) maps.get(prodName);
                Map<String, Map<String, String>> mapmap = new HashMap<>();
                map.put("ordstatus","Delivered");
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date date = new Date();
                dateString = formatter.format(date);
                map.put("orderdelivereddate",dateString);
                //mholder.tvOrderDates.append("\nOrder delivered: "+dateString);
                maps.put(prodName,map);

                FirebaseFirestore firebaseFirestore1 = FirebaseFirestore.getInstance();
                firebaseFirestore1.collection("ordermystock").document("userdoc")
                        .collection("shops").document(shopid)
                        .collection("ordersplaced").document(ordid)
                        .set(maps).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //mholder.tvOrdStatusResult.setText("Delivered");

                        FirebaseFirestore firebaseFirestore2 = FirebaseFirestore.getInstance();
                        firebaseFirestore2.collection("ordermystock").document("userdoc")
                                .collection("companies").document(compid)
                                .collection("ordersreceived").document(ordid)
                                .set(maps).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                AlertDialog alertDialog = new AlertDialog.Builder(mItemView.getContext()).create();
                                alertDialog.setTitle("Order Delivery");
                                alertDialog.setMessage("Order marked as Delivered");
                                alertDialog.show();
                                Log.d("ORDERED: ","DELIVERED");

                            }
                        });
                    }
                });
            }
        });
    }
}
