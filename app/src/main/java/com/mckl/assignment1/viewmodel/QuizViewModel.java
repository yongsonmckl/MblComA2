package com.mckl.assignment1.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.mckl.assignment1.model.CareerArchetype;
import com.mckl.assignment1.model.Question;
import com.mckl.assignment1.repository.QuizRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel for the Career Quiz.
 * Manages the state of the quiz, handles question progression, calculates results,
 * and persists the last result using SharedPreferences.
 */
public class QuizViewModel extends AndroidViewModel {
    private final QuizRepository repository;
    private final List<Question> questions;
    // Tally of scores: index 0: ARCHITECT, 1: VISIONARY, 2: GUARDIAN, 3: CAPTAIN
    private final int[] scores = new int[4]; 
    
    // LiveData for the UI to observe current state
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Question> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<CareerArchetype> result = new MutableLiveData<>();

    public QuizViewModel(@NonNull Application application) {
        super(application);
        repository = new QuizRepository();
        questions = repository.getQuestions();
        updateQuestion();
    }

    public LiveData<Integer> getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }
    public LiveData<Question> getCurrentQuestion() {
        return currentQuestion;
    }
    public LiveData<CareerArchetype> getResult() {
        return result;
    }
    public int getTotalQuestions() {
        return questions.size();
    }

    /**
     * Records the user's choice and moves to the next question or finishes the quiz.
     * The option index corresponds to the career categories:
     * 0: ARCHITECT, 1: VISIONARY, 2: GUARDIAN, 3: CAPTAIN
     * @param optionIndex The index of the selected option (0-3).
     */
    public void answerQuestion(int optionIndex) {
        if (optionIndex >= 0 && optionIndex < 4) {
            // Increment the score for the selected category
            scores[optionIndex]++;
        }
        Integer index = currentQuestionIndex.getValue();
        if (index != null) {
            // If there are more questions, move to the next one
            if (index < questions.size() - 1) {
                currentQuestionIndex.setValue(index + 1);
                updateQuestion();
            } else {
                // Otherwise, calculate the final result
                calculateAndSaveResult();
            }
        }
    }

    private void updateQuestion() {
        Integer index = currentQuestionIndex.getValue();
        if (index != null && index < questions.size()) {
            currentQuestion.setValue(questions.get(index));
        }
    }

    /**
     * Calculates the final result and saves it to SharedPreferences for persistence.
     */
    private void calculateAndSaveResult() {
        CareerArchetype finalResult = repository.calculateResult(scores);
        result.setValue(finalResult);
        
        // Save to SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        prefs.edit()
             .putString("last_result", finalResult.getTitle())
             .putString("last_date", date)
             .apply();
    }
}
