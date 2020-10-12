package com.example.mychatapp.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.IpSecManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mychatapp.Adapter.MessageAdapter;
import com.example.mychatapp.Adapter.UserAdapter;
import com.example.mychatapp.MessageActivity;
import com.example.mychatapp.Model.Chat;
import com.example.mychatapp.Model.User;
import com.example.mychatapp.Prevalent.Prevalent;
import com.example.mychatapp.R;
import com.example.mychatapp.RegisterActivity;
import com.example.mychatapp.StartActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;



import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    CircleImageView image_profile;
    TextView username;
    DatabaseReference reference;
    FirebaseUser fuser;
    TextView session_key,txt_email;


    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_profile,container,false);

       image_profile = view.findViewById(R.id.profile_image);
       username = view.findViewById(R.id.username);
       session_key = view.findViewById(R.id.session_key);
       txt_email = view.findViewById(R.id.email_id);


       storageReference = FirebaseStorage.getInstance().getReference("uploads");
       fuser = FirebaseAuth.getInstance().getCurrentUser();
       reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        Paper.init(getContext());
        String sk = Paper.book().read(Prevalent.pkey);
        String em = Paper.book().read(Prevalent.Email);
        session_key.setText("Your Current Session Private Key : "+sk);
        if (!TextUtils.isEmpty(em)) {
            txt_email.setText("Email ID : " + em);
        }else{
            txt_email.setVisibility(View.GONE);
        }

       reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               User user = dataSnapshot.getValue(User.class);
               username.setText(user.getUsername());
               if (user.getImageURL().equals("default")){
                   image_profile.setImageResource(R.mipmap.ic_launcher);
               }else{
                   if(isAdded()){
                       Glide.with(getContext()).load(user.getImageURL()).into(image_profile);
                   }else{
                       image_profile.setImageResource(R.mipmap.ic_launcher);
                   }
                   //Glide.with(getContext()).load(user.getImageURL()).into(image_profile);
               }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

       image_profile.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               //CropImage.activity(imageUri).setAspectRatio(1, 1);
               openImage();
           }
       });

       username.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               changeUsername();

           }
       });


        return view;
    }

    private void changeUsername(){
        final String[] new_name = new String[1];
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter the new username: ");
        final EditText input = new EditText(getContext());
        builder.setView(input);
        builder.setPositiveButton("Change",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        if (input.getText().toString().equals("")){
                            Toast.makeText(getContext(),"Username cannot be empty!",Toast.LENGTH_SHORT);
                        }else {
                            new_name[0] = input.getText().toString();
                            Log.v("New USERNAME ", "" + new_name[0]);
                            reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("username", new_name[0]);
                            hashMap.put("search", new_name[0].toLowerCase());
                            reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Update Successful!", Toast.LENGTH_SHORT);

                                        username.setText(new_name[0]);
                                    } else {
                                        Toast.makeText(getContext(), "Failed! Please Try again Later..", Toast.LENGTH_SHORT);

                                    }
                                }

                            });
                        }
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();

    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
       // By the help of android startActivityForResult() method, we can send information from one activity to another and vice-versa
        startActivityForResult(intent,IMAGE_REQUEST);
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }
    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();
        if (imageUri != null){
            final StorageReference fileReference = storageReference.child( System.currentTimeMillis()+"."+getFileExtension(imageUri) );
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();
                        reference  = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("imageURL",mUri);
                        reference.updateChildren(map);
                        pd.dismiss();

                    }else{
                        Toast.makeText(getContext(),"Failed",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }else{
            Toast.makeText(getContext(),"No image selected!",Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData()!=null){
          /*  CropImage.activity(data.getData())
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setGuidelines(CropImageView.Guidelines.OFF)
                    .setAutoZoomEnabled(false)
                    .start(getContext(), ProfileFragment.this);
            System.out.println("hello4");
           // CropImage.activity(data.getData()).setAspectRatio(1, 1).start(getContext(),ProfileFragment.this);
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();*/
            imageUri = data.getData();
            System.out.println("hello3");
            System.out.println(imageUri);
            if (uploadTask!=null && uploadTask.isInProgress()){
                System.out.println("hello2");
                Toast.makeText(getContext(),"Upload in progress..",Toast.LENGTH_SHORT).show();
            }else{
                System.out.println("hello");
                uploadImage();
            }

        }

    }
}


                        /*
                        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        System.out.println(firebaseUser);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        User user = snapshot.getValue(User.class);
                                        System.out.println(user);
                                        assert user != null;
                                        assert firebaseUser != null;
                                        System.out.println(firebaseUser.getUid());
                                        if (!user.getId().equals(firebaseUser.getUid())) {
                                            HashMap<String, Object> userMap = new HashMap<>();
                                            userMap. put("image", myUrl);


                                        }
                                    }
                                    // check if its true or false, before it was false
                                    userAdapter = new UserAdapter(getContext(), mUsers, false,true);
                                    recyclerView.setAdapter(userAdapter);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });*/
