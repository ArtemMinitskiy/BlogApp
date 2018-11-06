package com.example.artem.blogapp.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.artem.blogapp.Model.Posts;
import com.example.artem.blogapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

//import java.sql.Date;
import java.util.Date;
import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostFragment extends Fragment {

    private RecyclerView recyclerPost;

    private DatabaseReference postReference;
    private DatabaseReference userReference;
    private FirebaseAuth firebaseAuth;

    private View view;

    private String currentUserId;

    public PostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_post, container, false);

        recyclerPost = view.findViewById(R.id.recycler_post);

        firebaseAuth = FirebaseAuth.getInstance();
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference.keepSynced(true);
        userReference.keepSynced(true);

        recyclerPost.setHasFixedSize(true);
        recyclerPost.setLayoutManager(new LinearLayoutManager(getContext()));

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
                final String listUserId = getRef(position).getKey();
                postReference.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String postDescription = dataSnapshot.child("desc").getValue().toString();
                        String postImage = dataSnapshot.child("image_url").getValue().toString();
                        long postTimestamp = Long.parseLong(dataSnapshot.child("timestamp").getValue().toString());

                        holder.postDesc.setText(postDescription);
                        holder.setImage(postImage, getContext());
                        holder.getDataOfTimeStamp(postTimestamp);

                        String postUserId = dataSnapshot.child("user_id").getValue().toString();

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

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_item, viewGroup, false);
                return new PostViewHolder(view);
            }

        };

        adapter.startListening();
        recyclerPost.setAdapter(adapter);
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView postUserImage;
        private ImageView postImage;
        private TextView postUserName, postTime, postDesc;
        private View view;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            postUserName = (TextView) view.findViewById(R.id.post_user_name);
            postTime = (TextView) view.findViewById(R.id.post_time);
            postDesc = (TextView) view.findViewById(R.id.post_description);
//            blogLikeCount = (TextView) view.findViewById(R.id.blog_like_count);
//            blogCommentCount = (TextView) view.findViewById(R.id.blog_comment_count);
            postImage = (ImageView) view.findViewById(R.id.post_image);
//            likeBtn = (ImageView) view.findViewById(R.id.blog_like_btn);
//            commentBtn = (ImageView) view.findViewById(R.id.blog_comment_btn);
            postUserImage = (CircleImageView) view.findViewById(R.id.post_user_image);

        }

        public void setImage(String image, Context context) {
            Picasso.get().load(image).into(postImage);
        }
        public void setUserImage(String image, Context context) {
            Picasso.get().load(image).placeholder(R.drawable.user_default).into(postUserImage);
        }
        public void getDataOfTimeStamp(long timestamp){
            Date time = new Date(timestamp);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            simpleDateFormat.format(time);
            postTime.setText(time.toString());
        }

    }
}
