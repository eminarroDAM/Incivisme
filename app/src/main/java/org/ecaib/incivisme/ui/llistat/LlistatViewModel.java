package org.ecaib.incivisme.ui.llistat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LlistatViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LlistatViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}