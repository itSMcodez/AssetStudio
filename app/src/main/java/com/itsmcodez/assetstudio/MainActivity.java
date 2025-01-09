package com.itsmcodez.assetstudio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.ConvertUtils;
import com.github.megatronking.svg.generator.svg.Svg2Vector;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itsmcodez.assetstudio.adapters.IconsAdapter;
import com.itsmcodez.assetstudio.callbacks.IconsLoadCallback;
import com.itsmcodez.assetstudio.callbacks.SAFLaunchCallback;
import com.itsmcodez.assetstudio.databinding.ActivityMainBinding;
import com.itsmcodez.assetstudio.databinding.LayoutExportIconBinding;
import com.itsmcodez.assetstudio.models.IconModel;
import com.itsmcodez.assetstudio.viewmodels.IconsViewModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private IconsViewModel iconsViewModel;
    private IconsAdapter iconsAdapter;
    private final int exportToSvgOption = R.id.export_svg_menu_item;
    private final int exportToVectorOption = R.id.export_vector_menu_item;
    private final int REQ_CODE_STORAGE_PERMISSION = 1;
    private ActivityResultLauncher<Uri> SAFLauncher;
    private SAFLaunchCallback SAFLaunchCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        	ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE_STORAGE_PERMISSION);
            return;
        }

        // Register for SAF
        SAFLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Check for the freshest data.
                    final int takeFlags = getIntent().getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    
                    if(SAFLaunchCallback != null) {
                    	SAFLaunchCallback.onLaunchResult(uri);
                    }
                }
        });

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
                iconsAdapter.setOnItemClickListener((view, model, position) -> {
                        launchExportOptions(view, model);
                });
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
                    if(iconsAdapter != null)
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
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_CODE_STORAGE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        	recreate();
        }
    }
    
    private void launchExportOptions(View view, IconModel icon) {
    	PopupMenu menu = new PopupMenu(this, view);
        menu.inflate(R.menu.menu_icon_export_options);
        menu.setOnMenuItemClickListener(item -> {
                int option = item.getItemId();
                exportIcon(option, icon);
                return true;
        });
        menu.show();
    }
    
    private void launchSAF() {
        SAFLauncher.launch(null);
    }
    
    private void exportIcon(int option, IconModel icon) {
        String iconNameConvention = "ic_"+icon.getName().replace('-', '_') ;
        String exportType = option == exportToSvgOption ? "SVG" : "Vector";
        String extension = option == exportToSvgOption ? ".svg" : ".xml";
        
        LayoutExportIconBinding exportIconBinding = LayoutExportIconBinding.inflate(getLayoutInflater());
        exportIconBinding.iconFilenameTextfield.setSuffixText(extension);
        exportIconBinding.iconFilenameTextfield.getEditText().setText(iconNameConvention);
        exportIconBinding.iconDestPathTextfield.getEditText().setFocusable(false);
        exportIconBinding.iconDestPathTextfield.getEditText().setInputType(InputType.TYPE_NULL);
        exportIconBinding.iconDestPathTextfield.setEndIconOnClickListener(view -> {
                launchSAF();
        });
        
        SAFLaunchCallback = new SAFLaunchCallback() {
            private DocumentFile documentFile;
            
            @Override
            public void onLaunchResult(Uri uri) {
                exportIconBinding.iconDestPathTextfield.getEditText().setText(getPathFromUri(uri));
                documentFile = DocumentFile.fromTreeUri(MainActivity.this, uri);
            }
            
            @Override
            public DocumentFile getDocumentFile() {
                return this.documentFile;
            }
        };
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(exportIconBinding.getRoot());
        builder.setIcon(icon.getPreview());
        builder.setTitle(getString(R.string.dialog_title_export_icon, exportType));
        builder.setMessage(getString(R.string.dialog_msg_export_icon, iconNameConvention, exportType));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.dialog_ok_btn_export_icon, null);
        builder.setNegativeButton(R.string.dialog_cancel_btn_export_icon, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                boolean isDestPathTextFieldErrorEnabled = false;
                boolean isFilenameTextFieldErrorEnabled = false;
                
                if(TextUtils.isEmpty(exportIconBinding.iconDestPathTextfield.getEditText().getText())) {
                    if(!isDestPathTextFieldErrorEnabled) {
                        isDestPathTextFieldErrorEnabled = true;
                    	exportIconBinding.iconDestPathTextfield.setError(getString(R.string.dialog_export_icon_dest_path_textfield_error)+"\n"+getString(R.string.helper_export_icon_dest_path_textfield));
                        exportIconBinding.iconDestPathTextfield.setErrorIconDrawable(null);
                    }
                } else if(exportIconBinding.iconDestPathTextfield.isErrorEnabled()) {
                    isDestPathTextFieldErrorEnabled = false;
                    exportIconBinding.iconDestPathTextfield.setErrorEnabled(false);
                }
                
                if(TextUtils.isEmpty(exportIconBinding.iconFilenameTextfield.getEditText().getText())) {
                	if(!isFilenameTextFieldErrorEnabled) {
                        isFilenameTextFieldErrorEnabled = true;
                		exportIconBinding.iconFilenameTextfield.setError(getString(R.string.dialog_export_icon_filename_textfield_error));
                	}
                } else if(exportIconBinding.iconFilenameTextfield.isErrorEnabled()) {
                    isFilenameTextFieldErrorEnabled = false;
                	exportIconBinding.iconFilenameTextfield.setErrorEnabled(false);
                }
                
                // check for text field errors
                if(isDestPathTextFieldErrorEnabled || isFilenameTextFieldErrorEnabled) {
                	return;
                }
                
                if(SAFLaunchCallback != null) {
                	// SVG
                    if(option == exportToSvgOption) {
                        exportToSvg(exportIconBinding.iconFilenameTextfield.getEditText().getText().toString(), SAFLaunchCallback.getDocumentFile(), icon);
                    }
                    // Vector
                    if(option == exportToVectorOption) {
                        exportToVector(exportIconBinding.iconFilenameTextfield.getEditText().getText().toString(), SAFLaunchCallback.getDocumentFile(), icon);
                    }
                }
                
                dialog.dismiss();
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> {
                dialog.dismiss();
        });
    }

    private String getPathFromUri(Uri uri) {
        String docId = DocumentsContract.getTreeDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        String path = split.length > 1 ? split[1] : "";

        if ("primary".equalsIgnoreCase(type)) {
            return "/storage/emulated/0/" + path;
        }
        // Handle non-primary volumes
        return "storage/" + type + "/" + path;
    }
    
    private void exportToSvg(String filename, DocumentFile documentFile, IconModel icon) {
    	if(documentFile == null) {
    		return;
    	}
        try {
        	String destination = getPathFromUri(documentFile.getUri());
            Path destinationPath = Paths.get(destination+"/"+filename.replace('-', '_')+".svg");
            Files.copy(getAssets().open(icon.getAssetPath()), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            if(Files.exists(destinationPath)) {
                Toast.makeText(this, "file saved: "+destinationPath.toString(), Toast.LENGTH_LONG).show();
            }
        } catch(IOException err) {
        	err.printStackTrace();
            Toast.makeText(this, "An error occurred while saving the file!", Toast.LENGTH_LONG).show();
        }
    }
    
    private void exportToVector(String filename, DocumentFile documentFile, IconModel icon) {
    	if(documentFile == null) {
    		return;
    	}
        String destination = getPathFromUri(documentFile.getUri());
        Path destinationPath = Paths.get(destination+"/"+filename.replace('-', '_')+".xml");
        try {
            var fis = getAssets().open(icon.getAssetPath());
            
            String data = ConvertUtils.inputStream2String(fis, "utf-8");
            String svgData = data.replaceAll("currentColor", "#000000");
            
            Files.write(destinationPath, svgData.getBytes(), StandardOpenOption.CREATE);
            Svg2Vector.parseSvgToXml(destinationPath.toFile(), destinationPath.toFile(), -1, -1);
            
            if(Files.exists(destinationPath)) {
                Toast.makeText(this, "file saved: "+destinationPath.toString(), Toast.LENGTH_LONG).show();
            }
        } catch(IOException err) {
        	err.printStackTrace();
            Toast.makeText(this, "An error occurred while saving the file!", Toast.LENGTH_LONG).show();
        }
    }
    
}
