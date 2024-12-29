package com.itsmcodez.assetstudio.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.google.android.material.card.MaterialCardView;
import com.itsmcodez.assetstudio.R;
import com.itsmcodez.assetstudio.databinding.LayoutIconItemBinding;
import com.itsmcodez.assetstudio.models.IconModel;
import java.io.IOException;
import java.util.ArrayList;

public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.IconsViewHolder> {
    private LayoutIconItemBinding binding;
    private Context context;
    private ArrayList<IconModel> icons;

    public IconsAdapter(Context context, ArrayList<IconModel> icons) {
        this.context = context;
        this.icons = icons;
    }

    public static class IconsViewHolder extends RecyclerView.ViewHolder {
        public MaterialCardView itemView;
        public TextView name;
        public ImageView preview;

        public IconsViewHolder(LayoutIconItemBinding binding) {
            super(binding.getRoot());
            itemView = binding.itemView;
            name = binding.name;
            preview = binding.preview;
        }
    }

    @Override
    public IconsViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        // bind to views
        binding = LayoutIconItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new IconsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(IconsViewHolder holder, int position) {
        IconModel icon = icons.get(position);
        holder.name.setText(icon.getName());
        holder.preview.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null);
        try {
        	Glide.with(context)
            .load(SVG.getFromAsset(context.getAssets(), icon.getAssetPath()))
            .placeholder(context.getDrawable(R.drawable.ic_loading))
            .into(holder.preview);
        } catch(SVGParseException err) {
        	err.printStackTrace();
        } catch(IOException err) {
            err.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }
}
