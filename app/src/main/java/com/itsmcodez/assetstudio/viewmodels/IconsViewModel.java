package com.itsmcodez.assetstudio.viewmodels;

import androidx.annotation.NonNull;
import static androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle;
import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import android.app.Application;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;
import com.itsmcodez.assetstudio.BaseApplication;
import com.itsmcodez.assetstudio.markers.IconPacks;
import com.itsmcodez.assetstudio.repositories.IconsRepository;
import java.util.List;
import com.itsmcodez.assetstudio.models.IconModel;
import androidx.lifecycle.MutableLiveData;

public class IconsViewModel extends ViewModel {
    private List<IconModel> icons;
    private MutableLiveData<List<IconModel>> iconsLiveData;
    private IconsRepository repository;
    
    public IconsViewModel(@NonNull Application application, @NonNull IconPacks iconPack, SavedStateHandle savedStateHandle) { 
        repository = IconsRepository.getInstance(application, iconPack);
        icons = repository.getIcons();
        iconsLiveData = repository.getIconsLiveData();
     }

    public static final ViewModelInitializer<IconsViewModel> initializer = new ViewModelInitializer<>(
        IconsViewModel.class,
        creationExtras -> {
            BaseApplication app = (BaseApplication) creationExtras.get(APPLICATION_KEY);
            assert app != null;
            SavedStateHandle savedStateHandle = createSavedStateHandle(creationExtras);

            return new IconsViewModel(app, app.getDefaultIconPack(), savedStateHandle);
        });
    
    @NonNull
    public List<IconModel> getIcons() {
    	return this.icons;
    }
    
    @NonNull
    public MutableLiveData<List<IconModel>> getIconsLiveData() {
    	return this.iconsLiveData;
    }
    
    public void setDefaultIconPack(@NonNull IconPacks iconPack) {
    	repository.setDefaultIconPack(iconPack);
    }
}
