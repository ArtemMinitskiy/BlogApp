package com.example.artem.blogapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.artem.blogapp.Model.Comments;
import com.example.artem.blogapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private EditText commentText;
    private ImageButton commentBtn;
    private RecyclerView recyclerComment;
    private TextView postUserName, postTime, postDesc, postLikeCount, postCommentCount, postMenu;
    private ImageView likeBtn, postImage;
    private CircleImageView postUserImage;

    private String blogPostId;
    private String currentUserId;

    private DatabaseReference postReference;
    private DatabaseReference userReference;
    private FirebaseAuth commentsAuth;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        commentText = (EditText) findViewById(R.id.comment_text);
        commentBtn = (ImageButton) findViewById(R.id.comment_btn);
        recyclerComment = (RecyclerView) findViewById(R.id.recycler_comment);
        postUserName = (TextView) findViewById(R.id.comment_post_user_name);
        postTime = (TextView) findViewById(R.id.comment_post_time);
        postDesc = (TextView) findViewById(R.id.comment_post_description);
        postMenu = (TextView) findViewById(R.id.comment_post_menu);
        postLikeCount = (TextView) findViewById(R.id.comment_post_like_count);
        postCommentCount = (TextView) findViewById(R.id.comment_post_count);
        postImage = (ImageView) findViewById(R.id.comment_post_image);
        likeBtn = (ImageView) findViewById(R.id.comment_post_like_btn);
        postUserImage = (CircleImageView) findViewById(R.id.comment_post_user_image);

        commentsAuth = FirebaseAuth.getInstance();
        currentUserId = commentsAuth.getCurrentUser().getUid();
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference.keepSynced(true);
        userReference.keepSynced(true);

        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        blogPostId = getIntent().getStringExtra("blogPostId");

        getPostData();

        recyclerComment.setHasFixedSize(true);
        recyclerComment.setLayoutManager(new LinearLayoutManager(this));

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentMessage = commentText.getText().toString();
                final DatabaseReference commentsReference = postReference.child(blogPostId).child("Comments").push();
                if (!commentMessage.isEmpty()){
                    final Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("message", commentMessage);
                    commentMap.put("user_id", currentUserId);
                    commentMap.put("timestamp", ServerValue.TIMESTAMP);
                    commentsReference.setValue(commentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                }
            }
        });

    }

    public void getPostData(){
        postReference.child(blogPostId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String postDescription = dataSnapshot.child("desc").getValue().toString();
                    String postImage = dataSnapshot.child("image_url").getValue().toString();
                    long postTimestamp = Long.parseLong(dataSnapshot.child("timestamp").getValue().toString());

                    postDesc.setText(postDescription);
                    setImage(postImage, context);
                    getDataOfTimeStampPost(postTimestamp);

                    String postUserId = dataSnapshot.child("user_id").getValue().toString();

                    if (!postUserId.equals(currentUserId)) {
                        postMenu.setEnabled(false);
                        postMenu.setVisibility(View.INVISIBLE);
                    }
                    userReference.child(postUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String postProfileName = dataSnapshot.child("name").getValue().toString();
                            String postProfileImage = dataSnapshot.child("thumb_image").getValue().toString();

                            postUserName.setText(postProfileName);
                            setUserPostImage(postProfileImage, context);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postReference.child(blogPostId).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    postCommentCount.setText(dataSnapshot.getChildrenCount() + " comments");
                }else {
                    postCommentCount.setText("0 comments");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postReference.child(blogPostId).child("Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() != 0){
                    likeBtn.setImageDrawable(getDrawable(R.mipmap.ic_favorite_like));
                    postLikeCount.setText(dataSnapshot.getChildrenCount() + " likes");
                }else{
                    likeBtn.setImageDrawable(getDrawable(R.mipmap.ic_favorite));
                    postLikeCount.setText("0 likes");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference likeReference = postReference.child(blogPostId).child("Likes");
                likeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild(currentUserId)) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("user_id", currentUserId);
                            likeReference.child(currentUserId).setValue(likesMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }else {
                            likeReference.child(currentUserId).removeValue();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = postReference.child(blogPostId).child("Comments").orderByChild("timestamp");

        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(query, Comments.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Comments, ViewHolder>(options) {


            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_item, viewGroup, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull Comments model) {
                final String commentId = getRef(position).getKey();
                final Context context = holder.itemView.getContext();
                postReference.child(blogPostId).child("Comments").child(commentId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String comment = dataSnapshot.child("message").getValue().toString();
                            long commentTimestamp = Long.parseLong(dataSnapshot.child("timestamp").getValue().toString());
                            holder.commentMessage.setText(comment);
                            holder.getDataOfTimeStampComment(commentTimestamp);
                            String commentUserId = dataSnapshot.child("user_id").getValue().toString();
                            userReference.child(commentUserId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String profileName = dataSnapshot.child("name").getValue().toString();
                                    String profileImage = dataSnapshot.child("thumb_image").getValue().toString();
                                    holder.commentUserName.setText(profileName);
                                    holder.setUserImage(profileImage, context);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                holder.commentMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(context, holder.commentMenu);
                        popupMenu.inflate(R.menu.option_menu);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.edit:

                                        break;
                                    case R.id.delete:
                                        postReference.child(blogPostId).child("Comments").child(commentId).removeValue();
                                        break;
                                    default:
                                        break;
                                }
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
        };
        adapter.startListening();
        recyclerComment.setAdapter(adapter);

//        getPostData();
    }

    public void setImage(String image, Context context) {
        Picasso.get().load(image).into(postImage);
    }

    public void getDataOfTimeStampPost(long timestamp){
        String postDate = DateFormat.format("dd-MM-yy HH:mm", new Date(timestamp)).toString();
        postTime.setText(postDate);
    }

    public void setUserPostImage(String image, Context context) {
        Picasso.get().load(image).placeholder(R.drawable.user_default).into(postUserImage);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(CommentActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView commentUserName, commentMessage, commentTime, commentMenu;
        private CircleImageView commentUserImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            commentUserName = (TextView) view.findViewById(R.id.comment_username);
            commentMessage = (TextView) view.findViewById(R.id.comment_message);
            commentMenu = (TextView) view.findViewById(R.id.comment_menu);
            commentTime = (TextView) view.findViewById(R.id.comment_time);
            commentUserImage = (CircleImageView) view.findViewById(R.id.comment_image);

        }

        public void getDataOfTimeStampComment(long timestamp){
            String commentDate = DateFormat.format("dd-MM-yy HH:mm", new Date(timestamp)).toString();
            commentTime.setText(commentDate);
        }

        public void setUserImage(String image, Context context) {
            Picasso.get().load(image).placeholder(R.drawable.user_default).into(commentUserImage);
        }

    }
}
