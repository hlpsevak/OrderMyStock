package com.example.ordermystock;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFrag extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private View inflatedView;
    public String comporshop = "shops";
    private TextView tvProfCompShopName,tvProfContactEmail,tvProfstreet1,tvProfstreet2,tvProfCity,tvProfPincode,tvProfstate,tvProfAdd;
    private EditText etProfCompShopName,etProfContactEmail,etProfstreet1,etProfstreet2,etProfCity,etProfPincode,etProfstate;
    private ImageView imgProfCert;
    Button btnupdateProfile;
    private FirebaseUser firebaseUser;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFrag() {
        // Required empty public constructor
    }
    public ProfileFrag(String comporshop){
        this.comporshop = comporshop;
        //Toast.makeText(getContext(), ""+this.comporshop, Toast.LENGTH_SHORT).show();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFrag newInstance(String param1, String param2) {
        ProfileFrag fragment = new ProfileFrag();
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

        inflatedView = inflater.inflate(R.layout.fragment_profile, container, false);
        btnupdateProfile = inflatedView.findViewById(R.id.btn_prof_update);
        btnupdateProfile.setOnClickListener(this);

        initializeViews();
        comporshop = getArguments().getString("comporshop");
        //Toast.makeText(getContext(), "OnProfile"+comporshop, Toast.LENGTH_SHORT).show();
        if(comporshop.equals("companies"))
            changeProfileToCompany();
        else
            changeProfileToShop();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        insertProfileDetails();

        //MainActivity.mMenu.findItem(R.id.replacements).setVisible(false);
        return inflatedView;
    }

    public void changeProfileToCompany(){
        tvProfCompShopName.setText(R.string.tv_profile_comp_name);
        tvProfContactEmail.setText(R.string.tv_profile_contactemail);
        tvProfstreet1.setText(R.string.tv_profile_streetline1);
        tvProfstreet2.setText(R.string.tv_profile_streetline2);
        tvProfCity.setText(R.string.tv_profile_city);
        tvProfPincode.setText(R.string.tv_profile_pincode);
        tvProfstate.setText(R.string.tv_profile_state);
    }

    public void changeProfileToShop(){
        tvProfCompShopName.setText(R.string.tv_profile_shop_name);
        tvProfContactEmail.setText(R.string.tv_profile_contactemail);
        tvProfstreet1.setText(R.string.tv_profile_streetline1);
        tvProfstreet2.setText(R.string.tv_profile_streetline2);
        tvProfCity.setText(R.string.tv_profile_city);
        tvProfPincode.setText(R.string.tv_profile_pincode);
        tvProfstate.setText(R.string.tv_profile_state);
        tvProfAdd.setText(R.string.tv_profile_add);
    }

    public void initializeViews(){
        tvProfCompShopName = inflatedView.findViewById(R.id.tv_prof_compshopname);
        tvProfContactEmail = inflatedView.findViewById(R.id.tv_prof_contactemail);
        tvProfstreet1 = inflatedView.findViewById(R.id.tv_prof_street1);
        tvProfstreet2 = inflatedView.findViewById(R.id.tv_prof_street2);
        tvProfCity = inflatedView.findViewById(R.id.tv_prof_city);
        tvProfPincode = inflatedView.findViewById(R.id.tv_prof_pincode);
        tvProfstate = inflatedView.findViewById(R.id.tv_prof_state);
        tvProfAdd = inflatedView.findViewById(R.id.tv_prof_add);

        etProfCompShopName = inflatedView.findViewById(R.id.et_prof_compshopname);
        etProfContactEmail = inflatedView.findViewById(R.id.et_prof_contactemail);
        etProfstreet1 = inflatedView.findViewById(R.id.et_prof_street1);
        etProfstreet2 = inflatedView.findViewById(R.id.et_prof_street2);
        etProfCity = inflatedView.findViewById(R.id.et_prof_city);
        etProfPincode = inflatedView.findViewById(R.id.et_prof_pincode);
        etProfstate = inflatedView.findViewById(R.id.et_prof_state);

        imgProfCert = inflatedView.findViewById(R.id.iv_prof_img);
    }

    public void insertProfileDetails(){

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.document("ordermystock/userdoc/"+comporshop+"/"+firebaseUser.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    etProfCompShopName.setText(documentSnapshot.getString("name"));
                    etProfContactEmail.setText(documentSnapshot.getString("contactemailid"));
                    etProfstreet1.setText(documentSnapshot.getString("street1"));
                    etProfstreet2.setText(documentSnapshot.getString("street2"));
                    etProfCity.setText(documentSnapshot.getString("city"));
                    etProfstate.setText(documentSnapshot.getString("state"));
                    etProfPincode.setText(documentSnapshot.getString("pincode"));
                    String certurl = documentSnapshot.getString("certurl");

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("compshopcerticates/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"image");
                            storageReference.getBytes(1024*1024)
                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    imgProfCert.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "profimgfail: "+e, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else{
                   //Toast.makeText(getContext(), "Doc does not exists", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Fail: "+e, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void updateProfile(){
        Map<String, Object> updMap = new HashMap<>();
        updMap.put("name",etProfCompShopName.getText().toString());
        updMap.put("contactemailid",etProfContactEmail.getText().toString());
        updMap.put("street1",etProfstreet1.getText().toString());
        updMap.put("street2",etProfstreet2.getText().toString());
        updMap.put("city",etProfCity.getText().toString());
        updMap.put("pincode",etProfPincode.getText().toString());
        updMap.put("state",etProfstate.getText().toString());

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.document("ordermystock/userdoc/"+comporshop+"/"+firebaseUser.getUid())
                .set(updMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                    Toast.makeText(getContext(), "Profile Updated Successfully !", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), "ProfUpdFail: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == btnupdateProfile.getId()){
            updateProfile();
        }
    }
}