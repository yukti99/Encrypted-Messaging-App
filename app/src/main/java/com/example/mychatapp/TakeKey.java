package com.example.mychatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import javax.microedition.khronos.egl.EGLDisplay;

public class TakeKey extends AppCompatActivity {

    EditText input_key;
    Button submit_btn,cancel_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_key);
        input_key = findViewById(R.id.key_input);
        submit_btn = findViewById(R.id.submit_btn);
        cancel_btn = findViewById(R.id.cancel_btn);
        final Globals g = Globals.getInstance();

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String entered_key = input_key.getText().toString();
               //if (TextUtils.isEmpty(entered_key)){
                    Intent intent = new Intent(TakeKey.this, MessageActivity.class);
                    g.setData(entered_key);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                //}
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TakeKey.this, MessageActivity.class);
               // Globals g1 = Globals.getInstance();
                //g.setData(null);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(intent);
                finish();


            }
        });


    }
}
