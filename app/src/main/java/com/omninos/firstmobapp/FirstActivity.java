package com.omninos.firstmobapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirstActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private List<String> userList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        initView();
        SetUp();
    }

    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);

    }

    private void SetUp() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);


        if (CommonUtils.isNetworkConnected(FirstActivity.this)){
//            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            DatabaseReference ref = database.child("User");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (userList != null) {
                        userList.clear();
                    }
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        userList.add(snapshot.getKey());
                    }
                    adapter = new MyAdapter(FirstActivity.this, userList, new MyAdapter.Select() {
                        @Override
                        public void Choose(int position) {
                            startActivity(new Intent(FirstActivity.this, MainActivity.class).putExtra("UserId", userList.get(position)));
                        }
                    });

                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else {
            Toast.makeText(this, "Please Check Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }
}
