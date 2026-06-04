package com.mckl.assignment1.model;

/**
 * Data model for a single option belonging to a quiz question.
 */
public class QuestionOption {
    private final int optionId;
    private final int questionId;
    private final String text;
    private final String categoryKey;
    private final int pointValue;
    private final int displayOrder;

    public QuestionOption(
            int optionId,
            int questionId,
            String text,
            String categoryKey,
            int pointValue,
            int displayOrder
    ) {
        this.optionId = optionId;
        this.questionId = questionId;
        this.text = text;
        this.categoryKey = categoryKey;
        this.pointValue = pointValue;
        this.displayOrder = displayOrder;
    }

    public int getOptionId() {
        return optionId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public String getText() {
        return text;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public int getPointValue() {
        return pointValue;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
