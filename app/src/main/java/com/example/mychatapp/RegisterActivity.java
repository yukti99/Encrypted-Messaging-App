package com.example.mychatapp;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.example.mychatapp.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;


import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import io.paperdb.Paper;

import static com.example.mychatapp.Prevalent.Prevalent.pkey;

public class RegisterActivity extends AppCompatActivity {
    MaterialEditText username, email, password;
    Button btn_register;
    FirebaseAuth auth;
    DatabaseReference reference;
    private byte encKey[] = {9,115,51,86,105,4,-31,-13,-68,88,17,20,3,-105,119,-53};
    DBHelper myDb;
    private RelativeLayout relativeLayout;
    private AnimationDrawable animationDrawable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById((R.id.toolbar));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorLavender), PorterDuff.Mode.SRC_ATOP);

        // For Background Animation
        relativeLayout = findViewById(R.id.register_form);
        animationDrawable = (AnimationDrawable)relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(4000);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();
        //----------------------------------------------------------------------------------------

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById((R.id.btn_register));
        auth = FirebaseAuth.getInstance();
        btn_register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                if (TextUtils.isEmpty(txt_username)|| TextUtils.isEmpty(txt_email)||TextUtils.isEmpty(txt_password)){
                    Toast.makeText(RegisterActivity.this,"All the Fields are required!",Toast.LENGTH_SHORT).show();
                }else if(txt_password.length()<6){
                   Toast.makeText(RegisterActivity.this,"Password must be at least 6 characters!",Toast.LENGTH_SHORT).show();
                }else{
                    register(txt_username,txt_email,txt_password);
                }
            }
        });

    }
    private void register(final String username, String email, String password){
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                           /*
                            Random random = new Random();
                            int max = 100,min=-100;
                            for(int i=0;i<16;i++){
                                encKey[i] = (byte) (random.nextInt(max - min) + min);
                                System.out.println(encKey[i]+" ");
                            }
                            System.out.println("\n");
                            String Pkey = encKey.toString();
                              //Paper.book().write(user_id,userid);
                            //Paper.book().write(pkey ,Pkey);
                            //hashMap.put("pkey", Pkey);
                            //hashMap.put("pkey","default");
                            */
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser!=null;
                            String userid  = firebaseUser.getUid();
                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("id",userid);
                            hashMap.put("username",username);
                            hashMap.put("imageURL","default");
                            hashMap.put("status","offline");
                            hashMap.put("search",username.toLowerCase());
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Intent intent = new Intent(RegisterActivity.this,StartActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(RegisterActivity.this,"You can't register with this email or password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
