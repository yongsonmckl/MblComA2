package com.mckl.assignment1.repository;

import com.mckl.assignment1.model.CareerArchetype;
import com.mckl.assignment1.model.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * repository class responsible for providing the quiz questions and calculating the result
 * based on the user's score tally
 */
public class QuizRepository {
    /**
     * provides a list of 10 questions focused on scenarios
     * @return the list of 10 Question objects
     */
    public List<Question> getQuestions() {
        List<Question> questions = new ArrayList<>();
        
        questions.add(new Question("How do you prefer to spend your free time?", 
            Arrays.asList("Building or fixing things", "Drawing, writing, or playing music", "Volunteering or helping friends", "Organizing events or leading a group")));
            
        questions.add(new Question("Which subject did you enjoy most in school?", 
            Arrays.asList("Mathematics or Science", "Art or Literature", "Biology or Social Studies", "History or Economics")));
            
        questions.add(new Question("What kind of problems do you enjoy solving?", 
            Arrays.asList("Technical or logical puzzles", "Creative or design challenges", "Personal or interpersonal issues", "Strategic or business problems")));
            
        questions.add(new Question("In a group project, what is your typical role?", 
            Arrays.asList("The technical expert", "The creative lead", "The supportive team member", "The project manager")));
            
        questions.add(new Question("What is your ideal work environment?", 
            Arrays.asList("A laboratory or tech hub", "A studio or flexible space", "A hospital, school, or community center", "A modern office or courtroom")));
            
        questions.add(new Question("What motivates you the most?", 
            Arrays.asList("Discovering how things work", "Creating something beautiful", "Making a difference in someone's life", "Achieving goals and winning")));
            
        questions.add(new Question("How do you handle complex data?", 
            Arrays.asList("Analyze it for patterns", "Visualize it creatively", "Consider its human impact", "Use it to make decisions")));
            
        questions.add(new Question("Which of these tools would you rather use?", 
            Arrays.asList("A computer or toolkit", "A paintbrush or camera", "A stethoscope or textbook", "A planner or briefcase")));
            
        questions.add(new Question("What is your favorite type of book or movie?", 
            Arrays.asList("Science fiction or non-fiction", "Fantasy or drama", "Biographies or human interest", "Thriller or business success stories")));
            
        questions.add(new Question("How do others describe you?", 
            Arrays.asList("Logical and precise", "Creative and unique", "Kind and helpful", "Confident and ambitious")));
            
        return questions;
    }

    /**
     * calculates the career archetype based on the highest tally of scores
     * @param scores array of 4 integers representing the count for each archetype
     * @return the CareerArchetype with the highest score.
     */
    public CareerArchetype calculateResult(int[] scores) {
        int maxIndex = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[maxIndex]) {
                maxIndex = i;
            }
        }
        
        switch (maxIndex) {
            case 0: return CareerArchetype.ARCHITECT;
            case 1: return CareerArchetype.VISIONARY;
            case 2: return CareerArchetype.GUARDIAN;
            case 3: return CareerArchetype.CAPTAIN;
            default: return CareerArchetype.ARCHITECT;
        }
    }
}
