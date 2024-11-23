
package com.itsmcodez.assetstudio;

import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.itsmcodez.assetstudio.adapters.IconsAdapter;
import com.itsmcodez.assetstudio.databinding.ActivityMainBinding;
import com.itsmcodez.assetstudio.markers.IconPacks;
import com.itsmcodez.assetstudio.models.IconModel;
import com.itsmcodez.assetstudio.utils.FlexboxUtils;
import com.itsmcodez.assetstudio.viewmodels.IconsViewModel;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private IconsViewModel iconsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        
        /* ViewModel */
        iconsViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(IconsViewModel.initializer))
        .get(IconsViewModel.class);
        
        iconsViewModel.getIconsLiveData().observe(this, icons -> {
                
                // set searchField hint
                binding.searchField.setHint(getString(R.string.hint_search_icon, icons.size()));
                
                /* RecyclerView and Adapter */
                IconsAdapter adapter = new IconsAdapter(this, icons);
                adapter.setOnItemClickListener((view, model, position) -> {
                        // get target icon
                        IconModel icon = (IconModel) model;
                        
                });
                
                FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this, FlexDirection.ROW);
                flexboxLayoutManager.setJustifyContent(JustifyContent.SPACE_EVENLY);
                binding.iconsRecyclerView.setLayoutManager(flexboxLayoutManager);
                var onGlobalLayoutListener = FlexboxUtils.createGlobalLayoutListenerToDistributeFlexboxItemsEvenly(
                    () -> adapter,
                    () -> flexboxLayoutManager,
                    (_adapter, diff) -> { _adapter.fillDiff(diff); }
                );
                binding.iconsRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
                binding.iconsRecyclerView.setAdapter(adapter);
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
