package com.mckl.assignment1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mckl.assignment1.model.CareerArchetype;
import com.mckl.assignment1.model.Question;
import com.mckl.assignment1.model.QuestionOption;
import com.mckl.assignment1.repository.QuizRepository;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel for the Career Quiz flow backed by SQLite.
 */
public class QuizViewModel extends AndroidViewModel {
    private final QuizRepository repository;
    private final List<Question> questions;
    private final int[] scores = new int[CareerArchetype.values().length];
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Question> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<CareerArchetype> result = new MutableLiveData<>();
    private long currentAttemptId = -1L;

    public QuizViewModel(@NonNull Application application) {
        super(application);
        repository = new QuizRepository(application);
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

    public void answerQuestion(int optionPosition) {
        Question question = currentQuestion.getValue();
        Integer index = currentQuestionIndex.getValue();
        if (question == null || index == null) {
            return;
        }

        List<QuestionOption> options = question.getOptions();
        if (optionPosition < 0 || optionPosition >= options.size()) {
            return;
        }

        QuestionOption selectedOption = options.get(optionPosition);
        if (currentAttemptId < 0) {
            currentAttemptId = repository.createQuizAttempt();
        }

        int scoreIndex = CareerArchetype.valueOf(selectedOption.getCategoryKey()).ordinal();
        scores[scoreIndex] += selectedOption.getPointValue();
        repository.insertAttemptAnswer(
                currentAttemptId,
                question.getQuestionId(),
                selectedOption.getOptionId(),
                selectedOption.getPointValue()
        );

        if (index < questions.size() - 1) {
            currentQuestionIndex.setValue(index + 1);
            updateQuestion();
        } else {
            calculateAndSaveResult();
        }
    }

    private void updateQuestion() {
        Integer index = currentQuestionIndex.getValue();
        if (index != null && index >= 0 && index < questions.size()) {
            currentQuestion.setValue(questions.get(index));
        }
    }

    private void calculateAndSaveResult() {
        CareerArchetype finalResult = repository.calculateResult(scores);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        if (currentAttemptId < 0) {
            currentAttemptId = repository.createQuizAttempt();
        }

        repository.updateQuizAttemptResult(currentAttemptId, finalResult.name(), date);
        result.setValue(finalResult);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Arrays.fill(scores, 0);
    }
}
