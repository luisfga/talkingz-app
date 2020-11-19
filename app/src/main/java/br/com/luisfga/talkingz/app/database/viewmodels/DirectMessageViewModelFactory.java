package br.com.luisfga.talkingz.app.database.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.UUID;

public class DirectMessageViewModelFactory implements ViewModelProvider.Factory {

    private Application application;
    private UUID contactId;
    private UUID mainUserId;

    public DirectMessageViewModelFactory(Application application, UUID contactId, UUID mainUserId) {
        this.application = application;
        this.contactId = contactId;
        this.mainUserId = mainUserId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DirectMessageViewModel(application, contactId, mainUserId);
    }
}
