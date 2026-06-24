# Mobile Computing Assignment 2

This project is a Career Quiz Android app for my Mobile Computing Assignment 2. It was updated to use a SQLite database instead of relying on hardcoded arrays for the quiz content and stored results.

The database implementation stores the main quiz data, including categories, questions, options, point values, quiz attempts, and selected answers. A custom `SQLiteOpenHelper` class was created to define the schema, create the tables, seed the initial data, and handle database upgrades. Repository methods were also added so the app can perform CRUD operations and connect the database layer to the existing app flow.

The app now reads quiz questions and options from SQLite, records the user's answers during an attempt, calculates the final result, and saves the latest completed result for display on the home screen. The user interface design was kept mostly the same, while the backend data handling was moved into the database layer.

## Main Features

- SQLite database integration for quiz content and stored attempts
- Custom database helper using `SQLiteOpenHelper`
- Tables for categories, questions, options, quiz attempts, and attempt answers
- CRUD methods for key database entities
- Persistent storage of the user's latest quiz result
- Existing Career Quiz flow connected to database operations

## Project Structure

- `app/src/main/java/com/mckl/assignment1/database/`
  Contains the SQLite helper and schema setup.
- `app/src/main/java/com/mckl/assignment1/repository/`
  Contains the repository methods used to read and write quiz data.
- `app/src/main/java/com/mckl/assignment1/viewmodel/`
  Connects the UI layer to the repository and database logic.
- `.harness/`
  Contains planning and supporting documentation for the assignment work.

## How to Run

1. Open the project in Android Studio.
2. Allow Gradle to sync.
3. Run the app on an emulator or Android device.
4. Start the assessment and answer the quiz questions.
5. View the final career result and the saved latest result on the home page.

## Notes

- The visible app name is `Assignment 2`.
- The Java package path still uses `com.mckl.assignment1` because the original source structure was kept.
- The focus of this assignment update is the SQLite database implementation and integration with the existing app.
