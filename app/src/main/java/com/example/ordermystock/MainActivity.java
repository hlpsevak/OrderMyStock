package com.example.ordermystock;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    NavigationView navigationView;
    Toolbar toolbar;
    public String comporshop = "";
    static SearchView searchView;
    static MenuInflater menuInflator;
    static Menu mMenu;
    static MenuItem searchItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.tb_mainactivity);
        setSupportActionBar(toolbar);
        //toolbar.setBackgroundColor(getResources().getColor(R.color.headerTextColor));

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        Intent intent = getIntent();
        comporshop = intent.getStringExtra("comporshop");

        Bundle bundle = new Bundle();
        bundle.putString("comporshop",comporshop);
        ProfileFrag pf = new ProfileFrag();
        pf.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout_main, pf).commit();
        getSupportActionBar().setTitle("Profile");
        navigationView.setCheckedItem(R.id.nav_profile);
        //Toast.makeText(this, "cs: "+comporshop, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNavigationHeader();
    }

    public void setNavigationHeader(){
        NavigationView nv = (NavigationView)findViewById(R.id.nav_view);
        View navHeader = nv.getHeaderView(0);
        TextView tv = (TextView) navHeader.findViewById(R.id.tv_navheaderemail);
        tv.setText(mAuth.getInstance().getCurrentUser().getEmail().toString());

        ImageView imageView = (ImageView) navHeader.findViewById(R.id.iv_navheaderimage);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        StorageReference imgref = storageReference.child("compshopcerticates/"+mAuth.getInstance().getCurrentUser().getUid()+"image");
        imgref.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes,0, bytes.length));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage().toString()+"   myyee", Toast.LENGTH_SHORT).show();
            }
        });
        //1imageView.setImageURI(Uri.parse("com.google.android.gms.tasks.zzu@148fc98"));


    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);

        menuInflator = getMenuInflater();
        menuInflator.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.replacements).setVisible(false);
        mMenu = menu;
        searchItem = menu.findItem(R.id.search);
        searchView = (SearchView)searchItem.getActionView();
        searchView.setQueryHint("search");


        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(MainActivity.this, newText, Toast.LENGTH_SHORT).show();
                new OrderProducts().mAdapter.getFilter().filter(newText);
                return false;
            }
        });*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        int id = item.getItemId();
        if(id == R.id.replacements) {
            getSupportActionBar().setTitle("Replacements");
            ft.replace(R.id.framelayout_main, new ReplacementFrag()).commit();
            ft.addToBackStack(null);
        }
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch(item.getItemId()){

            case R.id.nav_CompaniesList:
                getSupportActionBar().setTitle("Products");
                if(comporshop.equals("shops"))
                    ft.replace(R.id.framelayout_main, new OrderProducts()).commit();
                else
                    ft.replace(R.id.framelayout_main, new ProductListForCompFrag()).commit();
                ft.addToBackStack(null);
                navigationView.setCheckedItem(R.id.nav_CompaniesList);
                break;
            case R.id.nav_profile:
                getSupportActionBar().setTitle("Profile");
                Bundle bundle = new Bundle();
                bundle.putString("comporshop",comporshop);
                ProfileFrag pf = new ProfileFrag();
                pf.setArguments(bundle);
                ft.replace(R.id.framelayout_main, pf).commit();
                ft.addToBackStack(null);
                navigationView.setCheckedItem(R.id.nav_profile);
                break;
            case R.id.nav_orders:
                getSupportActionBar().setTitle("Orders");
                ft.replace(R.id.framelayout_main, new OrdersPlacedReceived()).commit();
                ft.addToBackStack(null);
                navigationView.setCheckedItem(R.id.nav_orders);
                break;
            case R.id.nav_aboutapp:
                getSupportActionBar().setTitle("About App");
                ft.replace(R.id.framelayout_main, new AboutappFrag()).commit();
                ft.addToBackStack(null);
                navigationView.setCheckedItem(R.id.nav_aboutapp);
                break;
            case R.id.nav_share:
                shareApp();
                break;
            case R.id.nav_logout:
                Toast.makeText(this, "Successfully loged out", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, SignInSignUp.class));
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void shareApp(){
        Intent shareIntent =  new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Order my stock Play store link");
        String shareMessage="https://play.google.com/store/apps/details?id=com.ordermystock.android";
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,shareMessage);
        startActivity(Intent.createChooser(shareIntent,"Sharing via"));
    }
}