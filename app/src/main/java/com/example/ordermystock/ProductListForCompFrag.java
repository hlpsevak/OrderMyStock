package com.example.ordermystock;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProductListForCompFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductListForCompFrag extends Fragment {

    private RecyclerView mRecyclerView;
    private static ProductListAdapter mAdapter;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    View view;
    ProgressBar pb;
    Map<String,DocumentSnapshot> mapdocs;
    Map<String, byte[]> mapiamges;
    Map<String,String> prodData;
    int doccnt = 0,imgcnt = 0;
    ArrayList<DocumentSnapshot> arrdocs;
    ArrayList<byte[]> arrimages;
    FloatingActionButton fab;
    public Uri imgSelected;
    String currProdDocID = "";
    AlertDialog alertDialog;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProductListForCompFrag() {
        // Required empty public constructor
        MainActivity.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("NEW TEXT: ",newText);
                Log.d("mAdapter",mAdapter==null?"Nullhai":"NotNullnahihai");
                if(mAdapter!=null)
                    ProductListForCompFrag.mAdapter.getFilter().filter(newText);
                else
                    Log.d("ELSE","null hai bhai");
                return false;
            }
        });

        MainActivity.searchView.setQuery("", false);
        MainActivity.searchView.clearFocus();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProductListForCompFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductListForCompFrag newInstance(String param1, String param2) {
        ProductListForCompFrag fragment = new ProductListForCompFrag();
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

        view = inflater.inflate(R.layout.fragment_product_list_for_comp, container, false);
        mRecyclerView = view.findViewById(R.id.rv_products);
        pb = view.findViewById(R.id.pb_productlist);
        pb.setVisibility(View.VISIBLE);

        fab = view.findViewById(R.id.fab_add_product);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProduct();
            }
        });
        retrieveProductList();

        MainActivity.mMenu.findItem(R.id.replacements).setVisible(true);
        return view;
    }

    public void retrieveProductList(){

        mapdocs = new HashMap<>();
        mapiamges = new HashMap<>();
        arrdocs = new ArrayList<>();
        arrimages = new ArrayList<>();
        doccnt = 0;
        imgcnt = 0;
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ordermystock").document("userdoc")
                .collection("companies").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("products").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if(querySnapshot.isEmpty()){
                    Toast.makeText(getContext(), "No Product exists", Toast.LENGTH_SHORT).show();
                    pb.setVisibility(View.GONE);
                }
                else{
                    List<DocumentSnapshot> docslist = querySnapshot.getDocuments();

                    for(DocumentSnapshot doc : docslist){
                        mapdocs.put(doc.getId(), doc);
                        doccnt++;
                        Log.d("DOCUMENT ID", doc.getId());
                        storageReference = FirebaseStorage.getInstance().getReference().child("prodimages/"+doc.getId());
                                storageReference.getBytes(4000*4000)
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        mapiamges.put(doc.getId(), bytes);
                                        imgcnt++;
                                        if(imgcnt == docslist.size()){
                                            for(String key : mapdocs.keySet()){
                                                arrdocs.add(mapdocs.get(key));
                                                arrimages.add(mapiamges.get(key));


                                            }
                                            ProductListForCompFrag.mAdapter = new ProductListAdapter(getContext(), arrdocs, arrimages);

                                            mRecyclerView.setAdapter(mAdapter);
                                            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                                            pb.setVisibility(View.GONE);
                                            MainActivity.searchView.setQuery("", false);
                                            MainActivity.searchItem.collapseActionView();
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    public void addProduct(){
        //Toast.makeText(getContext(), "FAB", Toast.LENGTH_SHORT).show();
        View view = getLayoutInflater().inflate(R.layout.add_product_dialogue, null);
        alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Add Product");

        ImageButton imageButton = view.findViewById(R.id.ib_add_product);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }

        });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                prodData = new HashMap<>();
                EditText et = view.findViewById(R.id.et_db_prodname);
                String prodName = et.getText().toString();
                prodData.put("prodname",prodName);
                et = view.findViewById(R.id.et_db_proddesc);
                String prodDesc = et.getText().toString();
                prodData.put("proddesc", prodDesc);
                et = view.findViewById(R.id.et_db_prodprice);
                String prodPrice = et.getText().toString()+"rs/-";
                prodData.put("prodprice", prodPrice);

                uploadData();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.setView(view);
        alertDialog.show();
    }

    public void getImage(){

        Intent imgIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(imgIntent , 1);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent retImgIntent) {
        super.onActivityResult(requestCode, resultCode, retImgIntent);

        if(requestCode == 1 && resultCode == getActivity().RESULT_OK){
            Toast.makeText(getContext(), "Image Selected", Toast.LENGTH_SHORT).show();
            imgSelected = retImgIntent.getData();

        }
        else{
            Toast.makeText(getContext(), "Select appropriate image", Toast.LENGTH_SHORT).show();
        }
    }

    public  void  uploadData(){

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ordermystock").document("userdoc")
                .collection("companies").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("products").add(prodData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(), "Product added"+documentReference.getId(), Toast.LENGTH_SHORT).show();
                        currProdDocID = documentReference.getId();

                        if (currProdDocID != "") {
                            firebaseStorage = FirebaseStorage.getInstance();
                            firebaseStorage.getReference().child("prodimages/"+currProdDocID)
                                    .putFile(imgSelected).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    retrieveProductList();
                                }
                            });
                        }
                    }
                });
    }
}