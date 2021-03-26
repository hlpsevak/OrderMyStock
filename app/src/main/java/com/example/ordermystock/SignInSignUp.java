package com.example.ordermystock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class SignInSignUp extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_sign_up);

        getSupportFragmentManager().beginTransaction().replace(R.id.cl_Signinup, SignInFrag.newInstance()).commit();
        //moveTaskToBack(null);
    }
}