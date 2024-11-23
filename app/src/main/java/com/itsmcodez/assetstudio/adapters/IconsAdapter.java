package com.itsmcodez.assetstudio.adapters;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.itsmcodez.assetstudio.databinding.LayoutIconItemBinding;
import com.itsmcodez.assetstudio.listeners.OnItemClickListener;
import com.itsmcodez.assetstudio.models.IconModel;
import java.util.ArrayList;
import java.util.List;

public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.MyViewHolder> {
    private LayoutIconItemBinding binding;
    private Context context;
    private List<IconModel> icons;
    private OnItemClickListener onItemClickListener;

    public IconsAdapter(Context context, List<IconModel> icons) {
        this.context = context;
        this.icons = icons;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public MaterialCardView itemView;
        public ImageView preview;
        public TextView name;

        public MyViewHolder(LayoutIconItemBinding binding) {
            super(binding.getRoot());
            this.itemView = binding.itemView;
            this.preview = binding.preview;
            this.name = binding.name;
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        // bind to views
        binding = LayoutIconItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        IconModel icon = icons.get(position);
        if(icon == null) {
        	holder.itemView.setVisibility(View.GONE);
        	return;
        }
        
        holder.name.setText(icon.getName());
        holder.preview.setImageDrawable(new PictureDrawable(icon.getPreview()));
        
        holder.itemView.setOnClickListener(view -> {
                if(onItemClickListener != null) {
                	onItemClickListener.onItemClick(view, icon, position);
                }
        });
    }

    @Override
    public int getItemCount() {
        return this.icons.size();
    }
    
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    	this.onItemClickListener = onItemClickListener;
    }

    public void fillDiff(final int extras) {
        final int count = getItemCount();

        // Add the specified number of EMPTY icons
        for (int i = 1; i <= extras; i++) {
            icons.add(null);
        }

        // Calculate the diff
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(
                        new DiffUtil.Callback() {
                            @Override
                            public int getOldListSize() {
                                return count;
                            }

                            @Override
                            public int getNewListSize() {
                                return count + extras;
                            }

                            @Override
                            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                                return newItemPosition < count && oldItemPosition == newItemPosition;
                            }

                            @Override
                            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                                return areItemsTheSame(oldItemPosition, newItemPosition);
                            }
                        });

        // Dispatch the updates to this adapter
        diff.dispatchUpdatesTo(this);
    }
}
