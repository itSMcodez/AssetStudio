package com.itsmcodez.assetstudio.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class IconOptionsViewModel extends AndroidViewModel {
    private String xml_content;
    private MutableLiveData<String> xml_contentLiveData;

    public IconOptionsViewModel(Application application) {
        super(application);
        xml_content = "";
        xml_contentLiveData = new MutableLiveData<>();
    }

    public String getXml_content() {
        return this.xml_content;
    }

    public void setXml_content(String xml_content) {
        this.xml_content = xml_content;
    }

    public MutableLiveData<String> getXml_contentLiveData() {
        return this.xml_contentLiveData;
    }

    public void setXml_contentLiveData(String xml_content) {
        xml_contentLiveData.setValue(xml_content);
        this.xml_content = xml_content;
    }
}
