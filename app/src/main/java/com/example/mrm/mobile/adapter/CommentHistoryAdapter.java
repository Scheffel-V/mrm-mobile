package com.example.mrm.mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrm.mobile.R;
import com.example.mrm.mobile.model.CommentHistory;

import java.util.List;

public class CommentHistoryAdapter extends RecyclerView.Adapter<CommentHistoryAdapter.ViewHolder> {

    Context context;
    List<CommentHistory> commentHistoryList;

    public CommentHistoryAdapter(Context context, List<CommentHistory> commentHistoryList) {
        this.context = context;
        this.commentHistoryList = commentHistoryList;
    }

    @NonNull
    @Override
    public CommentHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHistoryAdapter.ViewHolder holder, int position) {
        if (this.commentHistoryList != null && this.commentHistoryList.size() > 0) {
            CommentHistory commentHistory = this.commentHistoryList.get(position);
            holder.date_tv.setText(commentHistory.getDate());
            holder.event_tv.setText(commentHistory.getEvent());
            holder.comment_tv.setText(commentHistory.getComment());
        } else {
            return;
        }
    }

    @Override
    public int getItemCount() {
        return this.commentHistoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView date_tv, event_tv, comment_tv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            date_tv = itemView.findViewById(R.id.date_tv);
            event_tv = itemView.findViewById(R.id.event_tv);
            comment_tv = itemView.findViewById(R.id.comment_tv);
        }
    }
}
