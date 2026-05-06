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
 * ViewModel for the Career Quiz
 * manages the state of the quiz, handles question progression, and calculates results
 * holds the last result by using Shared Preferences
 */
public class QuizViewModel extends AndroidViewModel {

    private final QuizRepository repository; // stores the question List

    private final List<Question> questions; // question list

    // index for the scores: 0 = ARCHITECT, 1 = VISIONARY, 2 = GUARDIAN, 3 = CAPTAIN
    private final int[] scores = new int[4]; // turns out the number here isn't index but slots
    
    // LiveData for the UI to see changes
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Question> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<CareerArchetype> result = new MutableLiveData<>();

    // pulls the 10 questions and updates it to the UI
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
     * records the user's choice and moves to the next question or finishes the quiz
     * the option index corresponds to the career categories:
     * 0: ARCHITECT, 1: VISIONARY, 2: GUARDIAN, 3: CAPTAIN
     * @param optionIndex the index of the selected option (0,1,2,3)
     */
    public void answerQuestion(int optionIndex) {
        if (optionIndex >= 0 && optionIndex < 4) {
            // increment the score for the specific archetype chosen
            scores[optionIndex]++;
        }
        Integer index = currentQuestionIndex.getValue();
        if (index != null) {
            // move to the next question unless user is at the end
            if (index < questions.size() - 1) {
                currentQuestionIndex.setValue(index + 1);
                updateQuestion();
            } else {
                calculateAndSaveResult();
            }
        }
    }

    // updates the UI with the question the user is at based on index
    private void updateQuestion() {
        Integer index = currentQuestionIndex.getValue();
        if (index != null && index < questions.size()) {
            currentQuestion.setValue(questions.get(index)); // stops it from crashing
        }
    }

    // calculates the final result and also saves it to Shared Preferences
    private void calculateAndSaveResult() {
        CareerArchetype finalResult = repository.calculateResult(scores);
        result.setValue(finalResult);
        
        // save to Shared Preferences with formatting
        SharedPreferences prefs = getApplication().getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        prefs.edit()
             .putString("last_result", finalResult.getTitle())
             .putString("last_date", date)
             .apply();
    }
}
