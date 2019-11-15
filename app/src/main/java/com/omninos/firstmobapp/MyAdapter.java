package com.omninos.firstmobapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by Manjinder Singh on 15 , November , 2019
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    List<String> list;
    Select select;

    public MyAdapter(Context context, List<String> list, Select select) {
        this.context = context;
        this.list = list;
        this.select = select;
    }

    public interface Select {
        void Choose(int position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.userId.setText(list.get(position));
        holder.userId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select.Choose(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView userId;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userId = itemView.findViewById(R.id.userId);
        }
    }
}
