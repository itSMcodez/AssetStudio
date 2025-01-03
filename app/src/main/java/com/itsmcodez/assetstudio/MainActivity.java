package com.itsmcodez.assetstudio;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.itsmcodez.assetstudio.adapters.IconsAdapter;
import com.itsmcodez.assetstudio.callbacks.IconsLoadCallback;
import com.itsmcodez.assetstudio.databinding.ActivityMainBinding;
import com.itsmcodez.assetstudio.models.IconModel;
import com.itsmcodez.assetstudio.viewmodels.IconsViewModel;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private IconsViewModel iconsViewModel;
    private IconsAdapter iconsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        
        // ViewModel and Recyclerview
        iconsViewModel = new ViewModelProvider(this).get(IconsViewModel.class);
        iconsViewModel.getIconsLiveData(new IconsLoadCallback() {
                @Override
                public void onLoadStart() {
                    new Handler(getMainLooper()).post(() -> {
                            binding.progressIndicator.setVisibility(View.VISIBLE);
                    });
                }
                
                @Override
                public void onLoadSuccess(ArrayList<IconModel> icons) {
                    new Handler(getMainLooper()).post(() -> {
                            binding.progressIndicator.setVisibility(View.GONE);
                            binding.searchField.setHint(getString(R.string.hint_search_for_icons, icons.size()));
                    });
                }
                
                @Override
                public void onLoadFailed() {
                    new Handler(getMainLooper()).post(() -> {
                            binding.progressIndicator.setVisibility(View.GONE);
                            binding.searchField.setHint("Load failed...");
                    });
                }
        })
        .observe(this, icons -> {
                FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(MainActivity.this, FlexDirection.ROW);
                flexboxLayoutManager.setJustifyContent(JustifyContent.SPACE_EVENLY);
                iconsAdapter = new IconsAdapter(MainActivity.this, icons);
                binding.iconsRecyclerView.setLayoutManager(flexboxLayoutManager);
                binding.iconsRecyclerView.setHasFixedSize(true);
                binding.iconsRecyclerView.setNestedScrollingEnabled(false);
                binding.iconsRecyclerView.setAdapter(iconsAdapter);
        });
        
        // Filter icons
        binding.searchField.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    // TODO: Implement this method
                }
                
                @Override
                public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                    iconsAdapter.filter(s, new IconsLoadCallback() {
                            @Override
                            public void onLoadStart() {
                                new Handler(getMainLooper()).post(() -> {
                                        binding.progressIndicator.setVisibility(View.VISIBLE);
                                });
                            }
                            
                            @Override
                            public void onLoadSuccess(ArrayList<IconModel> icons) {
                                new Handler(getMainLooper()).post(() -> {
                                        binding.progressIndicator.setVisibility(View.GONE);
                                });
                            }
                            
                            @Override
                            public void onLoadFailed() {
                                // TODO: Implement this method
                            }
                    });
                }
                
                @Override
                public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                    // TODO: Implement this method
                }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
