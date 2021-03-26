package com.example.ordermystock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignInFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignInFrag extends Fragment implements View.OnClickListener {

    private String password = null, email  = null, comporshop = null;
    View inflatedView;
    View pbSigninup;
    private FirebaseAuth mAuth;
    private Button btnSignIn;
    private TextView tvNoAccSignUp;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignInFrag() {
        // Required empty public constructor
    }

    public static SignInFrag newInstance() {
        SignInFrag fragment = new SignInFrag();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
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
        inflatedView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        btnSignIn = inflatedView.findViewById(R.id.btn_Signin);
        btnSignIn.setOnClickListener(this);
        tvNoAccSignUp = inflatedView.findViewById(R.id.tv_NoAccSignUp);
        tvNoAccSignUp.setOnClickListener(this);
        pbSigninup = getActivity().findViewById(R.id.pb_Signinup);

        return inflatedView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == tvNoAccSignUp.getId()) {
            openSignUpPage();
        }
        else if (v.getId() == btnSignIn.getId()) {

            EditText et1 = (EditText) inflatedView.findViewById(R.id.et_Emailid);
            email = et1.getText().toString();
            EditText et2 = (EditText) inflatedView.findViewById(R.id.et_password);
            password = et2.getText().toString();

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                //Toast.makeText(getContext(), "Enter valid Email-id and Password", Toast.LENGTH_SHORT).show();
                et1.setError("Email can not be empty");
                et1.requestFocus();
                et2.setError("password can not be empty");
                et2.requestFocus();
            }
            else {
                showProgressBar();
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    openMainActivity();
                                } else {
                                    Toast.makeText(getContext(), task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                                }
                                hideProgressBar();
                            }
                        });
            }
        }
    }

    public void openSignUpPage(){
        SignUpFrag signUpFrag = new SignUpFrag();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.cl_Signinup, SignUpFrag.newInstance()).commit();
        //getActivity().finish();
    }

    public void openMainActivity(){

        String currUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().document("ordermystock/userdoc/shops/"+currUserID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    comporshop = "shops";
                }
                else{
                    comporshop = "companies";
                }

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("comporshop",comporshop);
                editor.putString("compshopname",documentSnapshot.getString("name"));
                editor.commit();

                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra("comporshop", comporshop);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    public void showProgressBar(){
        pbSigninup.setVisibility(View.VISIBLE);
    }
    public void hideProgressBar(){
        pbSigninup.setVisibility(View.INVISIBLE);
    }
}
