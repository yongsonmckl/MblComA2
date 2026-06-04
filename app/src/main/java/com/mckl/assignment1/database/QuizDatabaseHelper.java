package com.mckl.assignment1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mckl.assignment1.model.CareerArchetype;

/**
 * SQLite helper for the Career Quiz app.
 */
public class QuizDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "career_quiz.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_CATEGORY_KEY = "category_key";
    public static final String COLUMN_CATEGORY_TITLE = "title";
    public static final String COLUMN_CATEGORY_FIELD = "field";
    public static final String COLUMN_CATEGORY_DESCRIPTION = "description";
    public static final String COLUMN_CATEGORY_EXAMPLES = "examples";

    public static final String TABLE_QUESTIONS = "questions";
    public static final String COLUMN_QUESTION_ID = "question_id";
    public static final String COLUMN_QUESTION_TEXT = "question_text";
    public static final String COLUMN_QUESTION_DISPLAY_ORDER = "display_order";

    public static final String TABLE_OPTIONS = "options";
    public static final String COLUMN_OPTION_ID = "option_id";
    public static final String COLUMN_OPTION_QUESTION_ID = "question_id";
    public static final String COLUMN_OPTION_TEXT = "option_text";
    public static final String COLUMN_OPTION_CATEGORY_ID = "category_id";
    public static final String COLUMN_OPTION_POINT_VALUE = "point_value";
    public static final String COLUMN_OPTION_DISPLAY_ORDER = "display_order";

    public static final String TABLE_QUIZ_ATTEMPTS = "quiz_attempts";
    public static final String COLUMN_ATTEMPT_ID = "attempt_id";
    public static final String COLUMN_ATTEMPT_FINAL_CATEGORY_ID = "final_category_id";
    public static final String COLUMN_ATTEMPT_COMPLETED_AT = "completed_at";

    public static final String TABLE_ATTEMPT_ANSWERS = "attempt_answers";
    public static final String COLUMN_ATTEMPT_ANSWER_ID = "attempt_answer_id";
    public static final String COLUMN_ATTEMPT_ANSWER_ATTEMPT_ID = "attempt_id";
    public static final String COLUMN_ATTEMPT_ANSWER_QUESTION_ID = "question_id";
    public static final String COLUMN_ATTEMPT_ANSWER_OPTION_ID = "option_id";
    public static final String COLUMN_ATTEMPT_ANSWER_AWARDED_POINTS = "awarded_points";

    public QuizDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " ("
                + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CATEGORY_KEY + " TEXT NOT NULL UNIQUE, "
                + COLUMN_CATEGORY_TITLE + " TEXT NOT NULL, "
                + COLUMN_CATEGORY_FIELD + " TEXT NOT NULL, "
                + COLUMN_CATEGORY_DESCRIPTION + " TEXT NOT NULL, "
                + COLUMN_CATEGORY_EXAMPLES + " TEXT NOT NULL"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_QUESTIONS + " ("
                + COLUMN_QUESTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_QUESTION_TEXT + " TEXT NOT NULL, "
                + COLUMN_QUESTION_DISPLAY_ORDER + " INTEGER NOT NULL UNIQUE"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_OPTIONS + " ("
                + COLUMN_OPTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_OPTION_QUESTION_ID + " INTEGER NOT NULL, "
                + COLUMN_OPTION_TEXT + " TEXT NOT NULL, "
                + COLUMN_OPTION_CATEGORY_ID + " INTEGER NOT NULL, "
                + COLUMN_OPTION_POINT_VALUE + " INTEGER NOT NULL DEFAULT 1, "
                + COLUMN_OPTION_DISPLAY_ORDER + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + COLUMN_OPTION_QUESTION_ID + ") REFERENCES "
                + TABLE_QUESTIONS + "(" + COLUMN_QUESTION_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + COLUMN_OPTION_CATEGORY_ID + ") REFERENCES "
                + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID + "), "
                + "UNIQUE (" + COLUMN_OPTION_QUESTION_ID + ", " + COLUMN_OPTION_DISPLAY_ORDER + ")"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_QUIZ_ATTEMPTS + " ("
                + COLUMN_ATTEMPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ATTEMPT_FINAL_CATEGORY_ID + " INTEGER, "
                + COLUMN_ATTEMPT_COMPLETED_AT + " TEXT, "
                + "FOREIGN KEY (" + COLUMN_ATTEMPT_FINAL_CATEGORY_ID + ") REFERENCES "
                + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID + ")"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_ATTEMPT_ANSWERS + " ("
                + COLUMN_ATTEMPT_ANSWER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ATTEMPT_ANSWER_ATTEMPT_ID + " INTEGER NOT NULL, "
                + COLUMN_ATTEMPT_ANSWER_QUESTION_ID + " INTEGER NOT NULL, "
                + COLUMN_ATTEMPT_ANSWER_OPTION_ID + " INTEGER NOT NULL, "
                + COLUMN_ATTEMPT_ANSWER_AWARDED_POINTS + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + COLUMN_ATTEMPT_ANSWER_ATTEMPT_ID + ") REFERENCES "
                + TABLE_QUIZ_ATTEMPTS + "(" + COLUMN_ATTEMPT_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + COLUMN_ATTEMPT_ANSWER_QUESTION_ID + ") REFERENCES "
                + TABLE_QUESTIONS + "(" + COLUMN_QUESTION_ID + "), "
                + "FOREIGN KEY (" + COLUMN_ATTEMPT_ANSWER_OPTION_ID + ") REFERENCES "
                + TABLE_OPTIONS + "(" + COLUMN_OPTION_ID + "), "
                + "UNIQUE (" + COLUMN_ATTEMPT_ANSWER_ATTEMPT_ID + ", " + COLUMN_ATTEMPT_ANSWER_QUESTION_ID + ")"
                + ")");

        seedDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTEMPT_ANSWERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_ATTEMPTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OPTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    private void seedDatabase(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            long architectId = insertCategory(db, CareerArchetype.ARCHITECT);
            long visionaryId = insertCategory(db, CareerArchetype.VISIONARY);
            long guardianId = insertCategory(db, CareerArchetype.GUARDIAN);
            long captainId = insertCategory(db, CareerArchetype.CAPTAIN);

            insertQuestionWithOptions(db, 1, "How do you prefer to spend your free time?",
                    new String[]{
                            "Building or fixing things",
                            "Drawing, writing, or playing music",
                            "Volunteering or helping friends",
                            "Organizing events or leading a group"
                    },
                    new long[]{architectId, visionaryId, guardianId, captainId});

            insertQuestionWithOptions(db, 2, "Which subject did you enjoy most in school?",
                    new String[]{
                            "Mathematics or Science",
                            "Art or Literature",
                            "Biology or Social Studies",
                            "History or Economics"
                    },
                    new long[]{architectId, visionaryId, guardianId, captainId});

            insertQuestionWithOptions(db, 3, "What kind of problems do you enjoy solving?",
                    new String[]{
                            "Technical or logical puzzles",
                            "Creative or design challenges",
                            "Personal or interpersonal issues",
                            "Strategic or business problems"
                    },
                    new long[]{architectId, visionaryId, guardianId, captainId});

            insertQuestionWithOptions(db, 4, "In a group project, what is your typical role?",
                    new String[]{
                            "The technical expert",
                            "The creative lead",
                            "The supportive team member",
                            "The project manager"
                    },
                    new long[]{architectId, visionaryId, guardianId, captainId});

            insertQuestionWithOptions(db, 5, "What is your ideal work environment?",
                    new String[]{
                            "A laboratory or tech hub",
                            "A studio or flexible space",
                            "A hospital, school, or community center",
                            "A modern office or courtroom"
                    },
                    new long[]{architectId, visionaryId, guardianId, captainId});

            insertQuestionWithOptions(db, 6, "What motivates you the most?",
                    new String[]{
                            "Discovering how things work",
                            "Creating something beautiful",
                            "Making a difference in someone's life",
                            "Achieving goals and winning"
                    },
                    new long[]{architectId, visionaryId, guardianId, captainId});

            insertQuestionWithOptions(db, 7, "How do you handle complex data?",
                    new String[]{
                            "Analyze it for patterns",
                            "Visualize it creatively",
                            "Consider its human impact",
                            "Use it to make decisions"
                    },
                    new long[]{architectId, visionaryId, guardianId, captainId});

            insertQuestionWithOptions(db, 8, "Which of these tools would you rather use?",
                    new String[]{
                            "A computer or toolkit",
                            "A paintbrush or camera",
                            "A stethoscope or textbook",
                            "A planner or briefcase"
                    },
                    new long[]{architectId, visionaryId, guardianId, captainId});

            insertQuestionWithOptions(db, 9, "What is your favorite type of book or movie?",
                    new String[]{
                            "Science fiction or non-fiction",
                            "Fantasy or drama",
                            "Biographies or human interest",
                            "Thriller or business success stories"
                    },
                    new long[]{architectId, visionaryId, guardianId, captainId});

            insertQuestionWithOptions(db, 10, "How do others describe you?",
                    new String[]{
                            "Logical and precise",
                            "Creative and unique",
                            "Kind and helpful",
                            "Confident and ambitious"
                    },
                    new long[]{architectId, visionaryId, guardianId, captainId});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private long insertCategory(SQLiteDatabase db, CareerArchetype archetype) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_KEY, archetype.name());
        values.put(COLUMN_CATEGORY_TITLE, archetype.getTitle());
        values.put(COLUMN_CATEGORY_FIELD, archetype.getField());
        values.put(COLUMN_CATEGORY_DESCRIPTION, archetype.getDescription());
        values.put(COLUMN_CATEGORY_EXAMPLES, archetype.getExamples());
        return db.insert(TABLE_CATEGORIES, null, values);
    }

    private void insertQuestionWithOptions(
            SQLiteDatabase db,
            int displayOrder,
            String questionText,
            String[] optionTexts,
            long[] categoryIds
    ) {
        ContentValues questionValues = new ContentValues();
        questionValues.put(COLUMN_QUESTION_TEXT, questionText);
        questionValues.put(COLUMN_QUESTION_DISPLAY_ORDER, displayOrder);
        long questionId = db.insert(TABLE_QUESTIONS, null, questionValues);

        for (int i = 0; i < optionTexts.length; i++) {
            ContentValues optionValues = new ContentValues();
            optionValues.put(COLUMN_OPTION_QUESTION_ID, questionId);
            optionValues.put(COLUMN_OPTION_TEXT, optionTexts[i]);
            optionValues.put(COLUMN_OPTION_CATEGORY_ID, categoryIds[i]);
            optionValues.put(COLUMN_OPTION_POINT_VALUE, 1);
            optionValues.put(COLUMN_OPTION_DISPLAY_ORDER, i);
            db.insert(TABLE_OPTIONS, null, optionValues);
        }
    }
}
