package com.mckl.assignment1.model;

/**
 * Enum representing the four career paths/archetypes as defined in the assignment requirements.
 * Each archetype contains a title, broad field, description, and job examples.
 */
public enum CareerArchetype {
    ARCHITECT("The Architect", "STEM (Science, Technology, Engineering, Math)", "You are analytical, logical, and love solving complex problems. You thrive in environments that require precision and technical expertise.", "Software Engineer, Data Scientist, Civil Engineer, Research Scientist"),
    VISIONARY("The Visionary", "Arts and Design", "You are creative, imaginative, and see the world differently. You enjoy expressing yourself and creating things that inspire others.", "Graphic Designer, Architect, Film Director, UX/UI Designer"),
    GUARDIAN("The Guardian", "Healthcare and Education", "You are compassionate, patient, and dedicated to helping others. You find fulfillment in nurturing growth and providing care.", "Doctor, Teacher, Nurse, Social Worker"),
    CAPTAIN("The Captain", "Business and Law", "You are ambitious, strategic, and a natural leader. You enjoy taking charge, making big decisions, and driving success.", "CEO, Lawyer, Project Manager, Marketing Director");

    private final String title;
    private final String field;
    private final String description;
    private final String examples;

    CareerArchetype(String title, String field, String description, String examples) {
        this.title = title;
        this.field = field;
        this.description = description;
        this.examples = examples;
    }

    public String getTitle() { return title; }
    public String getField() { return field; }
    public String getDescription() { return description; }
    public String getExamples() { return examples; }
}
