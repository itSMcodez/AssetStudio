package com.itsmcodez.assetstudio.viewmodels;
import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.itsmcodez.assetstudio.callbacks.IconsLoadCallback;
import com.itsmcodez.assetstudio.repositories.IconsRepository;
import java.util.ArrayList;
import com.itsmcodez.assetstudio.models.IconModel;
import androidx.lifecycle.MutableLiveData;

public class IconsViewModel extends AndroidViewModel {
    private IconsRepository repository;
    private Application application;
    
    public IconsViewModel(Application application) {
    	super(application);
        this.application = application;
        repository = IconsRepository.getInstance(application);
    }
    
    public ArrayList<IconModel> getIcons() {
    	return repository.getIcons();
    }
    
    public MutableLiveData<ArrayList<IconModel>> getIconsLiveData(IconsLoadCallback callback) {
    	return repository.getIconsLiveData(callback);
    }
}
