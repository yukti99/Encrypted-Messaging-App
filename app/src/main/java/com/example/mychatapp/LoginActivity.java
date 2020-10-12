package com.example.mychatapp;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mychatapp.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.paperdb.Paper;

import static com.example.mychatapp.Prevalent.Prevalent.Email;
import static com.example.mychatapp.Prevalent.Prevalent.Password;
import static com.example.mychatapp.Prevalent.Prevalent.pkey;



public class LoginActivity extends AppCompatActivity {

    MaterialEditText email, password;
    Button btn_login;
    FirebaseAuth auth;
    TextView forgot_passsword;
    TextView encKey;
    DBHelper myDb;
    final Globals Session_Key = Globals.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById((R.id.toolbar));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorLavender), PorterDuff.Mode.SRC_ATOP);


        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        forgot_passsword = findViewById((R.id.forgot_password));
        encKey = findViewById(R.id.private_key);
        myDb = new DBHelper(this);
        forgot_passsword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));

            }
        });
        Paper.init(this);

        btn_login = findViewById((R.id.btn_register));
        btn_login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                String input_key = encKey.getText().toString();
                if (TextUtils.isEmpty(txt_email)||TextUtils.isEmpty(txt_password)||TextUtils.isEmpty(input_key)) {
                    Toast.makeText(LoginActivity.this,"All the Fields are required!",Toast.LENGTH_SHORT).show();
                }else{
                    System.out.println(txt_email);
                    System.out.println(txt_password);
                    System.out.println(input_key);

                    if (input_key.length()!=16){
                        Toast.makeText(LoginActivity.this,"Please enter a 16-char valid Key!",Toast.LENGTH_SHORT).show();
                    }else {
                        Paper.book().write(pkey,input_key);
                        Paper.book().write(Email,txt_email);
                        Paper.book().write(Password,txt_password);

                        auth = FirebaseAuth.getInstance();
                        auth.signInWithEmailAndPassword(txt_email, txt_password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Welcome!!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                    }

                }
            }
        });

    }
}
