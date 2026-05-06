package com.mckl.assignment1.model;

import java.util.List;

/**
 * data model for a quiz question
 * each question consists of a query string and a list of four options
 */
public class Question {
    // The text of the question
    private final String text;
    // The list of 4 possible answers
    private final List<String> options;

    /**
     * constructor for Question
     * @param text the question text
     * @param options the list of options
     */
    public Question(String text, List<String> options) {
        this.text = text;
        this.options = options;
    }

    public String getText() { return text; }
    public List<String> getOptions() { return options; }
}
