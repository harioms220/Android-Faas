package com.rizzle.sdk.faas.demo.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Go to Dashboard Fragment to enjoy Feed <-");
    }

    public LiveData<String> getText() {
        return mText;
    }
}