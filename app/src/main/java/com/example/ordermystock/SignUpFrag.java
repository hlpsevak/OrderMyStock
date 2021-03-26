package com.example.ordermystock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFrag extends Fragment implements View.OnClickListener{

    private String email = null, password = null, mobno=null, CompShopName=null;
    private View inflatedView,pbSigninup;
    private FirebaseAuth mAuth;
    private Button btnSignUp;
    private TextView tvAlreadyAccSignIn;
    private boolean imgflag = false;
    private EditText etEmail,etMobNo,etPassword,etCompShopName;
    private ImageButton ibCert;
    private Uri imgSelected;
    public String comporshop = "";



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpFrag() {
        // Required empty public constructor
    }

    public static SignUpFrag newInstance() {
        SignUpFrag fragment = new SignUpFrag();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        */
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflatedView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        btnSignUp = inflatedView.findViewById(R.id.btn_SignUp);
        btnSignUp.setOnClickListener(this);
        tvAlreadyAccSignIn = inflatedView.findViewById(R.id.tv_AlreadyAccSignIn);
        tvAlreadyAccSignIn.setOnClickListener(this);
        pbSigninup = getActivity().findViewById(R.id.pb_Signinup);
        ibCert = inflatedView.findViewById(R.id.ib_compshop_cert);
        ibCert.setOnClickListener(this);
        Button btnChangeForm = inflatedView.findViewById(R.id.btn_changeform);
        btnChangeForm.setOnClickListener(this);

        changeToShop();
        return inflatedView;
    }


    @Override
    public void onClick(View v) {

        if(v.getId() == tvAlreadyAccSignIn.getId()){
            openSignInPage();
        }
        else if(v.getId() == R.id.btn_changeform){
            changeForm();
        }
        else if(v.getId() == ibCert.getId()){
            getImage();
        }
        else if(v.getId() == btnSignUp.getId()){

            etEmail = (EditText)inflatedView.findViewById(R.id.et_EmailSignUp);
            etMobNo = (EditText)inflatedView.findViewById(R.id.et_MobNo);
            etPassword = (EditText)inflatedView.findViewById(R.id.et_PasswordSignUp);
            etCompShopName = (EditText)inflatedView.findViewById(R.id.et_compshop_name);

            CompShopName = etCompShopName.getText().toString().trim();
            email = etEmail.getText().toString().trim();
            mobno = etMobNo.getText().toString().trim();
            password = etPassword.getText().toString().trim();

            boolean flagAllCorrect = true, bool;
            TextView tv;

            if(!imgflag){
                flagAllCorrect = false;
                Toast.makeText(getContext(), "image not selected", Toast.LENGTH_SHORT).show();
            }

            if(CompShopName.isEmpty()){
                flagAllCorrect = false;
                etCompShopName.setError("Name can not be empty");
                etCompShopName.requestFocus();
            }

            //Pattern.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",email)
            bool = (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
            if(!bool){
                flagAllCorrect = false;
                etEmail.setError("Invalid Email");
                etEmail.requestFocus();
            }

            bool = (!TextUtils.isEmpty(email) && Pattern.matches("^\\d{10}$",mobno));
            if(!bool){
                flagAllCorrect = false;
                etMobNo.setError("Mobile no. should be 10 digit");
                etMobNo.requestFocus();
            }

            bool = (!TextUtils.isEmpty(email) && Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$+=])(?=\\S+$).{8,}$",password));
            if(!bool){
                flagAllCorrect = false;
                etPassword.setError("password should have \nat least 1 no.\nat least 1 small and capital letter\nat least 1 special character(@#$) \nlength >= 8");
                etPassword.requestFocus();
            }

            if(flagAllCorrect){

                showProgressBar();

                //creating user with email and password
                mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getContext(), "SuccessFully Signed Up, storing data", Toast.LENGTH_SHORT).show();
                                    uploadImageCertificate();
                                }
                                else{
                                    Toast.makeText(getContext(), task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                                    hideProgressBar();
                                }
                            }
                        });
            }
        }
    }

    public void saveProfileData(String imgurl){

        Map<String, Object> obj = new HashMap<>();
        obj.put("name", CompShopName);
        obj.put("emailid", email);
        obj.put("contactemailid", "");
        obj.put("contact", mobno);
        obj.put("certurl", imgurl);
        obj.put("street1","");
        obj.put("street2","");
        obj.put("city","");
        obj.put("pincode","");
        obj.put("state","");

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ordermystock").document("userdoc").
                collection(comporshop).document(mAuth.getInstance().getCurrentUser().getUid()).set(obj).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            Toast.makeText(getContext(), "Data added successfully", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();

                        hideProgressBar();
                        openHomePage();
                    }
                });

    }

    public void uploadImageCertificate(){

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        StorageReference certimagesref = storageReference.child("compshopcerticates/"+mAuth.getInstance().getCurrentUser().getUid()+"image");
        certimagesref.putFile(imgSelected).addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    String url = certimagesref.getDownloadUrl().toString();
                    saveProfileData(url);
                }
                else{
                    Toast.makeText(getContext(), task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                }
            }
        });
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
            imgflag = true;
        }
        else{
            //Toast.makeText(getContext(), "Select appropriate image", Toast.LENGTH_SHORT).show();
        }
    }

    public void openHomePage(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("comporshop",comporshop);
        editor.commit();

        Intent intStartHome = new Intent(getContext(), MainActivity.class);
        intStartHome.putExtra("comporshop", comporshop);
        startActivity(intStartHome);
        getActivity().finish();
    }

    public void openSignInPage(){
        SignInFrag signInFrag = new SignInFrag();
        //FragmentManager manager = FragmentManager.
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.cl_Signinup, signInFrag).commit();
    }

    public void changeForm(){
        Button btnChangeForm = inflatedView.findViewById(R.id.btn_changeform);
        if(btnChangeForm.getText().toString().equals("Sign up as user")){
            changeToShop();
        }
        else{
            changeToComp();
        }
    }

    public void changeToComp(){
        TextView tv = inflatedView.findViewById(R.id.tv_comshop_name);
        tv.setText(R.string.tv_comp_name);
        tv = inflatedView.findViewById(R.id.tv_comshop_cert);
        tv.setText(R.string.tv_comp_cert);
        tv = inflatedView.findViewById(R.id.tv_EmailSignUp);
        tv.setText(R.string.tv_comp_email);
        tv = inflatedView.findViewById(R.id.tv_MobNo);
        tv.setText(R.string.tv_phone);
        tv = inflatedView.findViewById(R.id.tv_Password);
        tv.setText(R.string.tv_password);
        tv = inflatedView.findViewById(R.id.tv_Signup);
        tv.setText("Sign Up as Company");

        Button btnChangeForm = (Button)inflatedView.findViewById(R.id.btn_changeform);
        btnChangeForm.setText("Sign up as user");

        comporshop = "companies";
    }

    public void changeToShop(){
        TextView tv = inflatedView.findViewById(R.id.tv_comshop_name);
        tv.setText(R.string.tv_shop_name);
        tv = inflatedView.findViewById(R.id.tv_comshop_cert);
        tv.setText(R.string.tv_shop_cert);
        tv = inflatedView.findViewById(R.id.tv_EmailSignUp);
        tv.setText(R.string.tv_shop_email);
        tv = inflatedView.findViewById(R.id.tv_MobNo);
        tv.setText(R.string.tv_phone);
        tv = inflatedView.findViewById(R.id.tv_Password);
        tv.setText(R.string.tv_password);
        tv = inflatedView.findViewById(R.id.tv_Signup);
        tv.setText("Sign Up as User");

        Button btnChangeForm = (Button)inflatedView.findViewById(R.id.btn_changeform);
        btnChangeForm.setText("Sign up as company");
        comporshop = "shops";
    }

    public void showProgressBar(){
        pbSigninup.setVisibility(View.VISIBLE);
    }
    public void hideProgressBar(){
        pbSigninup.setVisibility(View.INVISIBLE);
    }
}