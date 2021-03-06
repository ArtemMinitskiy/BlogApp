package com.example.artem.blogapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.artem.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout regName, regEmail, regPassword, confirmPassword;
    private Button btnCreate;
    
    private FirebaseAuth registerAuth;
    private DatabaseReference databaseReference;
    
    private ProgressDialog registerProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerAuth = FirebaseAuth.getInstance();
        
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        regName = (TextInputLayout) findViewById(R.id.reg_name);
        regEmail = (TextInputLayout) findViewById(R.id.reg_email);
        regPassword = (TextInputLayout) findViewById(R.id.reg_password);
        confirmPassword = (TextInputLayout) findViewById(R.id.confirm_paswword);
        btnCreate = (Button) findViewById(R.id.btn_create_account);

        registerProgress = new ProgressDialog(this);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dsplName = regName.getEditText().getText().toString();
                String dsplEmail = regEmail.getEditText().getText().toString();
                String dsplPassword = regPassword.getEditText().getText().toString();
                String dsplConfPassword = confirmPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(dsplName) || !TextUtils.isEmpty(dsplEmail) || !TextUtils.isEmpty(dsplPassword) || !TextUtils.isEmpty(dsplConfPassword)){
                    if (dsplPassword.equals(dsplConfPassword)) {

                        registerProgress.setTitle("Rigister User");
                        registerProgress.setMessage("Please wait...");
                        registerProgress.setCanceledOnTouchOutside(false);
                        registerProgress.show();
                        registerUser(dsplName, dsplEmail, dsplPassword);
                    } else {
                        Toast.makeText(RegisterActivity.this, " Confirm password right!", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }

    private void registerUser(final String dsplName, String dsplEmail, String dsplPassword) {

        registerAuth.createUserWithEmailAndPassword(dsplEmail, dsplPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", dsplName);
                            userMap.put("status", "You status");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            userMap.put("device_token", deviceToken);

                            databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                         registerProgress.dismiss();
                                         Toast.makeText(RegisterActivity.this, "Authentication success.", Toast.LENGTH_SHORT).show();
                                         Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                         mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                         startActivity(mainIntent);
                                         finish();
                                    }
                                }
                            });

                        } else {
                            registerProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
