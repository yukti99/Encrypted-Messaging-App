package com.example.mychatapp;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.mychatapp.Prevalent.Prevalent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.paperdb.Paper;


public class StartActivity extends AppCompatActivity {

    Button login,register;
    FirebaseUser  firebaseUser;
    private RelativeLayout relativeLayout;
    private AnimationDrawable animationDrawable;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // check if user is null
        Paper.init(this);
        String current_session_key = Paper.book().read(Prevalent.pkey);
        /*if (firebaseUser!=null  &&  current_session_key!=null ){
            Intent intent = new Intent(StartActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        // For animation
        relativeLayout = findViewById(R.id.start_act);
        animationDrawable = (AnimationDrawable)relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(4000);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        login = findViewById(R.id.login);
        register = findViewById(R.id.register);

        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
               // startActivity(new Intent(StartActivity.this,LoginActivity.class));
            }
        });
        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this,RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                //startActivity(new Intent(StartActivity.this,RegisterActivity.class));
            }
        });
    }
}
