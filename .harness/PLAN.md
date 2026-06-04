# SQLite Integration Plan

## Feasibility
Yes. The current codebase is sufficient to implement the revised database scope without rewriting the app architecture.

What already exists and can be reused:

- a stable 3-screen flow: `HomeFragment` -> `QuizFragment` -> `ResultFragment`
- a repository layer: `QuizRepository`
- a view model layer: `QuizViewModel`
- a clear category model: `CareerArchetype`
- an existing point system in memory: `int[] scores`

What needs to change:

- move hardcoded quiz content into SQLite
- convert the implicit category and point logic into explicit database tables/columns
- replace `SharedPreferences` result storage with SQLite
- add enough CRUD coverage to satisfy the assignment rubric

## Objective
Implement a Chapter 13 style SQLite solution for the Career Quiz app using:

- `SQLiteOpenHelper`
- `SQLiteDatabase`
- `ContentValues`
- `Cursor`
- `onCreate()` and `onUpgrade()`

The implementation must satisfy the assignment paper and support the rubric areas:

- database schema design
- helper class implementation
- CRUD operations
- data consistency and integrity
- seamless integration with the existing UI

## Current App Understanding
The current app already contains the domain concepts required by the assignment, but they are mostly hardcoded.

Current behavior:

- `QuizRepository.getQuestions()` builds the full quiz from hardcoded arrays
- `QuizViewModel` keeps category points in `int[] scores`
- each answer adds one point to one archetype using `scores[optionIndex]++`
- `CareerArchetype` acts as the current category definition
- the latest result is stored in `SharedPreferences`

Current data status:

- categories exist implicitly through enum values
- points exist only in memory
- questions and options are hardcoded
- no persistent answer-level data exists

## Chapter 13 Alignment
The notes point to classic SQLite usage, not Room. The implementation should therefore visibly use:

- a custom helper class extending `SQLiteOpenHelper`
- SQL table creation with `db.execSQL(...)`
- `ContentValues` for inserts and updates
- `Cursor` for reads
- `getReadableDatabase()` and `getWritableDatabase()`
- explicit close handling for cursors

Room dependencies are present in Gradle, but this assignment should follow the Chapter 13 syntax and lab style instead of switching to Room.

## Revised Database Scope
To match the question paper more closely, the schema should store:

- questions
- options
- categories
- user points
- user attempts / results

### Table 1: `categories`
Purpose: persist the personality/career categories instead of relying only on enum position.

Suggested columns:

- `category_id INTEGER PRIMARY KEY AUTOINCREMENT`
- `category_key TEXT NOT NULL UNIQUE`
- `title TEXT NOT NULL`
- `field TEXT NOT NULL`
- `description TEXT NOT NULL`
- `examples TEXT NOT NULL`

Notes:

- `category_key` should match enum names such as `ARCHITECT`
- the enum can remain in code for display mapping and safe conversion, but SQLite becomes the source of record

### Table 2: `questions`
Purpose: store all quiz questions.

Suggested columns:

- `question_id INTEGER PRIMARY KEY AUTOINCREMENT`
- `question_text TEXT NOT NULL`
- `display_order INTEGER NOT NULL UNIQUE`

Notes:

- `display_order` keeps the quiz sequence deterministic

### Table 3: `options`
Purpose: store answer options separately and explicitly connect them to categories and points.

Suggested columns:

- `option_id INTEGER PRIMARY KEY AUTOINCREMENT`
- `question_id INTEGER NOT NULL`
- `option_text TEXT NOT NULL`
- `category_id INTEGER NOT NULL`
- `point_value INTEGER NOT NULL DEFAULT 1`
- `display_order INTEGER NOT NULL`

Relationships:

- `question_id` references `questions.question_id`
- `category_id` references `categories.category_id`

Notes:

- this directly satisfies the requirement to store options, categories, and points
- each selected option contributes `point_value` to its linked category

### Table 4: `quiz_attempts`
Purpose: store each completed quiz result.

Suggested columns:

- `attempt_id INTEGER PRIMARY KEY AUTOINCREMENT`
- `final_category_id INTEGER NOT NULL`
- `completed_at TEXT NOT NULL`

Relationships:

- `final_category_id` references `categories.category_id`

### Table 5: `attempt_answers`
Purpose: store what the user selected during an attempt so scoring is auditable and updateable.

Suggested columns:

- `attempt_answer_id INTEGER PRIMARY KEY AUTOINCREMENT`
- `attempt_id INTEGER NOT NULL`
- `question_id INTEGER NOT NULL`
- `option_id INTEGER NOT NULL`
- `awarded_points INTEGER NOT NULL`

Relationships:

- `attempt_id` references `quiz_attempts.attempt_id`
- `question_id` references `questions.question_id`
- `option_id` references `options.option_id`

Why this matters:

- supports create/read/update/delete at a meaningful data level
- helps demonstrate data consistency and point tracking
- makes the score derivation explainable in the report

## Schema Quality Rationale
This schema is stronger than the original flat `option_0` to `option_3` design because it better matches the assignment wording and rubric.

Benefits:

- categories are explicit data, not only enum assumptions
- points are explicit data, not only `scores[]` memory state
- options are linked to both questions and categories
- attempts can be inspected and validated later
- CRUD can be demonstrated on real entities

## Planned Code Changes

### 1. Create the SQLite helper class
Add `com.mckl.assignment1.database.QuizDatabaseHelper`.

Responsibilities:

- define database name and version
- define table and column constants
- implement `onCreate(SQLiteDatabase db)`
- implement `onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)`
- enable foreign key support where appropriate
- create all required tables
- seed initial categories, questions, and options

### 2. Seed the initial database
Move the current hardcoded quiz content into database seeding logic.

Seed data must include:

- 4 categories from `CareerArchetype`
- 10 existing questions
- 4 options per question
- option-to-category mapping
- point values, initially `1`

Recommended approach:

- insert categories first
- insert each question next
- insert options with linked `category_id` and `point_value`

### 3. Update the models
Keep the existing UI-facing models simple, but add database-friendly models where needed.

Likely additions:

- `Question` may gain `questionId`
- add `QuestionOption` model for option rows
- add `QuizAttempt` model
- add `AttemptAnswer` model if needed for repository clarity

The `CareerArchetype` enum should stay, but it should no longer be the only place where categories exist.

### 4. Refactor `QuizRepository` into the main data access layer
`QuizRepository` should handle all SQLite interactions.

Core read methods:

- `List<Question> getQuestions()`
- `List<QuestionOption> getOptionsForQuestion(int questionId)`
- `QuizAttempt getLatestQuizAttempt()`
- `List<QuizAttempt> getAllQuizAttempts()`
- `CareerArchetype calculateResultFromTallies(...)` or equivalent helper

Core create methods:

- `long insertQuestion(...)`
- `long insertOption(...)`
- `long insertCategory(...)`
- `long createQuizAttempt(...)`
- `long insertAttemptAnswer(...)`

Core update methods:

- `int updateQuestion(...)`
- `int updateOption(...)`
- `int updateCategory(...)`
- `int updateAttemptAnswer(...)`

Core delete methods:

- `int deleteQuestion(int questionId)`
- `int deleteOption(int optionId)`
- `int deleteQuizAttempt(int attemptId)`

These methods do not all need public UI buttons immediately, but they should exist to satisfy the CRUD requirement and make the data layer complete.

### 5. Replace in-memory-only scoring with database-backed answer tracking
The app can still use in-memory tallies during the live quiz for responsive UI behavior, but the final implementation should also persist the answer trail.

Recommended flow:

1. user starts a quiz attempt
2. repository creates a pending or active attempt record
3. each answer selected is written to `attempt_answers`
4. each option already contains the category and point value
5. final result is derived from stored selections or synchronized tallies
6. completed attempt stores the winning category

This directly addresses the paper's statement that the user's point should be updated in the database.

### 6. Update `QuizViewModel`
Required changes:

- initialize repository with `Application` or `Context`
- load questions from SQLite instead of hardcoded arrays
- create and track the current quiz attempt
- save selected answers to SQLite
- calculate the final category using the stored category/point logic
- stop writing the last result to `SharedPreferences`

The current `LiveData` structure can remain mostly intact so the UI does not need major restructuring.

### 7. Update `QuizFragment`
Required changes:

- bind question text and options from database-backed models
- on answer click, call the view model so the database records the chosen option
- continue progressing through the quiz as it does now

The UI flow should remain familiar, but the backing data will now come from SQLite.

### 8. Update `HomeFragment`
Required changes:

- replace `SharedPreferences` reads with SQLite reads
- display the latest saved attempt result and completion time

Preferred approach:

- use a view model or repository-backed retrieval
- avoid raw SQL inside the fragment

### 9. Keep `ResultFragment` mostly intact
`ResultFragment` can keep its current rendering logic.

Keep:

- enum-based display mapping
- bundle navigation pattern
- result-based color changes

Only the source of the result changes, not the screen behavior.

### 10. Demonstrate CRUD without breaking the quiz app
The existing quiz UI naturally demonstrates:

- `Create`: saving attempts and answers
- `Read`: loading questions, options, categories, and latest result

To satisfy full CRUD more clearly, the plan should include one of these approaches:

- add lightweight admin/test methods in the repository and call them in controlled tests or seeded debug flows
- add a simple internal management screen later for question/option maintenance

For this project stage, repository-level CRUD methods are enough to plan and implement. If the lecturer expects visible UI CRUD, that should be confirmed before final submission.

## Data Consistency and Integrity
To align with the rubric, the implementation should explicitly include:

- primary keys on all tables
- foreign keys for linked tables
- `NOT NULL` constraints where appropriate
- unique constraints on keys such as `category_key` and question order
- validation before insert/update
- safe cursor usage and closure

Recommended consistency handling:

- insert seeded categories before questions/options
- when saving a quiz result, wrap related writes in a transaction if multiple tables are updated together
- reject invalid option/category references
- ensure each option belongs to exactly one question and one category

## SQL / API Patterns To Follow
The implementation should visibly reflect Chapter 13 syntax.

Patterns to use:

- `class QuizDatabaseHelper extends SQLiteOpenHelper`
- `db.execSQL("CREATE TABLE ...")`
- `ContentValues values = new ContentValues();`
- `values.put(...)`
- `db.insert(...)`
- `db.update(...)`
- `db.delete(...)`
- `Cursor cursor = db.rawQuery(...)` or `db.query(...)`
- `cursor.moveToFirst()`
- `cursor.getString(...)`
- `cursor.getInt(...)`
- `cursor.close()`

## Execution Order
Implement in this order:

1. Create `.harness/AGENTS.md`.
2. Add the SQLite helper and schema constants.
3. Create and seed `categories`, `questions`, and `options`.
4. Create `quiz_attempts` and `attempt_answers`.
5. Refactor `QuizRepository` to become the SQLite CRUD layer.
6. Update `QuizViewModel` to use database-backed reads and writes.
7. Replace `SharedPreferences` last-result logic.
8. Verify quiz flow still works end to end.
9. Validate CRUD methods and integrity handling.
10. Remove obsolete hardcoded persistence logic.

## Testing Checklist
After implementation, verify the following:

- app launches without SQLite errors
- first launch creates all tables correctly
- first launch seeds categories, questions, and options correctly
- questions and options are read from SQLite, not hardcoded runtime arrays
- answering a question stores the selected option and points correctly
- final result still matches expected archetype outcomes
- latest result displays correctly on `HomeFragment`
- repository CRUD methods for major entities behave correctly
- invalid data does not silently corrupt scoring
- cursors and related resources are handled safely

## Risks and Notes

- the current UI does not naturally expose visible update/delete screens for all entities
- repository-level CRUD is technically sufficient for implementation, but visible UI CRUD may still be expected by a strict marker
- using a normalized schema increases code complexity, but it better fits the paper and rubric than a flat four-option table
- Room should be avoided for this assignment unless the requirement changes

## Recommended Outcome
End state should be:

- a custom SQLite helper class that is complete and rubric-aligned
- a database schema that explicitly stores questions, options, categories, points, and attempts
- repository methods covering create, read, update, and delete
- a quiz flow that reads from and writes to SQLite
- a result system that persists user points/answers instead of relying only on memory or `SharedPreferences`
- implementation style that matches the Chapter 13 notes and assignment wording closely
