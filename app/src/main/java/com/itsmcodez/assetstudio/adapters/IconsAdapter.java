package com.itsmcodez.assetstudio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.card.MaterialCardView;
import com.itsmcodez.assetstudio.R;
import com.itsmcodez.assetstudio.callbacks.IconsLoadCallback;
import com.itsmcodez.assetstudio.databinding.LayoutIconItemBinding;
import com.itsmcodez.assetstudio.models.IconModel;
import java.util.ArrayList;

public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.IconsViewHolder> 
implements Filterable {
    private LayoutIconItemBinding binding;
    private Context context;
    private ArrayList<IconModel> icons;
    private ArrayList<IconModel> iconsCopy;
    private IconsLoadCallback iconsLoadCallback;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public IconsAdapter(Context context, ArrayList<IconModel> icons) {
        this.context = context;
        this.icons = icons;
        iconsCopy = new ArrayList<>(icons);
        mSelection = new MultiSelection(new ArrayList<IconModel>());
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
        
        Glide.with(context)
            .load(icon.getSvg())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .signature(new ObjectKey(icon.getSvg().hashCode()))
            .placeholder(context.getDrawable(R.drawable.ic_loading))
            .into(holder.preview);
        
        // check selection at this position
        holder.itemView.setChecked(icon.getSelected() && inSelectMode);
        
        holder.itemView.setOnClickListener(view -> {
                if(onItemClickListener != null) {
                	onItemClickListener.onItemClick(view, icon, position);
                }
        });
        
        holder.itemView.setOnLongClickListener(view -> {
                if(onItemLongClickListener != null) {
                	onItemLongClickListener.onItemLongClick(view, icon, position);
                }
                return true;
        });
        
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }
    
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    	this.onItemClickListener = onItemClickListener;
    }
    
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
    	this.onItemLongClickListener = onItemLongClickListener;
    }
    
    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(View itemView, IconModel model, int position);
    }
    
    @FunctionalInterface
    public interface OnItemLongClickListener {
        boolean onItemLongClick(View itemView, IconModel model, int position);
    }
    
    /* Multiple selections */
    private MultiSelection mSelection;
    private boolean inSelectMode;
    
    public static class MultiSelection {
        private ArrayList<IconModel> selectionList;
        private boolean inSelectMode;
        
        public interface Selectable {
            void setSelected(boolean selected);
            boolean getSelected();
        }
        
        public MultiSelection(ArrayList<IconModel> selectionList) {
        	this.selectionList = selectionList;
        }
        
        public void setInSelectMode(boolean inSelectMode) {
        	this.inSelectMode = inSelectMode;
        }
        
        public boolean getInSelectMode() {
        	return this.inSelectMode;
        }
        
        public void add(IconModel icon) {
        	if(inSelectMode) {
        		if(icon != null && !icon.getSelected() && !selectionList.contains(icon)) {
        			icon.setSelected(true);
                    selectionList.add(icon);
        		}
        	}
        }
        
        public void remove(IconModel icon) {
        	if(inSelectMode) {
        		if(icon != null && selectionList.contains(icon)) {
                    if(icon.getSelected()) {
                    	icon.setSelected(false);
                    }
        			selectionList.remove(icon);
        		}
        	}
        }
        
        public boolean isSelected(IconModel icon) {
        	return icon.getSelected() && selectionList.contains(icon);
        }
        
        public ArrayList<IconModel> getSelectionList() {
        	return this.selectionList;
        }
        
        public void clearSelection() {
            if(inSelectMode) {
                for(IconModel icon : selectionList) {
                	if(icon != null) {
                        if(icon.getSelected()) {
                            icon.setSelected(false);
                        }
                    }
                }
                selectionList.clear();
        	}
        }
    }
    
    public void setInSelectMode(boolean inSelectMode) {
        this.inSelectMode = inSelectMode;
        if(mSelection != null) {
        	mSelection.setInSelectMode(inSelectMode);
        }
    }
    
    public boolean getInSelectMode() {
        return this.inSelectMode && (mSelection != null && mSelection.getInSelectMode());
    }
    
    public void addToSelection(int position) {
    	if(inSelectMode && mSelection != null) {
    		mSelection.add(icons.get(position));
            notifyItemChanged(position);
    	}
    }
    
    public void removeIfSelected(int position) {
    	if(inSelectMode && mSelection != null) {
    		mSelection.remove(icons.get(position));
            notifyItemChanged(position);
    	}
    }
    
    public boolean isSelected(int position) {
    	return mSelection != null && mSelection.isSelected(icons.get(position));
    }
    
    public ArrayList<IconModel> getSelectionList() {
    	return mSelection.getSelectionList();
    }
    
    public void clearSelection() {
    	mSelection.clearSelection();
        notifyDataSetChanged();
    }
    
    /* Filter icons*/
    public void filter(CharSequence constraint, IconsLoadCallback iconsLoadCallback) {
    	this.iconsLoadCallback = iconsLoadCallback;
        getFilter().filter(constraint);
    }
    
    @Override
    public Filter getFilter() {
        return iconsFilter;
    }
    
    @SuppressWarnings("unchecked")
    private Filter iconsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<IconModel> filteredIcons = new ArrayList<>();
            
            if(constraint != null && constraint.length() != 0) {
                
                if(iconsLoadCallback != null) {
                	iconsLoadCallback.onLoadStart();
                }
                
            	for(IconModel icon : iconsCopy) {
            		if(icon.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
            			filteredIcons.add(icon);
            		}
            	}
                
            } else {
                filteredIcons.addAll(iconsCopy);
            }
            FilterResults results = new FilterResults();
            results.values = filteredIcons;
            return results;
        }
        
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // reset and publish filtered list
            icons = (ArrayList<IconModel>) results.values;
            notifyDataSetChanged();
            if(iconsLoadCallback != null) {
            	iconsLoadCallback.onLoadSuccess(icons);
            }
        }
    };
}
