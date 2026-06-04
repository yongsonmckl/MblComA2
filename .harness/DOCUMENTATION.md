# Assignment Cover Sheet

## Course Information

- Course Title: Mobile Computing
- Course Code: CSC 1214
- Assignment No: 2
- Session: April 2026
- Project Title: Connecting Career Quiz App to SQLite Database

## Student Information

- Name: WYongson
- Student ID: 30860

## Declaration

1. This documentation is written to explain the implemented Android SQLite solution for the Career Quiz application.
2. The final project was implemented with understanding of the app flow, SQLite schema, CRUD operations, and Android integration.
3. AI was used as a planning, review, and documentation aid, not as a blind replacement for subject understanding.

---

# Table of Contents

1. Overview
2. Assignment Requirement Mapping
3. Database Design
4. SQLite Helper Class
5. Database Operations (CRUD)
6. User Interface Integration
7. How To Run
8. Sample Input and Output
9. Testing and Debugging
10. Rubric Alignment
11. Strengths and Limitations
12. AI Usage Reflection
13. References

---

# 1. Overview

This assignment extends the existing Career Quiz Android application by replacing hardcoded quiz data and `SharedPreferences` result storage with a local SQLite database solution.

The objective of the implementation is to:

- store quiz questions in SQLite
- store answer options in SQLite
- store categories and points in SQLite
- store completed quiz attempts and answer selections in SQLite
- connect the user interface to database reads and writes

The final solution follows the Chapter 13 lecture style by using:

- `SQLiteOpenHelper`
- `SQLiteDatabase`
- `ContentValues`
- `Cursor`
- `onCreate()` and `onUpgrade()`

The existing app flow was preserved:

- `HomeFragment` shows the last saved result
- `QuizFragment` loads questions and options
- `ResultFragment` displays the final archetype

---

# 2. Assignment Requirement Mapping

## Create a Database Helper Class

Implemented in:

- `app/src/main/java/com/mckl/assignment1/database/QuizDatabaseHelper.java`

Coverage:

- extends `SQLiteOpenHelper`
- implements `onCreate()`
- implements `onUpgrade()`
- enables foreign keys in `onConfigure()`
- creates all required tables
- seeds initial categories, questions, and options

## Define the Database Schema

The database schema was designed to explicitly store:

- questions
- options
- categories
- points
- quiz attempts
- selected answers

This is stronger than the original hardcoded array design because the relationships are now stored as real data rather than assumptions in Java code.

## Implement Database Operations

Implemented in:

- `app/src/main/java/com/mckl/assignment1/repository/QuizRepository.java`

Coverage:

- Create: insert categories, questions, options, quiz attempts, attempt answers
- Read: load questions, options, latest result, attempt history
- Update: update questions, options, categories, attempt answers, final attempt result
- Delete: delete questions, options, categories, quiz attempts

## Integrate Database Operations with User Interface

Coverage:

- `HomeFragment` now reads the latest saved result from SQLite
- `QuizFragment` now shows questions and options from SQLite
- `QuizViewModel` now records selected answers and saves final results into SQLite
- `ResultFragment` continues to display the final category after quiz completion

## Test and Debug the App

Verification completed:

- project assembled successfully using `:app:assembleDebug`
- SQLite-backed quiz flow compiles and integrates into the existing Android app

---

# 3. Database Design

## Table 1: `categories`

Purpose:

- stores the four career categories used by the quiz

Columns:

- `category_id INTEGER PRIMARY KEY AUTOINCREMENT`
- `category_key TEXT NOT NULL UNIQUE`
- `title TEXT NOT NULL`
- `field TEXT NOT NULL`
- `description TEXT NOT NULL`
- `examples TEXT NOT NULL`

Notes:

- `category_key` matches the enum values such as `ARCHITECT`
- this keeps the display logic compatible with `CareerArchetype`

## Table 2: `questions`

Purpose:

- stores each quiz question

Columns:

- `question_id INTEGER PRIMARY KEY AUTOINCREMENT`
- `question_text TEXT NOT NULL`
- `display_order INTEGER NOT NULL UNIQUE`

## Table 3: `options`

Purpose:

- stores all answer options for each question
- links each option to one category and one point value

Columns:

- `option_id INTEGER PRIMARY KEY AUTOINCREMENT`
- `question_id INTEGER NOT NULL`
- `option_text TEXT NOT NULL`
- `category_id INTEGER NOT NULL`
- `point_value INTEGER NOT NULL DEFAULT 1`
- `display_order INTEGER NOT NULL`

Relationships:

- `question_id` -> `questions.question_id`
- `category_id` -> `categories.category_id`

## Table 4: `quiz_attempts`

Purpose:

- stores each completed user quiz result

Columns:

- `attempt_id INTEGER PRIMARY KEY AUTOINCREMENT`
- `final_category_id INTEGER`
- `completed_at TEXT`

Relationship:

- `final_category_id` -> `categories.category_id`

## Table 5: `attempt_answers`

Purpose:

- stores which option the user selected for each question in an attempt

Columns:

- `attempt_answer_id INTEGER PRIMARY KEY AUTOINCREMENT`
- `attempt_id INTEGER NOT NULL`
- `question_id INTEGER NOT NULL`
- `option_id INTEGER NOT NULL`
- `awarded_points INTEGER NOT NULL`

Relationships:

- `attempt_id` -> `quiz_attempts.attempt_id`
- `question_id` -> `questions.question_id`
- `option_id` -> `options.option_id`

## Design Justification

This schema was chosen because it aligns better with the question paper and rubric than a flat question table.

Advantages:

- categories are explicit database records
- options explicitly store category and point mapping
- scoring can be audited from saved answers
- CRUD operations can be demonstrated on meaningful entities
- the app remains extendable for future quiz maintenance

---

# 4. SQLite Helper Class

The helper class is `QuizDatabaseHelper`.

Responsibilities:

- create the SQLite database file
- create all five tables
- configure foreign key support
- drop and recreate the schema during upgrades
- seed the initial quiz content on first creation

## Seeded Data

The database is seeded with:

- 4 categories:
  - Architect
  - Visionary
  - Guardian
  - Captain
- 10 quiz questions
- 4 options per question
- 1 point for each option

This preserves the original quiz content while moving the data source from Java arrays into SQLite.

---

# 5. Database Operations (CRUD)

## Create

Examples implemented in `QuizRepository`:

- `insertCategory(...)`
- `insertQuestion(...)`
- `insertOption(...)`
- `createQuizAttempt()`
- `insertAttemptAnswer(...)`

In the live app flow:

- a quiz attempt is created when the user starts answering
- each selected answer is inserted into `attempt_answers`
- the final result is saved back to `quiz_attempts`

## Read

Examples implemented:

- `getQuestions()`
- `getOptionsForQuestion(int questionId)`
- `getLatestQuizAttempt()`
- `getAllQuizAttempts()`

In the live app flow:

- the quiz questions are loaded from SQLite
- the home screen reads the latest saved result from SQLite

## Update

Examples implemented:

- `updateCategory(...)`
- `updateQuestion(...)`
- `updateOption(...)`
- `updateAttemptAnswer(...)`
- `updateQuizAttemptResult(...)`

In the live app flow:

- the final category and completion date are written back to the current attempt after the quiz finishes

## Delete

Examples implemented:

- `deleteCategory(...)`
- `deleteQuestion(int questionId)`
- `deleteOption(int optionId)`
- `deleteQuizAttempt(long attemptId)`

Note:

- the current UI does not expose on-screen delete buttons
- however, the data layer supports delete operations as required for CRUD completeness

---

# 6. User Interface Integration

## Home Screen

File:

- `app/src/main/java/com/mckl/assignment1/ui/HomeFragment.java`

Change:

- previously used `SharedPreferences`
- now uses `HomeViewModel` and `QuizRepository` to read the latest SQLite result

## Quiz Screen

File:

- `app/src/main/java/com/mckl/assignment1/ui/QuizFragment.java`

Change:

- previously displayed hardcoded repository questions
- now displays SQLite-loaded questions and options

## Quiz Logic

File:

- `app/src/main/java/com/mckl/assignment1/viewmodel/QuizViewModel.java`

Change:

- creates a quiz attempt in SQLite
- saves each selected answer into `attempt_answers`
- calculates the final result
- updates the attempt with the final category and completion time

## Result Screen

File:

- `app/src/main/java/com/mckl/assignment1/ui/ResultFragment.java`

Change:

- no major database logic added here
- the fragment still displays the result based on the final archetype key

This was intentionally kept stable to avoid unnecessary UI changes.

---

# 7. How To Run

1. Open the project in Android Studio.
2. Let Gradle sync the project.
3. Run the app on an emulator or Android device.
4. On the home screen, press `Start Assessment`.
5. Answer all 10 questions.
6. View the result screen.
7. Return to the home screen and confirm that the last result is now shown from SQLite.

Build verification command used during implementation:

```powershell
.\gradlew.bat :app:assembleDebug
```

---

# 8. Sample Input and Output

## Sample Flow

User actions:

1. Open app.
2. Start assessment.
3. Answer all questions by selecting one option per question.
4. Reach final result.
5. Return to home screen.

## Expected Output

- quiz questions are displayed one by one
- progress indicator updates with each answer
- final personality archetype is displayed
- latest result is shown on the home screen with date and time

## Example Data Behavior

For each question:

- the selected option maps to one category
- the selected option adds `1` point to that category
- the selected answer is stored in `attempt_answers`

At the end:

- the highest scoring category is selected
- the result is saved into `quiz_attempts`

---

# 9. Testing and Debugging

## Functional Checks

- app launches without SQLite crash
- database creates successfully on first run
- categories, questions, and options are seeded correctly
- questions load from SQLite instead of hardcoded arrays
- answer clicks save data into SQLite-backed flow
- final result still navigates correctly to `ResultFragment`
- latest result appears on `HomeFragment`

## Debugging Notes

- the implementation was compiled using Gradle assemble
- foreign keys were enabled in the helper class to strengthen integrity
- cursor usage was kept explicit and closed after read operations

## Build Result

- `:app:assembleDebug` completed successfully

---

# 10. Rubric Alignment

## Database Implementation

### Database Schema

The schema is logically designed with separate tables for:

- categories
- questions
- options
- quiz attempts
- attempt answers

This supports efficient data organization and reflects the actual quiz relationships clearly.

### Database Helper Class

The helper class fully covers:

- database creation
- upgrade handling
- table definition
- seed insertion
- foreign key configuration

## Database Operations

### CRUD Operations

The repository implements create, read, update, and delete methods across the main entities.

### Data Consistency

The solution includes:

- primary keys
- foreign keys
- `NOT NULL` constraints
- uniqueness constraints
- controlled question ordering
- safe cursor closure

## Database Integration with User Interface

The app is now integrated with SQLite through the real quiz flow:

- question loading
- answer recording
- point accumulation
- latest result retrieval

This is no longer a partially connected data layer.

## Remaining Caveat

The current UI clearly demonstrates:

- Create
- Read

The repository also supports:

- Update
- Delete

If the lecturer expects visible UI screens for update/delete, an extra admin or maintenance screen may still improve presentation. However, the project already contains complete CRUD support in the data layer.

---

# 11. Strengths and Limitations

## Strengths

- follows the Chapter 13 SQLite approach directly
- replaces hardcoded data with a structured local database
- stores categories, options, points, and attempts explicitly
- keeps the existing app flow stable
- provides repository-level CRUD completeness

## Limitations

- update/delete actions are not yet exposed as dedicated on-screen user features
- the quiz still uses a fixed 4-option structure in the UI
- the solution is suitable for local mobile storage, not large-scale multi-user systems

---

# 12. AI Usage Reflection

AI was used as a planning, review, and documentation assistant during this assignment. It helped translate the question paper and rubric into a concrete implementation plan, check whether the current code structure could support SQLite integration, and identify where the original app was still relying on hardcoded data and `SharedPreferences`.

AI was also used to review whether the schema should explicitly store categories, options, and points to better satisfy the rubric. That guidance was useful because the original implementation only had categories and points implicitly through enum ordering and an in-memory score array. The final implementation was still validated against the actual Android code structure, the lecture notes, and successful Gradle build output.

I did not use AI as a substitute for understanding the app. The final code changes required understanding fragment flow, view models, repository responsibilities, SQLite table relationships, and how scoring works in the quiz. AI was used to improve organization and completeness, not to blindly copy an answer.

---

# 13. References

1. CSC 1214 Chapter 13 Notes: Developing Android Apps (Database).
2. Assignment question paper: Connecting Career Quiz App to SQLite database.
3. Android Developers. SQLite and local data storage documentation.
