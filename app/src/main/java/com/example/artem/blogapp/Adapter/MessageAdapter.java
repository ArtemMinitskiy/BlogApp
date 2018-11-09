package com.example.artem.blogapp.Adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.artem.blogapp.Model.Messages;
import com.example.artem.blogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> messagesList;
    private DatabaseReference userDatabase;

    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item, viewGroup, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        final Messages messages = messagesList.get(position);
        final String fromUser = messages.getFrom();
        String messageType = messages.getType();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUser);
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();

                holder.nameUser.setText(name);
                holder.getDataOfTimeStamp(messages.getTime());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (messageType.equals("text")){
            holder.messageText.setText(messages.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }else {
            holder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(messages.getMessage()).placeholder(R.drawable.user_default).into(holder.messageImage);
        }

        if (fromUser.equals(currentUserId)) {
            holder.messageText.getResources().getColor(R.color.messageColor);
            holder.messageText.setTextColor(Color.BLACK);
        }else {
            holder.messageText.setBackgroundResource(R.drawable.text_from_background);
            holder.messageText.setTextColor(Color.BLACK);
        }
        holder.messageText.setText(messages.getMessage());

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{
        private TextView messageText, nameUser, timeMessage;
        private ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.user_chat_message);
            nameUser = (TextView) view.findViewById(R.id.user_chat_name);
            timeMessage = (TextView) view.findViewById(R.id.time_chat_message);
            messageImage = (ImageView) view.findViewById(R.id.message_image);
        }


        public void getDataOfTimeStamp(long timestamp){
            String postDate = DateFormat.format("HH:mm", new Date(timestamp)).toString();
            timeMessage.setText(postDate);
        }
    }
}
