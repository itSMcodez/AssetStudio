package com.itsmcodez.assetstudio.listeners;
import android.view.View;
import androidx.annotation.NonNull;
import com.itsmcodez.assetstudio.models.base.Model;

@FunctionalInterface
public interface OnItemClickListener {
    void onItemClick(@NonNull View view, @NonNull Model model, @NonNull int position);
}
