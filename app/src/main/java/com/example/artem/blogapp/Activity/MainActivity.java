package com.example.artem.blogapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.example.artem.blogapp.Fragment.ChatFragment;
import com.example.artem.blogapp.Fragment.FriendsFragment;
import com.example.artem.blogapp.Fragment.PostFragment;
import com.example.artem.blogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    
    private FirebaseAuth mainAuth;
    private DatabaseReference userRef;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationView = (NavigationView) findViewById(R.id.navLayout);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.containerView,new ChatFragment()).commit();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();
                if (menuItem.getItemId() == R.id.friends) {
                    FragmentTransaction friendsTransaction = fragmentManager.beginTransaction();
                    friendsTransaction.replace(R.id.containerView,new FriendsFragment()).addToBackStack(null).commit();
                }
                if (menuItem.getItemId() == R.id.messages) {
                    FragmentTransaction chatTransaction = fragmentManager.beginTransaction();
                    chatTransaction.replace(R.id.containerView,new ChatFragment()).addToBackStack(null).commit();
                }
                if (menuItem.getItemId() == R.id.posts) {
                    FragmentTransaction postTransaction = fragmentManager.beginTransaction();
                    postTransaction.replace(R.id.containerView,new PostFragment()).addToBackStack(null).commit();
                }
                if (menuItem.getItemId() == R.id.users) {
                    Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
                    startActivity(usersIntent);
                    finish();
                }
                if (menuItem.getItemId() == R.id.settings) {
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    finish();
                }
                if (menuItem.getItemId() == R.id.logout) {
                    FirebaseAuth.getInstance().signOut();
                    sendBack();
                }

                drawerLayout.closeDrawers();
                return false;
            }

        });

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this,drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        mainAuth = FirebaseAuth.getInstance();
        if (mainAuth.getCurrentUser() != null) {
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mainAuth.getCurrentUser().getUid());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                default:
            }
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
                default:

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mainAuth.getCurrentUser();
        if (currentUser == null){
            sendBack();

        }else {
            userRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mainAuth.getCurrentUser();
        if (currentUser != null){
            userRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    private void sendBack() {
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

}
