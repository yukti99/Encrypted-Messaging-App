package com.example.mychatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mychatapp.Fragments.ChatsFragment;
import com.example.mychatapp.Fragments.UsersFragment;
import com.example.mychatapp.Globals;
import com.example.mychatapp.MessageActivity;
import com.example.mychatapp.Model.Chat;
import com.example.mychatapp.Model.User;
import com.example.mychatapp.R;
import com.google.android.gms.common.FirstPartyScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private boolean ischat,check;
    private String stringMessage;
    private byte encryptionKey[] = {9,115,51,86,105,4,-31,-13,-68,88,17,20,3,-105,119,-53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec,secretKeySpec1;

    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat,boolean check) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
        this.check = check;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")){
            holder.profileImage.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(mContext).load(user.getImageURL()).into(holder.profileImage);
        }


        if (ischat && !check) {
            last_message_count(user.getId(), holder.last_msg,false);
            // lastMessage(user.getId(),holder.last_msg);
        }
        // for users fragment
        else if (ischat && check){
            last_message_count(user.getId(), holder.last_msg,true);
        }
        else{
            holder.last_msg.setVisibility(View.GONE);
        }


        if (ischat){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility((View.VISIBLE));
                holder.img_off.setVisibility(View.GONE);
            }else{
                holder.img_on.setVisibility((View.GONE));
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }else{
            holder.img_on.setVisibility((View.GONE));
            holder.img_off.setVisibility(View.GONE);
        }
        final Globals USER_ID = Globals.getInstance();


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid",user.getId());
                USER_ID.setData(user.getId());
                mContext.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public ImageView profileImage;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;


        public ViewHolder(View itemView){
            super(itemView);
            username=itemView.findViewById(R.id.username);
            profileImage=itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);

            try {
                cipher = Cipher.getInstance("AES");
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                decipher = Cipher.getInstance("AES");
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            secretKeySpec = new SecretKeySpec(encryptionKey,"AES");

        }
    }

    // to count new messages received from a particular count
    private void last_message_count(final String userid, final TextView last_msg, final boolean ch){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        if(firebaseUser != null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int count_msg=0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) && chat.isIsseen()==false){
                            count_msg++;
                        }
                        /*if ((chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) || (chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid()))) {
                            stringMessage = chat.getMessage();
                            theLastMessage =  stringMessage;
                        }*/

                    }
                    if (count_msg==0 && ch==false){
                        last_msg.setText("No New Message");

                    }else if (count_msg==0&& ch==true){
                        last_msg.setText("");

                    }else{
                        last_msg.setText(count_msg+" New Messages");

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
    // check for last message
    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        if(firebaseUser != null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if ((chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) || (chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid()))) {
                            stringMessage = chat.getMessage();
                            theLastMessage =  stringMessage;
                            /*try {
                                theLastMessage =  AESDecryptionMethod(stringMessage);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }*/
                        }
                        switch (theLastMessage) {
                            case "default":
                                last_msg.setText("No New Message");
                                break;
                            default:
                                last_msg.setText(theLastMessage);
                                break;

                        }
                        theLastMessage = "default";

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    /*
    private String AESDecryptionMethod(String encMsg,SecretKeySpec secretKeySpec1) throws UnsupportedEncodingException {
        byte[] EncryptedByte = encMsg.getBytes("ISO-8859-1");
        String decryptedString = encMsg;

        byte[] decryption ;
        try {
            decipher.init(cipher.DECRYPT_MODE,secretKeySpec);
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

        try {
            theLastMessage =  AESDecryptionMethod(stringMessage);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    switch (theLastMessage) {
        case "default":
            last_msg.setText("No New Message");
            break;
        default:
            last_msg.setText(theLastMessage);
            break;

    }
    theLastMessage = "default";



    */

}
