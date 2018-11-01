package com.example.artem.blogapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artem.blogapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName, profileStatus;
    private Button sendReqBtn, declineBtn;

    private DatabaseReference usersDatabase;

    private ProgressDialog profileProgress;

    private DatabaseReference friendReqDatabase;
    private DatabaseReference friendDatabase;

    private DatabaseReference rootRef;

    private FirebaseUser currentUser;

    private String currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final String user_id = getIntent().getStringExtra("user_id");

        rootRef = FirebaseDatabase.getInstance().getReference();

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friend");
    
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileImage = (ImageView) findViewById(R.id.profile_image);
        profileName = (TextView) findViewById(R.id.profile_name);
        profileStatus = (TextView) findViewById(R.id.profile_status);
        sendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        declineBtn = (Button) findViewById(R.id.profile_decline_btn);
        
        currentState = "not_friends";

        declineBtn.setVisibility(View.INVISIBLE);
        declineBtn.setEnabled(false);
        
        profileProgress = new ProgressDialog(this);
        profileProgress.setTitle("Loading User Data");
        profileProgress.setMessage("Please wait while we load the user data.");
        profileProgress.setCanceledOnTouchOutside(false);
        profileProgress.show();
        
        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                profileName.setText(display_name);
                profileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.user_default).into(profileImage);

                if(currentUser.getUid().equals(user_id)){

                    declineBtn.setEnabled(false);
                    declineBtn.setVisibility(View.INVISIBLE);

                    sendReqBtn.setEnabled(false);
                    sendReqBtn.setVisibility(View.INVISIBLE);

                }

                friendReqDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                currentState = "req_received";
                                sendReqBtn.setText("Accept Friend Request");

                                declineBtn.setVisibility(View.VISIBLE);
                                declineBtn.setEnabled(true);
                                
                            } else if(req_type.equals("sent")) {

                                currentState = "req_sent";
                                sendReqBtn.setText("Cancel Friend Request");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);

                            }
                            profileProgress.dismiss();


                        } else {
                            
                            friendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        currentState = "friends";
                                        sendReqBtn.setText("Unfriend this Person");

                                        declineBtn.setVisibility(View.INVISIBLE);
                                        declineBtn.setEnabled(false);

                                    }

                                    profileProgress.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    profileProgress.dismiss();

                                }
                            });

                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        sendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendReqBtn.setEnabled(false);

                if(currentState.equals("not_friends")){
                    
                    DatabaseReference notificationRef = rootRef.child("Notification").child(user_id).push();
                    String newNotificationId = notificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_request/" + currentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_request/" + user_id + "/" + currentUser.getUid() + "/request_type", "received");
                    requestMap.put("Notification/" + user_id + "/" + newNotificationId, notificationData);

                    rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            } else {

                                currentState = "req_sent";
                                sendReqBtn.setText("Cancel Friend Request");

                            }

                            sendReqBtn.setEnabled(true);

                        }
                    });

                }

                if(currentState.equals("req_sent")){

                    friendReqDatabase.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendReqDatabase.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendReqBtn.setEnabled(true);
                                    currentState = "not_friends";
                                    sendReqBtn.setText("Send Friend Request");

                                    declineBtn.setVisibility(View.INVISIBLE);
                                    declineBtn.setEnabled(false);


                                }
                            });

                        }
                    });

                }

                if(currentState.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friend/" + currentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friend/" + user_id + "/"  + currentUser.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_request/" + currentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_request/" + user_id + "/" + currentUser.getUid(), null);

                    rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){
                                sendReqBtn.setEnabled(true);
                                currentState = "friends";
                                sendReqBtn.setText("Unfriend this Person");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);

                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                }

                if(currentState.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friend/" + currentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friend/" + user_id + "/" + currentUser.getUid(), null);

                    rootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                currentState = "not_friends";
                                sendReqBtn.setText("Send Friend Request");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);

                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }
                            sendReqBtn.setEnabled(true);

                        }
                    });

                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ProfileActivity.this, UsersActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
