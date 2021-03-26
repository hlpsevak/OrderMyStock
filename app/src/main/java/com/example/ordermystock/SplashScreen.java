package com.example.ordermystock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    Handler handler;
    private FirebaseAuth mAuth;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Initializing firebase instance
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Checking if the user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){  //If user is already signed in, redirect to main activity
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String comporshop = sharedPreferences.getString("comporshop","shopno");

            intent = new Intent(this, MainActivity.class);
            intent.putExtra("comporshop",comporshop);
        }
        else{   //else redirect to Signinup activity
            intent = new Intent(this,SignInSignUp.class);
        }
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        },1500);
    }
}