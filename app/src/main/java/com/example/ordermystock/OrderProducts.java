package com.example.ordermystock;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderProducts#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderProducts extends Fragment {

    private RecyclerView mRecyclerView;
    public static CompanyListAdapter mAdapter;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    ArrayList<DocumentSnapshot> compDocs;
    ArrayList<byte[]> compimages;
    View view;
    ProgressBar pb;
    int text = 0, images = 0;
    Map<String,byte[]> mapImages;
    Map<String,DocumentSnapshot> mapDocs;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OrderProducts() {
        // Required empty public constructor

        MainActivity.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });


    }



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderProducts.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderProducts newInstance(String param1, String param2) {
        OrderProducts fragment = new OrderProducts();
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
        view = inflater.inflate(R.layout.fragment_order_products, container, false);
        mRecyclerView = view.findViewById(R.id.rv_companies);
        pb = view.findViewById(R.id.pb_orderproducts);
        pb.setVisibility(View.VISIBLE);
        Log.d("ONCREATEVIEW","R");
        retrieveCompaniesList();

        MainActivity.mMenu.findItem(R.id.replacements).setVisible(false);

        return view;
    }

    public void retrieveCompaniesList(){
        Log.d("ONCREATEVIEW","R2");
        mapDocs = new HashMap<>();
        mapImages = new HashMap<>();
        text = 0;
        images = 0;

        compDocs = new ArrayList<>();
        compimages = new ArrayList<>();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> t = firebaseFirestore.collection("ordermystock").document("userdoc")
                .collection("companies").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if(querySnapshot.isEmpty()){
                    Toast.makeText(getContext(), "No Companies exists", Toast.LENGTH_SHORT).show();
                }
                else {
                    Task<byte[]> timages = null;
                    StorageReference storageReference;
                    List<DocumentSnapshot> list = querySnapshot.getDocuments();
                    Log.d("ONCREATEVIEW","R3");
                    for(DocumentSnapshot doc : list){

                        mapDocs.put(doc.getId(),doc);
                        text++;
                        //Toast.makeText(getContext(), ""+doc.getId(), Toast.LENGTH_SHORT).show();
                        Log.d("ONCREATEVIEW","R4");
                        storageReference = FirebaseStorage.getInstance().getReference().child("compshopcerticates/"+doc.getId()+"image");
                        timages = storageReference.getBytes(4000*4000)
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {

                                        mapImages.put(doc.getId(),bytes);
                                        images++;
                                        Log.d("ONCREATEVIEW","RRR: "+text+":"+images);
                                        if(images == list.size()){
                                            for(String key : mapDocs.keySet()){
                                                compDocs.add(mapDocs.get(key));
                                                compimages.add(mapImages.get(key));
                                            }

                                            mAdapter = new CompanyListAdapter(getContext(), compDocs, compimages);
                                            mRecyclerView.setAdapter(mAdapter);
                                            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                            Log.d("ONCREATEVIEW","R5");
                                            pb.setVisibility(View.GONE);

                                            MainActivity.searchView.setQuery("", false);
                                            MainActivity.searchItem.collapseActionView();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("ONFAILURE", e.getMessage());
                            }
                        });
                    }
                }
            }
        });

    }
}