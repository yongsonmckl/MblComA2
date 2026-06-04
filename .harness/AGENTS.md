# AGENTS

## Purpose
This folder documents the implementation rules for migrating the Career Quiz app from hardcoded data and `SharedPreferences` to a Chapter 13 style SQLite solution.

## Project Goal
Implement a local SQLite database for the existing Android app so that the app stores and retrieves:

- categories
- questions
- options
- points
- quiz attempts
- selected answers

The implementation must remain compatible with the current Java app structure and the assignment rubric.

## Architecture Rules

- Use classic Android SQLite APIs, not Room, as the primary implementation approach.
- Create a custom helper class extending `SQLiteOpenHelper`.
- Keep `QuizRepository` as the main data access layer.
- Keep UI code thin; fragments should not contain raw SQL logic.
- Preserve the current app flow unless a database requirement forces a justified change.

## Required SQLite Patterns

- Define schema constants in the helper class.
- Create tables in `onCreate(SQLiteDatabase db)`.
- Handle schema resets or upgrades in `onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)`.
- Use `ContentValues` for insert and update operations.
- Use `Cursor` for read operations.
- Close cursors after use.
- Prefer `getReadableDatabase()` for reads and `getWritableDatabase()` for writes.

## Data Design Rules

- Persist categories explicitly in SQLite, even if `CareerArchetype` remains in code.
- Store questions and options in separate tables.
- Each option must link to exactly one category and one question.
- Store point values explicitly in the database.
- Persist quiz attempts and the user's selected answers.
- Use primary keys, foreign keys, `NOT NULL`, and uniqueness constraints where appropriate.

## Integration Rules

- `QuizViewModel` should load quiz data from the repository, not hardcoded arrays.
- `HomeFragment` should read the latest result from SQLite, not `SharedPreferences`.
- `QuizFragment` should trigger database-backed answer recording through the view model.
- `ResultFragment` may continue using enum-based display logic if the final category key remains compatible.

## CRUD Expectation
The repository should expose complete CRUD coverage for the relevant entities, even if the initial UI only exercises part of it directly.

Minimum expected entity coverage:

- categories
- questions
- options
- quiz attempts
- attempt answers

## Testing Rules

- Verify database creation and seeding on first launch.
- Verify question and option retrieval from SQLite.
- Verify selected answers store the correct category and point value.
- Verify the final result remains correct.
- Verify latest-result retrieval on the home screen.
- Verify update and delete methods behave safely for the entities they target.

## Scope Boundaries

- Do not switch the implementation to Room unless the requirement changes.
- Do not remove the current UI navigation flow unless necessary.
- Do not leave categories or points only as implicit hardcoded logic.

## Working Principle
When implementation decisions are ambiguous, prefer the option that:

1. matches the Chapter 13 notes more closely,
2. improves rubric coverage,
3. keeps the current app behavior stable,
4. makes the data model easier to explain in the final report.
