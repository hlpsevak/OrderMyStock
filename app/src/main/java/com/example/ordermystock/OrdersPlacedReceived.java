package com.example.ordermystock;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrdersPlacedReceived#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrdersPlacedReceived extends Fragment {

    View view;
    RecyclerView mRecyclerView;
    ProgressBar pb;
    String comporshop, comporshopord;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    ArrayList<Object> arrAllOrders = new ArrayList<>();
    int docCnt = 0;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OrdersPlacedReceived() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrdersPlacedReceived.
     */
    // TODO: Rename and change types and number of parameters
    public static OrdersPlacedReceived newInstance(String param1, String param2) {
        OrdersPlacedReceived fragment = new OrdersPlacedReceived();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_orders_placed_received, container, false);
        mRecyclerView = view.findViewById(R.id.rv_orderplacedreceived);
        pb = view.findViewById(R.id.pb_orderplacedreceived);
        pb.setVisibility(View.VISIBLE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        comporshop = prefs.getString("comporshop","comp");
        if(comporshop.equals("companies"))
            comporshopord = "ordersreceived";
        else
            comporshopord = "ordersplaced";

        displayOrderedProducts();

        MainActivity.mMenu.findItem(R.id.replacements).setVisible(false);

        return view;
    }

    public void displayOrderedProducts(){

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ordermystock").document("userdoc")
                .collection(comporshop).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(comporshopord).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {

                if(querySnapshot.isEmpty()){
                    Toast.makeText(getContext(), "No orders !", Toast.LENGTH_SHORT).show();
                }
                else{
                    List<DocumentSnapshot> ordersDocList = querySnapshot.getDocuments();
                    Map<String, Object> mapEachOrd = new HashMap<>();
                    for (DocumentSnapshot doc : ordersDocList){
                        Log.d("Doc: ",doc.toString());
                        mapEachOrd = doc.getData();
                        Log.d("MAPEACHORD: ",mapEachOrd.toString());
                        for(Object obj: mapEachOrd.values()) {
                            //Map<String, Object> m = (Map<String, Object>) obj;
                            //m.put("ordid")
                            Log.d("OBJ",obj.toString());
                            arrAllOrders.add(obj);
                        }

                        docCnt++;
                    }
                    if(docCnt == querySnapshot.size()){
                        Log.d("ALL DOCS"," : Appended");
                        Log.d("ALL: ",arrAllOrders.toString());

                        OrdersPlacedReceivedAdapter ordersPlacedReceivedAdapter = new OrdersPlacedReceivedAdapter(getContext(),arrAllOrders, comporshop);
                        mRecyclerView.setAdapter(ordersPlacedReceivedAdapter);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


                    }

                }
                pb.setVisibility(View.GONE);
            }
        });
    }
}