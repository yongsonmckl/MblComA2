package com.mckl.assignment1.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data model for a quiz question loaded from SQLite.
 */
public class Question {
    private final int questionId;
    private final String text;
    private final List<QuestionOption> options;

    public Question(int questionId, String text, List<QuestionOption> options) {
        this.questionId = questionId;
        this.text = text;
        this.options = new ArrayList<>(options);
    }

    public int getQuestionId() {
        return questionId;
    }

    public String getText() {
        return text;
    }

    public List<QuestionOption> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
