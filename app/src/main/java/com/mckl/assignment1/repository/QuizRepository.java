package com.mckl.assignment1.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mckl.assignment1.database.QuizDatabaseHelper;
import com.mckl.assignment1.model.CareerArchetype;
import com.mckl.assignment1.model.Question;
import com.mckl.assignment1.model.QuestionOption;
import com.mckl.assignment1.model.QuizAttempt;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class responsible for SQLite CRUD and quiz business logic.
 */
public class QuizRepository {
    private final QuizDatabaseHelper dbHelper;

    public QuizRepository(Context context) {
        dbHelper = new QuizDatabaseHelper(context.getApplicationContext());
    }

    public List<Question> getQuestions() {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                QuizDatabaseHelper.TABLE_QUESTIONS,
                new String[]{
                        QuizDatabaseHelper.COLUMN_QUESTION_ID,
                        QuizDatabaseHelper.COLUMN_QUESTION_TEXT
                },
                null,
                null,
                null,
                null,
                QuizDatabaseHelper.COLUMN_QUESTION_DISPLAY_ORDER + " ASC"
        );

        try {
            while (cursor.moveToNext()) {
                int questionId = cursor.getInt(cursor.getColumnIndexOrThrow(QuizDatabaseHelper.COLUMN_QUESTION_ID));
                String questionText = cursor.getString(cursor.getColumnIndexOrThrow(QuizDatabaseHelper.COLUMN_QUESTION_TEXT));
                questions.add(new Question(questionId, questionText, getOptionsForQuestion(questionId)));
            }
        } finally {
            cursor.close();
        }

        return questions;
    }

    public List<QuestionOption> getOptionsForQuestion(int questionId) {
        List<QuestionOption> options = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT o." + QuizDatabaseHelper.COLUMN_OPTION_ID + ", "
                + "o." + QuizDatabaseHelper.COLUMN_OPTION_QUESTION_ID + ", "
                + "o." + QuizDatabaseHelper.COLUMN_OPTION_TEXT + ", "
                + "c." + QuizDatabaseHelper.COLUMN_CATEGORY_KEY + ", "
                + "o." + QuizDatabaseHelper.COLUMN_OPTION_POINT_VALUE + ", "
                + "o." + QuizDatabaseHelper.COLUMN_OPTION_DISPLAY_ORDER
                + " FROM " + QuizDatabaseHelper.TABLE_OPTIONS + " o "
                + " INNER JOIN " + QuizDatabaseHelper.TABLE_CATEGORIES + " c "
                + " ON o." + QuizDatabaseHelper.COLUMN_OPTION_CATEGORY_ID
                + " = c." + QuizDatabaseHelper.COLUMN_CATEGORY_ID
                + " WHERE o." + QuizDatabaseHelper.COLUMN_OPTION_QUESTION_ID + " = ?"
                + " ORDER BY o." + QuizDatabaseHelper.COLUMN_OPTION_DISPLAY_ORDER + " ASC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(questionId)});

        try {
            while (cursor.moveToNext()) {
                options.add(new QuestionOption(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5)
                ));
            }
        } finally {
            cursor.close();
        }

        return options;
    }

    public QuizAttempt getLatestQuizAttempt() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT qa." + QuizDatabaseHelper.COLUMN_ATTEMPT_ID + ", "
                + "c." + QuizDatabaseHelper.COLUMN_CATEGORY_KEY + ", "
                + "c." + QuizDatabaseHelper.COLUMN_CATEGORY_TITLE + ", "
                + "qa." + QuizDatabaseHelper.COLUMN_ATTEMPT_COMPLETED_AT
                + " FROM " + QuizDatabaseHelper.TABLE_QUIZ_ATTEMPTS + " qa "
                + " INNER JOIN " + QuizDatabaseHelper.TABLE_CATEGORIES + " c "
                + " ON qa." + QuizDatabaseHelper.COLUMN_ATTEMPT_FINAL_CATEGORY_ID
                + " = c." + QuizDatabaseHelper.COLUMN_CATEGORY_ID
                + " WHERE qa." + QuizDatabaseHelper.COLUMN_ATTEMPT_FINAL_CATEGORY_ID + " IS NOT NULL "
                + " ORDER BY qa." + QuizDatabaseHelper.COLUMN_ATTEMPT_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                return new QuizAttempt(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                );
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    public List<QuizAttempt> getAllQuizAttempts() {
        List<QuizAttempt> attempts = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT qa." + QuizDatabaseHelper.COLUMN_ATTEMPT_ID + ", "
                + "c." + QuizDatabaseHelper.COLUMN_CATEGORY_KEY + ", "
                + "c." + QuizDatabaseHelper.COLUMN_CATEGORY_TITLE + ", "
                + "qa." + QuizDatabaseHelper.COLUMN_ATTEMPT_COMPLETED_AT
                + " FROM " + QuizDatabaseHelper.TABLE_QUIZ_ATTEMPTS + " qa "
                + " LEFT JOIN " + QuizDatabaseHelper.TABLE_CATEGORIES + " c "
                + " ON qa." + QuizDatabaseHelper.COLUMN_ATTEMPT_FINAL_CATEGORY_ID
                + " = c." + QuizDatabaseHelper.COLUMN_CATEGORY_ID
                + " ORDER BY qa." + QuizDatabaseHelper.COLUMN_ATTEMPT_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        try {
            while (cursor.moveToNext()) {
                attempts.add(new QuizAttempt(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            }
        } finally {
            cursor.close();
        }

        return attempts;
    }

    public CareerArchetype calculateResult(int[] scores) {
        int maxIndex = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[maxIndex]) {
                maxIndex = i;
            }
        }

        CareerArchetype[] values = CareerArchetype.values();
        return values[maxIndex];
    }

    public long createQuizAttempt() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        return db.insert(QuizDatabaseHelper.TABLE_QUIZ_ATTEMPTS, null, values);
    }

    public long insertAttemptAnswer(long attemptId, int questionId, int optionId, int awardedPoints) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(QuizDatabaseHelper.COLUMN_ATTEMPT_ANSWER_ATTEMPT_ID, attemptId);
        values.put(QuizDatabaseHelper.COLUMN_ATTEMPT_ANSWER_QUESTION_ID, questionId);
        values.put(QuizDatabaseHelper.COLUMN_ATTEMPT_ANSWER_OPTION_ID, optionId);
        values.put(QuizDatabaseHelper.COLUMN_ATTEMPT_ANSWER_AWARDED_POINTS, awardedPoints);
        return db.insertWithOnConflict(
                QuizDatabaseHelper.TABLE_ATTEMPT_ANSWERS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public int updateQuizAttemptResult(long attemptId, String categoryKey, String completedAt) {
        long categoryId = getCategoryIdByKey(categoryKey);
        if (categoryId == -1L) {
            return 0;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(QuizDatabaseHelper.COLUMN_ATTEMPT_FINAL_CATEGORY_ID, categoryId);
        values.put(QuizDatabaseHelper.COLUMN_ATTEMPT_COMPLETED_AT, completedAt);
        return db.update(
                QuizDatabaseHelper.TABLE_QUIZ_ATTEMPTS,
                values,
                QuizDatabaseHelper.COLUMN_ATTEMPT_ID + " = ?",
                new String[]{String.valueOf(attemptId)}
        );
    }

    public long insertCategory(String categoryKey, String title, String field, String description, String examples) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(QuizDatabaseHelper.COLUMN_CATEGORY_KEY, categoryKey);
        values.put(QuizDatabaseHelper.COLUMN_CATEGORY_TITLE, title);
        values.put(QuizDatabaseHelper.COLUMN_CATEGORY_FIELD, field);
        values.put(QuizDatabaseHelper.COLUMN_CATEGORY_DESCRIPTION, description);
        values.put(QuizDatabaseHelper.COLUMN_CATEGORY_EXAMPLES, examples);
        return db.insert(QuizDatabaseHelper.TABLE_CATEGORIES, null, values);
    }

    public int updateCategory(String existingCategoryKey, String title, String field, String description, String examples) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(QuizDatabaseHelper.COLUMN_CATEGORY_TITLE, title);
        values.put(QuizDatabaseHelper.COLUMN_CATEGORY_FIELD, field);
        values.put(QuizDatabaseHelper.COLUMN_CATEGORY_DESCRIPTION, description);
        values.put(QuizDatabaseHelper.COLUMN_CATEGORY_EXAMPLES, examples);
        return db.update(
                QuizDatabaseHelper.TABLE_CATEGORIES,
                values,
                QuizDatabaseHelper.COLUMN_CATEGORY_KEY + " = ?",
                new String[]{existingCategoryKey}
        );
    }

    public int deleteCategory(String categoryKey) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                QuizDatabaseHelper.TABLE_CATEGORIES,
                QuizDatabaseHelper.COLUMN_CATEGORY_KEY + " = ?",
                new String[]{categoryKey}
        );
    }

    public long insertQuestion(String questionText, int displayOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(QuizDatabaseHelper.COLUMN_QUESTION_TEXT, questionText);
        values.put(QuizDatabaseHelper.COLUMN_QUESTION_DISPLAY_ORDER, displayOrder);
        return db.insert(QuizDatabaseHelper.TABLE_QUESTIONS, null, values);
    }

    public int updateQuestion(int questionId, String questionText, int displayOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(QuizDatabaseHelper.COLUMN_QUESTION_TEXT, questionText);
        values.put(QuizDatabaseHelper.COLUMN_QUESTION_DISPLAY_ORDER, displayOrder);
        return db.update(
                QuizDatabaseHelper.TABLE_QUESTIONS,
                values,
                QuizDatabaseHelper.COLUMN_QUESTION_ID + " = ?",
                new String[]{String.valueOf(questionId)}
        );
    }

    public int deleteQuestion(int questionId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                QuizDatabaseHelper.TABLE_QUESTIONS,
                QuizDatabaseHelper.COLUMN_QUESTION_ID + " = ?",
                new String[]{String.valueOf(questionId)}
        );
    }

    public long insertOption(int questionId, String optionText, String categoryKey, int pointValue, int displayOrder) {
        long categoryId = getCategoryIdByKey(categoryKey);
        if (categoryId == -1L) {
            return -1L;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(QuizDatabaseHelper.COLUMN_OPTION_QUESTION_ID, questionId);
        values.put(QuizDatabaseHelper.COLUMN_OPTION_TEXT, optionText);
        values.put(QuizDatabaseHelper.COLUMN_OPTION_CATEGORY_ID, categoryId);
        values.put(QuizDatabaseHelper.COLUMN_OPTION_POINT_VALUE, pointValue);
        values.put(QuizDatabaseHelper.COLUMN_OPTION_DISPLAY_ORDER, displayOrder);
        return db.insert(QuizDatabaseHelper.TABLE_OPTIONS, null, values);
    }

    public int updateOption(int optionId, String optionText, String categoryKey, int pointValue, int displayOrder) {
        long categoryId = getCategoryIdByKey(categoryKey);
        if (categoryId == -1L) {
            return 0;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(QuizDatabaseHelper.COLUMN_OPTION_TEXT, optionText);
        values.put(QuizDatabaseHelper.COLUMN_OPTION_CATEGORY_ID, categoryId);
        values.put(QuizDatabaseHelper.COLUMN_OPTION_POINT_VALUE, pointValue);
        values.put(QuizDatabaseHelper.COLUMN_OPTION_DISPLAY_ORDER, displayOrder);
        return db.update(
                QuizDatabaseHelper.TABLE_OPTIONS,
                values,
                QuizDatabaseHelper.COLUMN_OPTION_ID + " = ?",
                new String[]{String.valueOf(optionId)}
        );
    }

    public int deleteOption(int optionId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                QuizDatabaseHelper.TABLE_OPTIONS,
                QuizDatabaseHelper.COLUMN_OPTION_ID + " = ?",
                new String[]{String.valueOf(optionId)}
        );
    }

    public int updateAttemptAnswer(long attemptId, int questionId, int optionId, int awardedPoints) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(QuizDatabaseHelper.COLUMN_ATTEMPT_ANSWER_OPTION_ID, optionId);
        values.put(QuizDatabaseHelper.COLUMN_ATTEMPT_ANSWER_AWARDED_POINTS, awardedPoints);
        return db.update(
                QuizDatabaseHelper.TABLE_ATTEMPT_ANSWERS,
                values,
                QuizDatabaseHelper.COLUMN_ATTEMPT_ANSWER_ATTEMPT_ID + " = ? AND "
                        + QuizDatabaseHelper.COLUMN_ATTEMPT_ANSWER_QUESTION_ID + " = ?",
                new String[]{String.valueOf(attemptId), String.valueOf(questionId)}
        );
    }

    public int deleteQuizAttempt(long attemptId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                QuizDatabaseHelper.TABLE_QUIZ_ATTEMPTS,
                QuizDatabaseHelper.COLUMN_ATTEMPT_ID + " = ?",
                new String[]{String.valueOf(attemptId)}
        );
    }

    private long getCategoryIdByKey(String categoryKey) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                QuizDatabaseHelper.TABLE_CATEGORIES,
                new String[]{QuizDatabaseHelper.COLUMN_CATEGORY_ID},
                QuizDatabaseHelper.COLUMN_CATEGORY_KEY + " = ?",
                new String[]{categoryKey},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(QuizDatabaseHelper.COLUMN_CATEGORY_ID));
            }
        } finally {
            cursor.close();
        }

        return -1L;
    }
}
