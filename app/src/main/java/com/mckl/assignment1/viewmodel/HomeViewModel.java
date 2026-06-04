package com.mckl.assignment1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mckl.assignment1.model.QuizAttempt;
import com.mckl.assignment1.repository.QuizRepository;

/**
 * ViewModel for loading the latest persisted result on the home screen.
 */
public class HomeViewModel extends AndroidViewModel {
    private final QuizRepository repository;
    private final MutableLiveData<QuizAttempt> latestAttempt = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new QuizRepository(application);
        refreshLatestAttempt();
    }

    public LiveData<QuizAttempt> getLatestAttempt() {
        return latestAttempt;
    }

    public void refreshLatestAttempt() {
        latestAttempt.setValue(repository.getLatestQuizAttempt());
    }
}
