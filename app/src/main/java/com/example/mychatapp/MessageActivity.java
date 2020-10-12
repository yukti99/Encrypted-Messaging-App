package com.example.mychatapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mychatapp.Adapter.MessageAdapter;
import com.example.mychatapp.Adapter.UserAdapter;
import com.example.mychatapp.Fragments.APIService;
import com.example.mychatapp.Model.Chat;
import com.example.mychatapp.Model.User;
import com.example.mychatapp.Notifications.Data;
import com.example.mychatapp.Notifications.MyResponse;
import com.example.mychatapp.Notifications.Sender;
import com.example.mychatapp.Notifications.Token;
import com.example.mychatapp.Prevalent.Prevalent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    FirebaseUser fuser;

    DatabaseReference reference,sref;
    Intent intent;

    EditText text_send;
    ImageButton btn_send;

    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;
    String userid;
    EditText key_input;
    private EditText result;
    boolean active;
    boolean receiver_active ;

    private String senderEncryptionKey;

    ValueEventListener seenListener;

    private String stringMessage;
    private byte encryptionKey[] = {9,115,51,86,105,4,-31,-13,-68,88,17,20,3,-105,119,-53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec,secretKeySpec1;

    String senderKey, receiverKey;

    APIService apiService;
    boolean notify = false;
    boolean do_decrypt = false;
    Button decrypt_btn;
    final Globals g = Globals.getInstance();
    final Globals USER_ID = Globals.getInstance();

    DBHelper myDb;
    private TextView resultText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        //Toast.makeText(MessageActivity.this,encryptionKey.toString(),Toast.LENGTH_SHORT).show();
        //System.out.println(encryptionKey.toString());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              // startActivity(new Intent(getApplicationContext(),MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorLavender), PorterDuff.Mode.SRC_ATOP);
        Paper.init(this);
        resultText = (TextView) findViewById(R.id.key_input);




        // TO GET CURRENT USER'S LOGIN SESSION PRIVATE KEY
        senderKey = Paper.book().read(Prevalent.pkey);
        System.out.println("KEY =  "+senderKey);
        /* THE MESSAGE MUST BE ENCRYPTED BEFORE STORING IN ONLINE DATABASE*/
        byte byteKey[]  = senderKey.getBytes();
        for(int i=0;i<byteKey.length;i++){
            System.out.print(byteKey[i]+" ");
        }
        // It can be used to construct a SecretKey from a byte array using a
        // particular cryptography algorithm
        secretKeySpec = new SecretKeySpec(byteKey,"AES");
        System.out.println(secretKeySpec);

        // THE CIPHER IS CREATED USING THIS KEY
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // THE DECIPHER IS CREATED USING AES ALGORITHM
        try {
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        reference = FirebaseDatabase.getInstance().getReference("Chats");
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        key_input = findViewById(R.id.key_input);
        text_send = findViewById(R.id.text_send);
        intent = getIntent();
        myDb = new DBHelper(this);

        //fuser IS THE SENDER WHILE userid IS THAT OF THE RECEIVER
        userid = intent.getStringExtra("userid");
        //userid = USER_ID.getData();
        System.out.println(userid);
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();
                if (!msg.equals("")){
                    send_message(fuser.getUid(),userid,msg,"default");
                }else{
                    Toast.makeText(MessageActivity.this,"You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");

            }
        });
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
               // Toast.makeText(getApplicationContext(),"If your private Key is correct you will be able to read the message!",Toast.LENGTH_SHORT).show();
                readMessage(fuser.getUid(), userid, user.getImageURL());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid);
    }

    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid())&& chat.getSender().equals(userid)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void send_message(final String sender, final String receiver, String message, String imageurl){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);

        String encryptedMsg = AESEncryptionMethod(message);

        hashMap.put("message",encryptedMsg);
        hashMap.put("isseen",false);
        reference.child("Chats").push().setValue(hashMap);
        // "fuser" IS THE SENDER WHILE "userid" IS THAT OF THE RECEIVER
        // To add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("Chatlist").child(fuser.getUid()).child(userid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
       private String AESEncryptionMethod(String msg){
        // the message to be encrypted is converted to byte array
        byte[] stringByte = msg.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];
        try {
            // the secretKeySpec created above is used to encrypt the message
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        String returnString = null;
        try {
            // ISO-8859-1 is being used here, but we can use any other
            // standard also  depending on requirement
            returnString =  new String(encryptedByte,"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // return the encrypted- ciphertext formed after AES encryption
        return returnString;
        }

        private String AESDecryptionMethod(String encMsg,SecretKeySpec secretKeySpec1) throws UnsupportedEncodingException {
            byte[] EncryptedByte = encMsg.getBytes("ISO-8859-1");
            String decryptedString = encMsg;
            byte[] decryption ;
            try {
                // here the secretKeySpec1 is formed from the key entered by the receiver
                // if the key was not correct, decryption won't be correct either
                // and hence without correct key, user cannot decrypt/read the messages
                decipher.init(cipher.DECRYPT_MODE,secretKeySpec1);
                decryption = decipher.doFinal(EncryptedByte);
                decryptedString = new String(decryption);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
            return decryptedString;

         }

    private void readMessage(final String myid, final String userid, final String imageurl){

        final String[] input_key = new String[1];
        System.out.println("HI0");
        if (active) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter the 16-bit Receiver's Private Key ");
            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setPositiveButton("SUBMIT",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Log.v("string: ", "" + input.getText().toString());
                            input_key[0] = input.getText().toString();
                            if (input.getText().toString().equals("")){
                                input_key[0]="0";
                            }
                            System.out.println(input_key[0]);

                            byte[] byteKey = (input_key[0].getBytes());
                            secretKeySpec1 = new SecretKeySpec(byteKey,"AES");
                            mchat = new ArrayList<>();
                            reference = FirebaseDatabase.getInstance().getReference("Chats");
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    mchat.clear();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Chat chat = snapshot.getValue(Chat.class);
                                        final String key_used_by_sender = chat.getKey();
                                        String decryptMessage;
                                        //IF I AM THE SENDER
                                        if ((chat.getReceiver().equals(userid) && chat.getSender().equals(myid))){
                                            try {
                                                decryptMessage = AESDecryptionMethod(chat.getMessage(),secretKeySpec);
                                                chat.setMessage(decryptMessage);
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }

                                        }else {// IF I AM THE RECEIVER
                                            try {
                                                decryptMessage = AESDecryptionMethod(chat.getMessage(), secretKeySpec1);
                                                chat.setMessage(decryptMessage);
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if ((chat.getReceiver().equals(myid) && chat.getSender().equals(userid)) ||
                                                (chat.getReceiver().equals(userid) && chat.getSender().equals(myid))) {
                                            mchat.add(chat);

                                        }
                                        messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                                        recyclerView.setAdapter(messageAdapter);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(getApplicationContext(),"Without the Key, You might not be able to read what others have sent you!",Toast.LENGTH_SHORT).show();
                                    mchat = new ArrayList<>();
                                    reference = FirebaseDatabase.getInstance().getReference("Chats");
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            mchat.clear();
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                Chat chat = snapshot.getValue(Chat.class);
                                                final String key_used_by_sender = chat.getKey();
                                                System.out.println("sender = " + chat.getSender());
                                                System.out.println("receiver = " + chat.getReceiver());
                                                System.out.println("key = " + chat.getKey());
                                                System.out.println("msg = " + chat.getMessage());
                                                String decryptMessage;
                                                System.out.println("sender key = " + key_used_by_sender);
                                                if ((chat.getReceiver().equals(userid) && chat.getSender().equals(myid))){
                                                    try {
                                                        decryptMessage = AESDecryptionMethod(chat.getMessage(),secretKeySpec);
                                                        chat.setMessage(decryptMessage);
                                                    } catch (UnsupportedEncodingException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                                if ((chat.getReceiver().equals(myid) && chat.getSender().equals(userid)) ||
                                                        (chat.getReceiver().equals(userid) && chat.getSender().equals(myid))) {
                                                    mchat.add(chat);

                                                }
                                                messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                                                recyclerView.setAdapter(messageAdapter);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            });




                        }
                    });
            builder.setNegativeButton("CANCEL",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            builder.show();
            System.out.println("HI3");
        }
        System.out.println("HI4");

    }







    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        active=false;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
 /*System.out.println("sender = " + chat.getSender());
                                        System.out.println("receiver = " + chat.getReceiver());
                                        System.out.println("key = " + chat.getKey());
                                        System.out.println("msg = " + chat.getMessage());
                                        if (key_used_by_sender != null) {
                                            if (input_key[0] != null && key_used_by_sender.equals(input_key[0])) {
                                                do_decrypt = true;
                                            }

                                        }*/
    //if (do_decrypt == true && secretKeySpec != null) {
    // TO DECRYPT THE STRING


    // DIRECTLY DECRYPT WHAT YOU HAVE SENT!




















    }
