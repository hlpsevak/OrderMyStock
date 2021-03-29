package com.example.ordermystock;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DisplayProductsForShopFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayProductsForShopFrag extends Fragment {

    private RecyclerView mRecyclerView;
    private ProductListForShopAdapter mAdapter;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    View view;
    ProgressBar pb;
    Map<String, DocumentSnapshot> mapdocs;
    Map<String, byte[]> mapimages;
    Map<String,String> prodData;
    int doccnt = 0,imgcnt = 0;
    ArrayList<DocumentSnapshot> arrdocs;
    ArrayList<byte[]> arrimages;
    DocumentSnapshot currDoc;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DisplayProductsForShopFrag() {
        // Required empty public constructor
    }

    public DisplayProductsForShopFrag(DocumentSnapshot doc){
        currDoc = doc;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DisplayProductsForShopFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static DisplayProductsForShopFrag newInstance(String param1, String param2) {
        DisplayProductsForShopFrag fragment = new DisplayProductsForShopFrag();
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
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_display_products_for_shop, container, false);
        mRecyclerView = view.findViewById(R.id.rv_products_for_shop);
        pb = view.findViewById(R.id.pb_productlist_for_shop);
        pb.setVisibility(View.VISIBLE);
        FloatingActionButton fab = view.findViewById(R.id.fab_place_order);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Finally called", Toast.LENGTH_SHORT).show();
                ProductListForShopAdapter pdad = new ProductListForShopAdapter(getContext(),currDoc);
                ProductListForShopAdapter.ProductListForShopViewHolder vh = pdad.new ProductListForShopViewHolder(view, new ProductListForShopAdapter(getContext(),currDoc));
                vh.placeOrder();
            }
        });

        retrieveProducts();

        return view;
    }

    public void retrieveProducts(){
        mapdocs = new HashMap<>();
        mapimages = new HashMap<>();
        arrdocs = new ArrayList<>();
        arrimages = new ArrayList<>();
        doccnt = 0;
        imgcnt = 0;

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ordermystock").document("userdoc")
                .collection("companies").document(currDoc.getId()).collection("products")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if(querySnapshot.isEmpty()){
                    Toast.makeText(getContext(), "No products found", Toast.LENGTH_SHORT).show();
                    pb.setVisibility(View.GONE);
                }
                else{
                    List<DocumentSnapshot> prodDocsList = querySnapshot.getDocuments();

                    for(DocumentSnapshot doc : prodDocsList){
                        mapdocs.put(doc.getId() , doc);
                        doccnt++;

                        firebaseStorage = FirebaseStorage.getInstance();
                        firebaseStorage.getReference().child("prodimages/"+doc.getId())
                                .getBytes(4000*4000)
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                mapimages.put(doc.getId(), bytes);
                                imgcnt++;

                                if(imgcnt == prodDocsList.size()){
                                    for(String key : mapdocs.keySet()){
                                        arrdocs.add(mapdocs.get(key));
                                        arrimages.add(mapimages.get(key));
                                    }
                                    mAdapter = new ProductListForShopAdapter(getContext(), arrdocs, arrimages, currDoc);
                                    mRecyclerView.setAdapter(mAdapter);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    pb.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}