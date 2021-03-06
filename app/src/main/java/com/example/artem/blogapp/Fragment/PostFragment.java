package com.example.artem.blogapp.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.artem.blogapp.Activity.CommentActivity;
import com.example.artem.blogapp.Model.Posts;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostFragment extends Fragment {

    private RecyclerView recyclerPost;

    private DatabaseReference postReference;
    private DatabaseReference userReference;
    private FirebaseAuth firebaseAuth;

    public Context context;

    private View view;
    private LinearLayoutManager layoutManager;

    private String currentUserId;

    public PostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_post, container, false);

        recyclerPost = view.findViewById(R.id.recycler_post);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference.keepSynced(true);
        userReference.keepSynced(true);

        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerPost.setHasFixedSize(true);
        recyclerPost.setLayoutManager(layoutManager);

        context = container.getContext();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = postReference.orderByChild("timestamp");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(query, Posts.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, PostViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder holder, int position, @NonNull final Posts model) {

                final String postId = getRef(position).getKey();
                postReference.child(postId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String postDescription = dataSnapshot.child("desc").getValue().toString();
                            String postImage = dataSnapshot.child("image_url").getValue().toString();
                            long postTimestamp = Long.parseLong(dataSnapshot.child("timestamp").getValue().toString());

                            holder.postDesc.setText(postDescription);
                            holder.setImage(postImage, getContext());
                            holder.getDataOfTimeStamp(postTimestamp);

                            String postUserId = dataSnapshot.child("user_id").getValue().toString();

                            if (!postUserId.equals(currentUserId)) {
                                holder.postMenu.setEnabled(false);
                                holder.postMenu.setVisibility(View.INVISIBLE);
                            }
                            userReference.child(postUserId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String postProfileName = dataSnapshot.child("name").getValue().toString();
                                    String postProfileImage = dataSnapshot.child("thumb_image").getValue().toString();

                                    holder.postUserName.setText(postProfileName);
                                    holder.setUserImage(postProfileImage, getContext());
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

                postReference.child(postId).child("Comments").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            holder.postCommentCount.setText(dataSnapshot.getChildrenCount() + " comments");
                        }else {
                            holder.postCommentCount.setText("0 comments");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                postReference.child(postId).child("Likes").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() != 0){
                            holder.likeBtn.setImageDrawable(context.getDrawable(R.mipmap.ic_favorite_like));
                            holder.postLikeCount.setText(dataSnapshot.getChildrenCount() + " likes");
                        }else{
                            holder.likeBtn.setImageDrawable(context.getDrawable(R.mipmap.ic_favorite));
                            holder.postLikeCount.setText("0 likes");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DatabaseReference likeReference = postReference.child(postId).child("Likes");
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

                holder.postMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(context, holder.postMenu);
                        popupMenu.inflate(R.menu.option_menu);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.edit:

                                        break;
                                    case R.id.delete:
                                        postReference.child(postId).removeValue();
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

                holder.commentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(getActivity(), CommentActivity.class);
                        commentIntent.putExtra("blogPostId", postId);
                        context.startActivity(commentIntent);
                    }
                });


            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_item, viewGroup, false);
                return new PostViewHolder(view);
            }

        };

        adapter.startListening();
        recyclerPost.setAdapter(adapter);
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView postUserImage;
        private ImageView postImage;
        private TextView postUserName, postTime, postDesc, postLikeCount, postCommentCount, postMenu;
        private View view;
        private ImageView likeBtn, commentBtn;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            postUserName = (TextView) view.findViewById(R.id.comment_post_user_name);
            postTime = (TextView) view.findViewById(R.id.post_time);
            postDesc = (TextView) view.findViewById(R.id.comment_post_description);
            postMenu = (TextView) view.findViewById(R.id.comment_post_menu);
            postLikeCount = (TextView) view.findViewById(R.id.post_like_count);
            postCommentCount = (TextView) view.findViewById(R.id.comment_post_count);
            postImage = (ImageView) view.findViewById(R.id.comment_post_image);
            likeBtn = (ImageView) view.findViewById(R.id.comment_post_like_btn);
            commentBtn = (ImageView) view.findViewById(R.id.post_comment_btn);
            postUserImage = (CircleImageView) view.findViewById(R.id.comment_post_user_image);

        }

        public void setImage(String image, Context context) {
            Picasso.get().load(image).into(postImage);
        }
        public void setUserImage(String image, Context context) {
            Picasso.get().load(image).placeholder(R.drawable.user_default).into(postUserImage);
        }
        public void getDataOfTimeStamp(long timestamp){
            String postDate = DateFormat.format("dd-MM-yy HH:mm", new Date(timestamp)).toString();
            postTime.setText(postDate);
        }

    }
}
