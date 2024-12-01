
package com.itsmcodez.assetstudio;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.PopupMenu;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itsmcodez.assetstudio.adapters.IconsAdapter;
import com.itsmcodez.assetstudio.databinding.ActivityMainBinding;
import com.itsmcodez.assetstudio.databinding.LayoutExportIconEditorBinding;
import com.itsmcodez.assetstudio.listeners.PathChooserListener;
import com.itsmcodez.assetstudio.markers.ExportType;
import com.itsmcodez.assetstudio.markers.IconPacks;
import com.itsmcodez.assetstudio.models.IconModel;
import com.itsmcodez.assetstudio.preferences.ExportPathPreference;
import com.itsmcodez.assetstudio.utils.PathUtils;
import com.itsmcodez.assetstudio.utils.FlexboxUtils;
import com.itsmcodez.assetstudio.utils.IconUtils;
import com.itsmcodez.assetstudio.viewmodels.IconsViewModel;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private IconsViewModel iconsViewModel;
    private ActivityResultLauncher<Intent> pathChooserCallback;
    private PathChooserListener pathChooserListener;
    private final int STORAGE_PERMISSION_REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        
        // Check for permission
        requestStoragePermission();
        
        /* Path chooser */
        pathChooserCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent path = result.getData();
                        if(pathChooserListener != null) {
                            pathChooserListener.onPathChosen(path);
                        }
                    } else if(pathChooserListener != null) pathChooserListener.onCancelled();
                }
        });
        
        /* ViewModel */
        iconsViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(IconsViewModel.initializer))
        .get(IconsViewModel.class);
        
        iconsViewModel.getIconsLiveData().observe(this, icons -> {
                
                // set searchField hint and hide progress
                binding.searchField.setHint(getString(R.string.hint_search_icon, icons.size()));
                binding.progressIndicator.setVisibility(View.GONE);
                
                /* RecyclerView and Adapter */
                IconsAdapter adapter = new IconsAdapter(this, icons);
                adapter.setOnItemClickListener((view, model, position) -> {
                        // get target icon
                        IconModel icon = (IconModel) model;
                        launchIconOptions(view, icon);
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
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == STORAGE_PERMISSION_REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        	recreate();
        } else requestStoragePermission();
    }
    
    private void launchIconOptions(View target, IconModel icon) {
    	PopupMenu menu = new PopupMenu(this, target);
        menu.inflate(R.menu.menu_icon_options);
        menu.setOnMenuItemClickListener((menuItem) -> {
                int option = menuItem.getItemId();
                int exportPNG = R.id.export_png_menu_item;
                int exportSVG = R.id.export_svg_menu_item;
                int exportVector = R.id.export_vector_menu_item;
                
                if(option == exportPNG) {
                    createExportDialog(ExportType.PNG, icon);
                	return true;
                }
                
                if(option == exportSVG) {
                    createExportDialog(ExportType.SVG, icon);
                	return true;
                }
                
                if(option == exportVector) {
                    createExportDialog(ExportType.VECTOR, icon);
                	return true;
                }
                
                return false;
        });
        menu.show();
    }
    
    private void createExportDialog(ExportType type, IconModel icon) {
        ExportPathPreference prevDestPath = ExportPathPreference.with(MainActivity.this);
        String iconName = icon.getName(); // used to reset icon name
        
        /* Export Icon Editor Layout */
        LayoutExportIconEditorBinding editorView = LayoutExportIconEditorBinding.inflate(getLayoutInflater());
        editorView.nameField.setText("ic_" + icon.getName().replace('-', '_'));
        icon.setName(editorView.nameField.getText().toString());
        editorView.nameField.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable t) {
                    icon.setName(t.toString());
                }
                
                @Override
                public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                    // TODO: Implement this method
                }
                
                @Override
                public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                    // TODO: Implement this method
                }
        });
        editorView.nameFieldHolder.setSuffixText(type == ExportType.PNG ? ".png" : type == ExportType.SVG ? ".svg" : ".xml");
        
        // make destPathField non-editable
        editorView.destPathField.setInputType(InputType.TYPE_NULL);
        editorView.destPathField.setFocusable(false);
        editorView.destPathField.setKeyListener(null);
        
        // retrieve previously chosen directory
        if(!(prevDestPath.getValue(ExportPathPreference.KEY_ICON_EXPORT_PATH).isEmpty())) {
            Uri previousDirUri = Uri.parse(prevDestPath.getValue(ExportPathPreference.KEY_ICON_EXPORT_PATH));
        	editorView.destPathField.setText(PathUtils.getFullPathFromTreeUri(previousDirUri));
        }
        
        editorView.destPathField.setOnClickListener(view -> startPathChooser());
        
        pathChooserListener = new PathChooserListener() {
            private Uri destPathUri;
            
            @Override
            public boolean onPathChosen(Intent data) {
                
                if(data != null) {
                    Uri pathUri = data.getData();
                    
                    if (pathUri != null) {
                        destPathUri = pathUri;
                        
                        // Persist permissions
                        getContentResolver().takePersistableUriPermission(
                            pathUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        );
                        
                        // display raw path from uri
                        editorView.destPathField.setText(PathUtils.getFullPathFromTreeUri(pathUri));
                        
                        // Update path preference 
                        prevDestPath.setValue(ExportPathPreference.KEY_ICON_EXPORT_PATH, pathUri.toString());
                    }
                    return true;
                }
                
                return false;
            }
            
            @Override
            public void onCancelled() {
                Toast.makeText(MainActivity.this, "No path selected!", Toast.LENGTH_LONG).show();
            }
            
            @Override
            public Uri getDestPathUri() {
                if(destPathUri == null) {
                    if(!(prevDestPath.getValue(ExportPathPreference.KEY_ICON_EXPORT_PATH).isEmpty())) {
                        destPathUri = Uri.parse(prevDestPath.getValue(ExportPathPreference.KEY_ICON_EXPORT_PATH));
                    }
                }
                return this.destPathUri;
            }
            
        };
        
        /* Dialog */
    	MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle(icon.getName());
        dialog.setMessage("Export to ".concat(type == ExportType.PNG ? "PNG" : type == ExportType.SVG ? "SVG" : "Vector"));
        dialog.setView(editorView.getRoot());
        dialog.setCancelable(false);
        dialog.setIcon(new PictureDrawable(icon.getPreview()));
        dialog.setPositiveButton("Export", (_dialog, which) -> {
                
                if(pathChooserListener == null && prevDestPath.getValue(ExportPathPreference.KEY_ICON_EXPORT_PATH).isEmpty()) {
                    Toast.makeText(this, "Please choose a destination folder!", Toast.LENGTH_LONG).show();
                	return;
                }
                
                DocumentFile destFolder = pathChooserListener.getDestPathUri() != null ? DocumentFile.fromTreeUri(this, pathChooserListener.getDestPathUri()) : null;
                switch(type) {
                    case PNG: IconUtils.exportIcon2PNG(icon, destFolder, this);
                        break;
                    case SVG: IconUtils.exportIcon2SVG(icon, destFolder, this);
                        break;
                    case VECTOR: IconUtils.exportIcon2Vector(icon, destFolder, this);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid ExportType!");
                }
        });
        dialog.setNegativeButton("Cancel", (_dialog, which) -> {
                _dialog.dismiss();
        });
        dialog.setOnDismissListener((_dialog) -> {
                // reset icon name
            icon.setName(iconName);
        });
        dialog.show();
    }
    
    private void startPathChooser() {
        Intent pathChooser = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        pathChooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        pathChooserCallback.launch(pathChooser);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQ_CODE);
                return;
            }
        }
    }
}
