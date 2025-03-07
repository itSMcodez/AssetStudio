package com.itsmcodez.assetstudio;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blankj.utilcode.util.ConvertUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.github.megatronking.svg.generator.svg.Svg2Vector;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.itsmcodez.assetstudio.adapters.IconsAdapter;
import com.itsmcodez.assetstudio.adapters.ListItemAdapter;
import com.itsmcodez.assetstudio.callbacks.IconsLoadCallback;
import com.itsmcodez.assetstudio.callbacks.SAFLaunchCallback;
import com.itsmcodez.assetstudio.common.IconPack;
import com.itsmcodez.assetstudio.databinding.ActivityMainBinding;
import com.itsmcodez.assetstudio.databinding.LayoutExportIconBinding;
import com.itsmcodez.assetstudio.models.IconModel;
import com.itsmcodez.assetstudio.options.IconOptions;
import com.itsmcodez.assetstudio.preferences.SAFPreference;
import com.itsmcodez.assetstudio.preferences.fragment.SettingsFragment;
import com.itsmcodez.assetstudio.util.PathUtil;
import com.itsmcodez.assetstudio.viewmodels.IconOptionsViewModel;
import com.itsmcodez.assetstudio.viewmodels.IconsViewModel;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import java.io.IOException;
import java.io.InputStream;
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
    private SAFPreference SAFPref;
    private SharedPreferences APP_PREFERENCES;
    private ActivityResultLauncher<Uri> SAFLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncherApi26ToApi29;
    private ActivityResultLauncher<Intent> storagePermissionLauncherApi30Plus;
    private SAFLaunchCallback SAFLaunchCallback;
    private boolean applyIconOptions = false;
    private IconOptionsViewModel iconOptionsVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        
        storagePermissionLauncherApi26ToApi29 = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(isGranted) {
                    init();
                } else {
                    Toast.makeText(getApplicationContext(), "Please allow storage permission for the app to function!", Toast.LENGTH_LONG).show();
                }
        });
        
        storagePermissionLauncherApi30Plus = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Environment.isExternalStorageManager()) {
                    init();
                } else {
                    Toast.makeText(getApplicationContext(), "Please allow storage permission for the app to function!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    storagePermissionLauncherApi30Plus.launch(intent);
                }
        });
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            
        	if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                init();
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getApplicationContext(), "Please allow storage permission for the app to function!", Toast.LENGTH_LONG).show();
                storagePermissionLauncherApi26ToApi29.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                storagePermissionLauncherApi26ToApi29.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                init();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                storagePermissionLauncherApi30Plus.launch(intent);
            }
        }
        
        
        // Register for SAF
        SAFLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if(uri != null) {
                        // Check for the freshest data.
                        final int takeFlags = getIntent().getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        
                        if(SAFLaunchCallback != null) {
                            SAFLaunchCallback.onLaunchResult(uri);
                        }
                    } else Toast.makeText(MainActivity.this, "Path selection canceled!", Toast.LENGTH_SHORT).show();
                }
        });
    }
    
    private void init() {
        
        // init Preferences
        SAFPref = SAFPreference.with(this);
        APP_PREFERENCES = PreferenceManager.getDefaultSharedPreferences(this);
        
        // init icon options viewmodel
        iconOptionsVM = new ViewModelProvider(this).get(IconOptions.class);
        
        // init multi-selection
        ArrayList<String> selectionList = new ArrayList<>();
        binding.selectionListSheet.setVisibility(View.GONE);
        BottomSheetBehavior<ConstraintLayout> selectionSheet = BottomSheetBehavior.from(binding.selectionListSheet);
        selectionSheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(View bottomSheet, int newState) {
                    if(newState == BottomSheetBehavior.STATE_EXPANDED) {
                        if(iconsAdapter != null) {
                            if(!selectionList.isEmpty()) {
                            	selectionList.clear();
                            }
                        	for(IconModel icon : iconsAdapter.getSelectionList()) {
                                selectionList.add(icon.getName());
                            }
                            ListItemAdapter adapter = new ListItemAdapter(MainActivity.this, selectionList);
                            adapter.setOnItemClickListener((view, name, position) -> {
                                    if(binding.iconsRecyclerView.getAdapter() != null) {
                                    	binding.iconsRecyclerView.scrollToPosition(iconsAdapter.indexOf(name));
                                    }
                            });
                            binding.selectionRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            binding.selectionRecyclerView.setAdapter(adapter);
                            binding.nSelectionText.setText(String.valueOf(selectionList.size()) + " selected");
                        }
                    }
                    if(newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        binding.selectionRecyclerView.setAdapter(null);
                    	binding.selectionRecyclerView.setLayoutManager(null);
                    }
                }
                
                @Override
                public void onSlide(View bottomSheet, float slideOffset) {
                    // rotation animation
                    binding.slider.setRotation(slideOffset * 180);
                }
        });
        binding.sheetTopBar.setOnClickListener(view -> {
                if(selectionSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    selectionSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                if(selectionSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    selectionSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
        });
        binding.cancelSelectionBt.setOnClickListener(view -> {
                if(iconsAdapter != null && iconsAdapter.getInSelectMode()) {
                	iconsAdapter.clearSelection();
                    iconsAdapter.setInSelectMode(false);
                    selectionList.clear();
                    binding.selectionListSheet.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Selection reset!", Toast.LENGTH_SHORT).show();
                }
        });
        binding.exportSelectionBt.setOnClickListener(view -> {
                if(iconsAdapter != null && iconsAdapter.getInSelectMode()) {
                	launchBatchExportDialog(iconsAdapter.getSelectionList(), binding.exportSelectionBt);
                }
        });

        // ViewModel and Recyclerview
        iconsViewModel = new ViewModelProvider(this).get(IconsViewModel.class);
        iconsViewModel.getIconsLiveData(getDefaultPack(), new IconsLoadCallback() {
                @Override
                public void onLoadStart() {
                    new Handler(getMainLooper()).post(() -> {
                            binding.progressIndicator.setVisibility(View.VISIBLE);
                            binding.searchField.setHint("Loading...");
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
                        // whether multi-selection is enabled
                        if(iconsAdapter.getInSelectMode()) {
                        	if(iconsAdapter.isSelected(position)) {
                                iconsAdapter.removeIfSelected(position);
                                // Update selectionList
                                if(binding.selectionListSheet.getVisibility() == View.VISIBLE) {
                                    final int target_index = selectionList.indexOf(model.getName());
                                	selectionList.remove(model.getName());
                                    if(binding.selectionRecyclerView.getAdapter() != null) binding.selectionRecyclerView.getAdapter().notifyItemRemoved(target_index);
                                    binding.nSelectionText.setText(String.valueOf(selectionList.size()) + " selected");
                                }
                                if(iconsAdapter.getSelectionList().size() == 0) {
                                    iconsAdapter.setInSelectMode(false);
                                    binding.selectionListSheet.setVisibility(View.GONE);
                                }
                            } else {
                                iconsAdapter.addToSelection(position);
                                // Update selectionList
                                if(binding.selectionListSheet.getVisibility() == View.VISIBLE) {
                                    if(!selectionList.contains(model.getName())) {
                                        selectionList.add(model.getName());
                                        if(binding.selectionRecyclerView.getAdapter() != null) binding.selectionRecyclerView.getAdapter().notifyItemInserted(iconsAdapter.getSelectionList().size()-1);
                                        binding.nSelectionText.setText(String.valueOf(selectionList.size()) + " selected");
                                    }
                                }
                            }
                            return;
                        }
                        launchExportOptions(view, model);
                });
                iconsAdapter.setOnItemLongClickListener((view, model, position) -> {
                        // 1. enable multi-selection if not enabled
                        if(!iconsAdapter.getInSelectMode()) {
                        	iconsAdapter.setInSelectMode(true);
                            binding.selectionListSheet.setVisibility(View.VISIBLE);
                        }
                        // 2. remove selection at position if selected
                        if(iconsAdapter.isSelected(position)) {
                        	iconsAdapter.removeIfSelected(position);
                            // Update selectionList
                            if(binding.selectionListSheet.getVisibility() == View.VISIBLE) {
                                final int target_index = selectionList.indexOf(model.getName());
                                selectionList.remove(model.getName());
                                if(binding.selectionRecyclerView.getAdapter() != null) binding.selectionRecyclerView.getAdapter().notifyItemRemoved(target_index);
                                binding.nSelectionText.setText(String.valueOf(selectionList.size()) + " selected");
                            }
                            // 3. disable multi-selection if selection list is empty
                            if(iconsAdapter.getSelectionList().size() == 0) {
                            	iconsAdapter.setInSelectMode(false);
                                binding.selectionListSheet.setVisibility(View.GONE);
                            }
                        } else {
                            // 4. add position to selection list if not already selected
                            iconsAdapter.addToSelection(position);
                            // Update selectionList
                            if(binding.selectionListSheet.getVisibility() == View.VISIBLE) {
                                if(!selectionList.contains(model.getName())) {
                                    selectionList.add(model.getName());
                                    if(binding.selectionRecyclerView.getAdapter() != null) binding.selectionRecyclerView.getAdapter().notifyItemInserted(iconsAdapter.getSelectionList().size()-1);
                                    binding.nSelectionText.setText(String.valueOf(selectionList.size()) + " selected");
                                }
                            }
                        }
                        return true;
                });
                binding.iconsRecyclerView.setLayoutManager(flexboxLayoutManager);
                binding.iconsRecyclerView.setHasFixedSize(true);
                binding.iconsRecyclerView.setAdapter(iconsAdapter);
        });
        
        /* Settings */
        binding.toolbar.setOnMenuItemClickListener(menuItem -> {
                if(menuItem.getItemId() == R.id.settings_menu_item) {
                    startActivity(new Intent(this, SettingsActivity.class));
                	return true;
                }
                return false;
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
    
    public static final String ACTION_UPDATE_ICONS  = "ACTION_UPDATE_ICONS";
    private static Intent updateIntent = new Intent();
    
    public static Intent getUpdateIntent() {
    	return updateIntent;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if(getUpdateIntent() != null && getUpdateIntent().getAction() != null) {
        	if(getUpdateIntent().getAction().equals(ACTION_UPDATE_ICONS)) {
                if(iconsViewModel != null) {
                    binding.searchField.setHint("Updating icons...");
                	iconsViewModel.refreshIcons(getDefaultPack());
                }
                getUpdateIntent().setAction(null);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
    
    private IconPack getDefaultPack() {
        if(APP_PREFERENCES != null) {
        	String pack = APP_PREFERENCES.getString(SettingsFragment.KEY_ICON_PACK, SettingsFragment.ICON_PACK_FEATHER);
            return pack.equals(SettingsFragment.ICON_PACK_FEATHER) ? IconPack.FEATHER : pack.equals(SettingsFragment.ICON_PACK_LUCIDE) ? IconPack.LUCIDE : IconPack.TABLER;
        }
        return null;
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
    
    private void launchBatchExportDialog(ArrayList<IconModel> selection, View view) {
    	PopupMenu menu = new PopupMenu(this, view);
        menu.inflate(R.menu.menu_icon_export_options);
        menu.setOnMenuItemClickListener(item -> {
                int option = item.getItemId();
                batchExport(option, selection);
                return true;
        });
        menu.show();
        
    }
    
    private void batchExport(int option, ArrayList<IconModel> selection) {
        if(selection == null) {
        	return;
        }
        String exportType = option == exportToSvgOption ? "SVG" : "Vector";
        String extension = option == exportToSvgOption ? ".svg" : ".xml";
        
        LayoutExportIconBinding exportIconBinding = LayoutExportIconBinding.inflate(getLayoutInflater());
        exportIconBinding.iconFilenameTextfield.setSuffixText(extension);
        exportIconBinding.iconFilenameTextfield.getEditText().setText("Exporting " + String.valueOf(selection.size()) + " icons");
        exportIconBinding.iconFilenameTextfield.getEditText().setEnabled(false);
        exportIconBinding.iconDestPathTextfield.getEditText().setFocusable(false);
        exportIconBinding.iconDestPathTextfield.getEditText().setInputType(InputType.TYPE_NULL);
        exportIconBinding.iconDestPathTextfield.setEndIconOnClickListener(view -> {
                launchSAF();
        });
        
        // Disable tinting option if export type is svg
        if(option == exportToSvgOption) exportIconBinding.checkboxTint.setVisibility(View.GONE);
        
        // Disable editting options for batch export
        exportIconBinding.checkboxAdvancedOptions.setVisibility(View.GONE);
        
        SAFLaunchCallback = new SAFLaunchCallback() {
            private DocumentFile documentFile;
            
            @Override
            public void onLaunchResult(Uri uri) {
                exportIconBinding.iconDestPathTextfield.getEditText().setText(PathUtil.getPathFromUri(uri));
                documentFile = DocumentFile.fromTreeUri(MainActivity.this, uri);
                if(SAFPref != null) SAFPref.putPreference(SAFPreference.KEY, uri.toString());
            }
            
            @Override
            public DocumentFile getDocumentFile() {
                return this.documentFile;
            }
        };
        
        // Retrieve previously chosen file destination if exists
        if(SAFPref != null) {
            if(!((String)SAFPref.getPreference(SAFPreference.KEY)).isEmpty()) {
            	SAFLaunchCallback.onLaunchResult(Uri.parse((String)SAFPref.getPreference(SAFPreference.KEY)));
            }
        }
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(exportIconBinding.getRoot());
        builder.setTitle(getString(R.string.dialog_title_export_icon, exportType));
        builder.setMessage(getString(R.string.dialog_msg_export_icon, String.valueOf(selection.size()) + " icons", exportType));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.dialog_ok_btn_export_icon, null);
        builder.setNegativeButton(R.string.dialog_cancel_btn_export_icon, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                boolean isDestPathTextFieldErrorEnabled = false;
                
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
                
                // check for text field errors
                if(isDestPathTextFieldErrorEnabled) {
                	return;
                }
                
                if(SAFLaunchCallback != null) {
                    for(IconModel icon : selection) {
                    	// SVG
                        if(option == exportToSvgOption) {
                            exportToSvg(icon.getName(), SAFLaunchCallback.getDocumentFile(), icon);
                        }
                        // Vector
                        if(option == exportToVectorOption) {
                            exportToVector(icon.getName(), SAFLaunchCallback.getDocumentFile(), icon, exportIconBinding.checkboxTint.isChecked());
                        }
                    }
                    Toast.makeText(getApplicationContext(), "Successful exported ".concat(String.valueOf(selection.size()) + " icons"), Toast.LENGTH_LONG).show();
                    if(iconsAdapter != null) {
                    	iconsAdapter.clearSelection();
                        iconsAdapter.setInSelectMode(false);
                    }
                    binding.selectionListSheet.setVisibility(View.GONE);
                }
                
                dialog.dismiss();
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> {
                dialog.dismiss();
        });
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
        ((MaterialAutoCompleteTextView)exportIconBinding.optionStrokeWidth.getEditText()).setSimpleItems(new String[] {"1", "2", "3", "4", "5"});
        ((MaterialAutoCompleteTextView)exportIconBinding.optionStrokeLinecap.getEditText()).setSimpleItems(new String[] {"round", "butt", "square"});
        ((MaterialAutoCompleteTextView)exportIconBinding.optionStrokeLinejoin.getEditText()).setSimpleItems(new String[] {"round", "arcs", "bevel", "miter", "miter-clip"});
        
        
        // Disable tinting option if export type is svg
        if(option == exportToSvgOption) exportIconBinding.checkboxTint.setVisibility(View.GONE);
        
        // Icon export options (Editing Options)
        exportIconBinding.checkboxAdvancedOptions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton checkbox, boolean isChecked) {
                    applyIconOptions = isChecked;
                    if(isChecked) {
                    	exportIconBinding.advancedOptions.setVisibility(View.VISIBLE);
                        exportIconBinding.checkboxTint.setChecked(false);
                    } else {
                        exportIconBinding.advancedOptions.setVisibility(View.GONE);
                        icon.setOptions(null);
                        loadIconPreview(exportIconBinding.preview, icon);
                    }
                }
        });
        
        SAFLaunchCallback = new SAFLaunchCallback() {
            private DocumentFile documentFile;
            
            @Override
            public void onLaunchResult(Uri uri) {
                exportIconBinding.iconDestPathTextfield.getEditText().setText(PathUtil.getPathFromUri(uri));
                documentFile = DocumentFile.fromTreeUri(MainActivity.this, uri);
                if(SAFPref != null) SAFPref.putPreference(SAFPreference.KEY, uri.toString());
            }
            
            @Override
            public DocumentFile getDocumentFile() {
                return this.documentFile;
            }
        };
        
        // Retrieve previously chosen file destination if exists
        if(SAFPref != null) {
            if(!((String)SAFPref.getPreference(SAFPreference.KEY)).isEmpty()) {
            	SAFLaunchCallback.onLaunchResult(Uri.parse((String)SAFPref.getPreference(SAFPreference.KEY)));
            }
        }
        
        BottomSheetDialog d = new BottomSheetDialog(this);
        d.setCancelable(true);
        loadIconPreview(exportIconBinding.preview, icon);
        exportIconBinding.title.setText(getString(R.string.dialog_title_export_icon, exportType));
        exportIconBinding.msg.setText(getString(R.string.dialog_msg_export_icon, iconNameConvention, exportType));
        exportIconBinding.positiveBt.setText(R.string.dialog_ok_btn_export_icon);
        exportIconBinding.positiveBt.setOnClickListener(view -> {
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
                    // Apply options if available
                    if(exportIconBinding.checkboxAdvancedOptions.isChecked()) icon.setOptions(getIconOptions());
                    
                	// SVG
                    if(option == exportToSvgOption) {
                        exportToSvg(exportIconBinding.iconFilenameTextfield.getEditText().getText().toString(), SAFLaunchCallback.getDocumentFile(), icon);
                    }
                    // Vector
                    if(option == exportToVectorOption) {
                        exportToVector(exportIconBinding.iconFilenameTextfield.getEditText().getText().toString(), SAFLaunchCallback.getDocumentFile(), icon, exportIconBinding.checkboxTint.isChecked());
                    }
                }
                
                d.dismiss();
        });
        exportIconBinding.negativeBt.setText(R.string.dialog_cancel_btn_export_icon);
        exportIconBinding.negativeBt.setOnClickListener(view -> {
                d.dismiss();
        });
        d.setContentView(exportIconBinding.getRoot());
        d.show();
        configureIconOptions(exportIconBinding, icon);
    }
    
    private void loadIconPreview(ImageView view, IconModel icon) {
    	try {
            SVG svg = SVG.getFromAsset(getAssets(), icon.getAssetPath());
            Glide.with(MainActivity.this)
            .as(Drawable.class)
            .priority(Priority.IMMEDIATE)
            .load(svg)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(MainActivity.this.getDrawable(R.drawable.ic_loading))
            .into(view);
        } catch(SVGParseException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } 
    }
    
    private void configureIconOptions(LayoutExportIconBinding editor, IconModel icon) {
        try {
            InputStream iconInputStream = getAssets().open(icon.getAssetPath());
        	IconOptions options = (IconOptions)iconOptionsVM;
            options.setIconInputStream(iconInputStream);
            options.getXml_contentLiveData().observe(MainActivity.this, xml -> {
                    try {
                        SVG svg = SVG.getFromString(xml);
                        Glide.with(MainActivity.this)
                        .as(Drawable.class)
                        .priority(Priority.IMMEDIATE)
                        .load(svg)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(MainActivity.this.getDrawable(R.drawable.ic_loading))
                        .into(editor.preview);
                    } catch(SVGParseException e) {
                        e.printStackTrace();
                    }
            });
            
            editor.optionStrokeColor.setOnClickListener(view -> {
                    ColorEnvelopeListener listener = new ColorEnvelopeListener() {
                        @Override
                        public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                            if(fromUser) {
                                refreshIconOptions(options, editor, "#"+envelope.getHexCode().substring(2), null);
                                view.setBackgroundColor(Color.parseColor("#"+envelope.getHexCode().substring(2)));
                                Toast.makeText(getApplicationContext(), envelope.getHexCode(), Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                    launchColorPickerDialog(listener);
            });
            
            editor.optionFillColor.setOnClickListener(view -> {
                    ColorEnvelopeListener listener = new ColorEnvelopeListener() {
                        @Override
                        public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                            if(fromUser) {
                                refreshIconOptions(options, editor, null, "#"+envelope.getHexCode().substring(2));
                                view.setBackgroundColor(Color.parseColor("#"+envelope.getHexCode().substring(2)));
                                Toast.makeText(getApplicationContext(), envelope.getHexCode(), Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                    launchColorPickerDialog(listener);
            });
            
            TextWatcher inputCallback = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                    // TODO: Implement this method
                }
                
                @Override
                public void afterTextChanged(Editable s) {
                    // TODO: Implement this method
                }
                
                @Override
                public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                    refreshIconOptions(options, editor, null, null);
                }
            };
            editor.optionStrokeLinejoin.getEditText().addTextChangedListener(inputCallback);
            editor.optionStrokeLinecap.getEditText().addTextChangedListener(inputCallback);
            editor.optionStrokeWidth.getEditText().addTextChangedListener(inputCallback);
            editor.optionWidth.getEditText().addTextChangedListener(inputCallback);
            editor.optionHeight.getEditText().addTextChangedListener(inputCallback);
            
        } catch(IOException err) {
        	err.printStackTrace();
        }
    }
    
    private void refreshIconOptions(IconOptions options, LayoutExportIconBinding editor, String stroke, String fill) {
    	// stroke and fill color
        if(fill != null) {
            options.setFillColor(fill); 
        }
        if(stroke != null) {
            options.setStrokeColor(stroke);
        }
        
        // dimensions 
        if(!TextUtils.isEmpty(editor.optionWidth.getEditText().getText().toString())) {
        	options.setWidth(editor.optionWidth.getEditText().getText().toString());
        }
        if(!TextUtils.isEmpty(editor.optionHeight.getEditText().getText().toString())) {
        	options.setHeight(editor.optionHeight.getEditText().getText().toString());
        }
        
        // stroke width
        String str_width = editor.optionStrokeWidth.getEditText().getText().toString();
        int width = TextUtils.isEmpty(str_width) ? 1 : Integer.parseInt(str_width);
        options.setStrokeWidth(width);
        
        // stroke line
        String strokeLineCap = editor.optionStrokeLinecap.getEditText().getText().toString();
        options.setStrokeLineCap(strokeLineCap.equals("square") ? IconOptions.StrokeLineCap.SQUARE : strokeLineCap.equals("butt") ? IconOptions.StrokeLineCap.BUTT : IconOptions.StrokeLineCap.ROUND);
        String strokeLineJoin = editor.optionStrokeLinejoin.getEditText().getText().toString();
        options.setStrokeLineJoin(strokeLineJoin.equals("bevel") ? IconOptions.StrokeLineJoin.BEVEL : strokeLineJoin.equals("arcs") ? IconOptions.StrokeLineJoin.ARCS : strokeLineJoin.equals("miter") ? IconOptions.StrokeLineJoin.MITER : strokeLineJoin.equals("miter-clip") ? IconOptions.StrokeLineJoin.MITER_CLIP : IconOptions.StrokeLineJoin.ROUND); 
    }
    
    private IconOptions getIconOptions() {
    	return (IconOptions)iconOptionsVM;
    }
    
    private void launchColorPickerDialog(ColorEnvelopeListener listener) {
    	new ColorPickerDialog.Builder(this)
		.setTitle("ColorPicker Dialog")
		.setPreferenceName("MyColorPickerDialog")
		.setPositiveButton(getString(R.string.confirm), listener)
		.setNegativeButton(getString(R.string.cancel),
		    new DialogInterface.OnClickListener() {
		        @Override
		        public void onClick(DialogInterface dialogInterface, int i) {
		            dialogInterface.dismiss();
		        }
		    })
		.attachAlphaSlideBar(false) // the default value is true.
		.attachBrightnessSlideBar(true)  // the default value is true.
		.setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
		.show();
    }
    
    private void exportToSvg(String filename, DocumentFile documentFile, IconModel icon) {
    	if(documentFile == null) {
    		return;
    	}
        try {
            var fis = (applyIconOptions && icon.getOptions() != null) ? (InputStream) icon.getOptions().getModifiedVersion() : getAssets().open(icon.getAssetPath());
            
        	String destination = PathUtil.getPathFromUri(documentFile.getUri());
            Path destinationPath = Paths.get(destination+"/"+filename.replace('-', '_')+".svg");
            Files.copy(fis, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            if(Files.exists(destinationPath)) {
                Toast.makeText(this, "file saved: "+destinationPath.toString(), Toast.LENGTH_SHORT).show();
                icon.setOptions(null); // reset options
            }
        } catch(IOException err) {
        	err.printStackTrace();
            Toast.makeText(this, "An error occurred while saving the file!", Toast.LENGTH_SHORT).show();
            icon.setOptions(null); // reset options
        }
    }
    
    private void exportToVector(String filename, DocumentFile documentFile, IconModel icon, boolean applyTint) {
    	if(documentFile == null) {
    		return;
    	}
        String destination = PathUtil.getPathFromUri(documentFile.getUri());
        Path destinationPath = Paths.get(destination+"/"+filename.replace('-', '_')+".xml");
        try {
            var fis = (applyIconOptions && icon.getOptions() != null) ? (InputStream) icon.getOptions().getModifiedVersion() : getAssets().open(icon.getAssetPath());
            
            String data = ConvertUtils.inputStream2String(fis, "utf-8");
            String svgData = data.replaceAll("currentColor", "#000000");
            
            Files.deleteIfExists(destinationPath);
            Files.write(destinationPath, svgData.getBytes(), StandardOpenOption.CREATE_NEW);
            Svg2Vector.parseSvgToXml(destinationPath.toFile(), destinationPath.toFile(), -1, -1, applyTint);
            
            if(Files.exists(destinationPath)) {
                Toast.makeText(this, "file saved: "+destinationPath.toString(), Toast.LENGTH_SHORT).show();
                icon.setOptions(null); // reset options
            }
        } catch(IOException err) {
        	err.printStackTrace();
            Toast.makeText(this, "An error occurred while saving the file!", Toast.LENGTH_SHORT).show();
            icon.setOptions(null); // reset options
        }
    }
    
}
