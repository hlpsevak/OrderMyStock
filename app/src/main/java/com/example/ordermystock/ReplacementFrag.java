package com.example.ordermystock;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReplacementFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReplacementFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView mRecyclerView;
    private static ReplacementAdapter mAdapter;
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
    String comporshop;
    UUID uuid;
    static String ordId;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReplacementFrag() {
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
     * @return A new instance of fragment ReplacementFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static ReplacementFrag newInstance(String param1, String param2) {
        ReplacementFrag fragment = new ReplacementFrag();
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
        view = inflater.inflate(R.layout.fragment_replacement, container, false);
        mRecyclerView = view.findViewById(R.id.rv_replaceproducts);
        pb = view.findViewById(R.id.pb_replace);
        pb.setVisibility(View.VISIBLE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        comporshop = prefs.getString("comporshop", "shops");
        fab = view.findViewById(R.id.fab_replace_product);
        if(comporshop.equals("companies"))
            fab.setVisibility(View.GONE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyForReplacement();
            }
        });

        retrieveReplacedProductList();

        return view;
    }

    public void retrieveReplacedProductList(){

        mapdocs = new HashMap<>();
        mapiamges = new HashMap<>();
        arrdocs = new ArrayList<>();
        arrimages = new ArrayList<>();
        doccnt = 0;
        imgcnt = 0;



        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ordermystock").document("userdoc")
                .collection(comporshop).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("replacements").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if(querySnapshot.isEmpty()){
                    Toast.makeText(getContext(), "No Products for replacement", Toast.LENGTH_SHORT).show();
                    pb.setVisibility(View.GONE);
                }
                else{
                    List<DocumentSnapshot> docslist = querySnapshot.getDocuments();

                    for(DocumentSnapshot doc : docslist){
                        mapdocs.put(doc.getId(), doc);
                        doccnt++;
                        Log.d("DOCUMENT ID", doc.getId());
                        storageReference = FirebaseStorage.getInstance().getReference().child("replacementprodimages/"+doc.getId());
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

                                                mAdapter = new ReplacementAdapter(getContext(), arrdocs, arrimages);
                                                mRecyclerView.setAdapter(mAdapter);
                                                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                                                pb.setVisibility(View.GONE);
                                                MainActivity.searchView.setQuery("", false);
                                                MainActivity.searchItem.collapseActionView();
                                            }
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }


    public void applyForReplacement(){
        //Toast.makeText(getContext(), "FAB", Toast.LENGTH_SHORT).show();
        View view = getLayoutInflater().inflate(R.layout.apply_replacement_dialogue, null);
        alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Add Product to replace");


        ImageButton imageButton = view.findViewById(R.id.ib_add_product);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }

        });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String ts = String.valueOf(System.currentTimeMillis());
                String rand = uuid.randomUUID().toString();
                ordId = rand+ts;

                Log.d("OrdidGene: ",ordId);

                prodData = new HashMap<>();
                prodData.put("replacementid",ordId);

                EditText et = view.findViewById(R.id.et_db_prodname);
                String prodName = et.getText().toString();
                prodData.put("prodname",prodName);
                et = view.findViewById(R.id.et_db_proddesc);
                et.setHint("Enter Reason for replacement");
                String prodDesc = et.getText().toString();
                prodData.put("reason", prodDesc);
                et = view.findViewById(R.id.et_db_prodprice);
                String prodPrice = et.getText().toString()+"rs/-";
                prodData.put("prodprice", prodPrice);
                prodData.put("status","pending");
                prodData.put("shopid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prodData.put("shopname",prefs.getString("compshopname", "def"));
                prodData.put("accept","Accept");
                prodData.put("reject","Reject");

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date date = new Date();
                prodData.put("applieddate",formatter.format(date));
                prodData.put("accepteddate","");
                prodData.put("rejecteddate","");
                prodData.put("replaceddate","");

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
                .collection("shops").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("replacements").document(ordId).set(prodData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseStorage = FirebaseStorage.getInstance();
                        firebaseStorage.getReference().child("replacementprodimages/"+ordId)
                                .putFile(imgSelected).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                                String currComp = prefs.getString("compIdForReplace","def");
                                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                firebaseFirestore.collection("ordermystock").document("userdoc")
                                        .collection("companies").document(currComp).collection("replacements")
                                        .document(ordId).set(prodData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                retrieveReplacedProductList();
                                            }
                                        });
                            }
                        });
                    }

                });
    }
}