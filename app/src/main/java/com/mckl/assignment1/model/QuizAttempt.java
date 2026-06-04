package com.mckl.assignment1.model;

/**
 * Data model for a persisted quiz attempt.
 */
public class QuizAttempt {
    private final long attemptId;
    private final String archetypeKey;
    private final String archetypeTitle;
    private final String completedAt;

    public QuizAttempt(long attemptId, String archetypeKey, String archetypeTitle, String completedAt) {
        this.attemptId = attemptId;
        this.archetypeKey = archetypeKey;
        this.archetypeTitle = archetypeTitle;
        this.completedAt = completedAt;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public String getArchetypeKey() {
        return archetypeKey;
    }

    public String getArchetypeTitle() {
        return archetypeTitle;
    }

    public String getCompletedAt() {
        return completedAt;
    }
}
