package com.mckl.assignment1.model;

import java.util.List;

/**
 * Data model for a quiz question.
 * Each question consists of a query string and a list of four options.
 */
public class Question {
    // The text of the question
    private final String text;
    // The list of 4 possible answers
    private final List<String> options;

    /**
     * Constructor for Question.
     * @param text The question text.
     * @param options The list of options (must be 4 as per requirements).
     */
    public Question(String text, List<String> options) {
        this.text = text;
        this.options = options;
    }

    public String getText() { return text; }
    public List<String> getOptions() { return options; }
}
