package com.example.artem.blogapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.artem.blogapp.R;

public class StartActivity extends AppCompatActivity {

    private Button regBtn, loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        regBtn = (Button) findViewById(R.id.btn_sign_up);
        loginBtn = (Button) findViewById(R.id.btn_sign_in);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(signUpIntent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIpIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(signIpIntent);
            }
        });
    }
}
