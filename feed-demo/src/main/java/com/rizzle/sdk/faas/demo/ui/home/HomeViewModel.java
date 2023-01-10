package com.rizzle.sdk.faas.demo.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("See Feed in Dashboard Fragment ->");
    }

    public LiveData<String> getText() {
        return mText;
    }
}