package com.itsmcodez.assetstudio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.itsmcodez.assetstudio.databinding.LayoutListItemBinding;
import java.util.ArrayList;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ListItemViewHolder> {
    private LayoutListItemBinding binding;
    private Context context;
    private ArrayList<String> data;
    private OnItemClickListener onItemClickListener;

    public ListItemAdapter(Context context, ArrayList<String> data) {
        this.context = context;
        this.data = data;
    }

    static class ListItemViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout itemView;
        public TextView name;

        public ListItemViewHolder(LayoutListItemBinding binding) {
            super(binding.getRoot());
            this.itemView = binding.itemView;
            this.name = binding.name;
        }
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        // Bind to views
        binding = LayoutListItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ListItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        String data = this.data.get(position);
        holder.name.setText(data);
        holder.itemView.setOnClickListener(view -> {
                if(onItemClickListener != null) {
                	onItemClickListener.onItemClick(view, data, position);
                }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
    
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    	this.onItemClickListener = onItemClickListener;
    }
    
    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(View itemView, String model, int position);
    }
}
