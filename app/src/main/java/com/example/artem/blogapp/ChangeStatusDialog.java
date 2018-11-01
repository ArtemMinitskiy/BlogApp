package com.example.artem.blogapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.artem.blogapp.Activity.SettingsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangeStatusDialog extends DialogFragment implements View.OnClickListener {

    private View view;
    private EditText changeStatusText;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    private String newStatus;

    private ProgressDialog chgStatusProgress;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.change_status_dialog, null);
        view.findViewById(R.id.chg_status_btn).setOnClickListener(this);
        view.findViewById(R.id.cancel_btn).setOnClickListener(this);
        changeStatusText = (EditText) view.findViewById(R.id.chg_status_text);

        chgStatusProgress = new ProgressDialog(getContext());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = firebaseUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = dataSnapshot.child("status").getValue().toString();

                changeStatusText.setText(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        newStatus = changeStatusText.getText().toString();

        switch (v.getId()) {
            case R.id.chg_status_btn:
                ChangeStatus(newStatus);
                dismiss();
                break;
            case R.id.cancel_btn:
                dismiss();
                break;
            default:
                break;
        }

    }

    private void ChangeStatus(String newStatus) {
        chgStatusProgress.setTitle("Please wait...");
        chgStatusProgress.show();

        databaseReference.child("status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chgStatusProgress.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Error saving changes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        
    }

}
