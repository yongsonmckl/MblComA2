# UNDERSTANDING

## Scope

This document explains the files and functions that I wrote or substantially rewrote for the SQLite integration work.

Line numbers in this document refer to the current live files in the workspace on `2026-06-22`.

This document intentionally focuses on the authored database-related files and the UI/view-model code that was changed to connect the app to SQLite:

- `app/src/main/java/com/mckl/assignment1/database/QuizDatabaseHelper.java`
- `app/src/main/java/com/mckl/assignment1/repository/QuizRepository.java`
- `app/src/main/java/com/mckl/assignment1/viewmodel/QuizViewModel.java`
- `app/src/main/java/com/mckl/assignment1/viewmodel/HomeViewModel.java`
- `app/src/main/java/com/mckl/assignment1/model/Question.java`
- `app/src/main/java/com/mckl/assignment1/model/QuestionOption.java`
- `app/src/main/java/com/mckl/assignment1/model/QuizAttempt.java`
- `app/src/main/java/com/mckl/assignment1/ui/HomeFragment.java`
- `app/src/main/java/com/mckl/assignment1/ui/QuizFragment.java`

The visible project/app naming is now `Assignment 2`, but the Java package paths in the live source still remain under `com.mckl.assignment1`. This document uses the real file paths exactly as they exist in code.

Files that I did not meaningfully author, such as `MainActivity.java`, `CareerArchetype.java`, and `ResultFragment.java`, are not expanded here because the request was specifically about the functions I wrote.

---

## 1. `QuizDatabaseHelper.java`

**Path**

- `app/src/main/java/com/mckl/assignment1/database/QuizDatabaseHelper.java`

**Purpose**

- This class is the schema owner for the SQLite database.
- It defines all table names and column names.
- It creates the schema on first launch.
- It recreates the schema on upgrade.
- It seeds the initial quiz data.

**What makes it different from the simplest Chapter 13 slide example**

- It does not only create one simple table.
- It creates five related tables with foreign keys.
- It seeds normalized question and option data instead of using a flat table.
- It introduces explicit category and answer-history tables, which is more advanced than the simplest lecture demo.

### Constants and Schema Fields

**Lines 14-48**

These are not functions, but they matter because they define the database vocabulary used everywhere else.

- `14-15`: define database file name and version.
- `17-23`: define the `categories` table and its columns.
- `25-28`: define the `questions` table and its columns.
- `30-36`: define the `options` table and its columns.
- `38-41`: define the `quiz_attempts` table and its columns.
- `43-48`: define the `attempt_answers` table and its columns.

**Why this matters**

- Every later query in the repository depends on these exact names.
- Centralizing them avoids string duplication across the codebase.

### Function: `QuizDatabaseHelper(Context context)`

**Lines:** `50-52`

**What it does**

- Creates the helper instance and binds it to the `career_quiz.db` file.

**How it works, line by line**

- `50`: constructor declaration. Accepts Android `Context`.
- `51`: calls the parent `SQLiteOpenHelper` constructor with:
  - the passed `context`
  - database name `career_quiz.db`
  - `null` factory
  - database version `1`
- `52`: constructor ends.

**Why it exists**

- Android expects the helper to identify one logical database file and a schema version.

**How it strays from the slides**

- It does not stray much here. This is standard `SQLiteOpenHelper` usage.

### Function: `onConfigure(SQLiteDatabase db)`

**Lines:** `55-58`

**What it does**

- Enables foreign key enforcement.

**How it works, line by line**

- `55`: override declaration.
- `56`: calls the parent implementation first.
- `57`: enables foreign key constraints on this database connection.
- `58`: method ends.

**Why it exists**

- Without this line, the foreign key definitions in `onCreate()` may exist in the schema but not actually be enforced during runtime on Android SQLite.

**How it strays from the slides**

- This is slightly more advanced than the basic lecture example.
- The slides often focus on `onCreate()` and `onUpgrade()`, but real integrity enforcement requires this extra setup.

### Function: `onCreate(SQLiteDatabase db)`

**Lines:** `61-115`

**What it does**

- Creates all five tables:
  - `categories`
  - `questions`
  - `options`
  - `quiz_attempts`
  - `attempt_answers`
- Then seeds the initial data.

**How it works, line by line**

- `61`: override declaration.
- `62-69`: executes SQL to create `categories`.
  - `63`: `category_id` as auto-increment primary key.
  - `64`: `category_key` is required and unique.
  - `65-68`: all user-facing category fields are required text fields.
- `71-75`: executes SQL to create `questions`.
  - `72`: `question_id` primary key.
  - `73`: question text is required.
  - `74`: `display_order` is required and unique.
- `77-89`: executes SQL to create `options`.
  - `78`: `option_id` primary key.
  - `79`: every option must belong to a question.
  - `80`: option text is required.
  - `81`: every option must belong to a category.
  - `82`: every option carries a point value, default `1`.
  - `83`: stores the button ordering.
  - `84-85`: foreign key from option to question, with `ON DELETE CASCADE`.
  - `86-87`: foreign key from option to category.
  - `88`: unique pair `(question_id, display_order)` so the same question cannot have two options in the same slot.
- `91-97`: executes SQL to create `quiz_attempts`.
  - `92`: `attempt_id` primary key.
  - `93`: `final_category_id` is nullable at creation time.
  - `94`: `completed_at` is nullable at creation time.
  - `95-96`: foreign key from final result to category.
- `99-112`: executes SQL to create `attempt_answers`.
  - `100`: `attempt_answer_id` primary key.
  - `101`: every answer must belong to an attempt.
  - `102`: every answer must reference a question.
  - `103`: every answer must reference an option.
  - `104`: awarded points are mandatory.
  - `105-106`: foreign key from answer to attempt, with `ON DELETE CASCADE`.
  - `107-108`: foreign key from answer to question.
  - `109-110`: foreign key from answer to option.
  - `111`: unique pair `(attempt_id, question_id)` so one attempt cannot store two answers for the same question.
- `114`: calls `seedDatabase(db)` after schema creation.
- `115`: method ends.

**Why it exists**

- This is the central schema creation step for first launch.

**How it strays from the slides**

- This is the most obvious deviation from the simplest Chapter 13 pattern.
- The chapter example usually shows one helper and one simple table.
- Here, the schema is normalized and relational:
  - options are separate from questions
  - points are stored per option
  - attempts are stored separately from answers
- This is more complex, but it matches the assignment wording more closely.

### Function: `onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)`

**Lines:** `118-125`

**What it does**

- Drops all five tables and recreates them.

**How it works, line by line**

- `118`: override declaration.
- `119`: drops `attempt_answers` first.
- `120`: drops `quiz_attempts`.
- `121`: drops `options`.
- `122`: drops `questions`.
- `123`: drops `categories`.
- `124`: calls `onCreate(db)` to rebuild the schema and seed data.
- `125`: method ends.

**Why it exists**

- Android requires upgrade logic whenever a schema version may change.

**How it strays from the slides**

- It still follows the basic lecture approach.
- The main difference is just the number of related tables that must be dropped in reverse dependency order.

### Function: `seedDatabase(SQLiteDatabase db)`

**Lines:** `127-229`

**What it does**

- Inserts the 4 categories and all 10 seeded quiz questions with 4 options each.

**How it works, line by line**

- `127`: private helper declaration.
- `128`: begins a database transaction.
- `129`: opens `try` block.
- `130-133`: inserts the four `CareerArchetype` rows and captures their generated IDs.
- `135-223`: repeatedly calls `insertQuestionWithOptions(...)` for all 10 questions.
  - Each call passes:
    - the question order
    - the question text
    - the 4 answer texts
    - the 4 category IDs in the same order
- `225`: marks the transaction successful if all inserts completed.
- `226-228`: ensures `endTransaction()` always runs, even if an insert fails.
- `229`: method ends.

**Why it exists**

- The app no longer hardcodes runtime question arrays in the repository.
- Instead, it seeds the quiz once into SQLite, then reads it from there.

**How it strays from the slides**

- It is still valid Chapter 13 SQLite logic, but it is more elaborate than the lab example.
- The lecture example usually seeds or inserts one simple entity.
- This function seeds a multi-table relational structure in one transaction.

### Function: `insertCategory(SQLiteDatabase db, CareerArchetype archetype)`

**Lines:** `231-239`

**What it does**

- Inserts one category row into the `categories` table using an enum instance.

**How it works, line by line**

- `231`: private helper declaration.
- `232`: creates `ContentValues`.
- `233`: stores enum name into `category_key`.
- `234`: stores title.
- `235`: stores field.
- `236`: stores description.
- `237`: stores examples.
- `238`: inserts the row and returns the new row ID.
- `239`: method ends.

**Why it exists**

- It centralizes category insertion logic so `seedDatabase()` stays readable.

**How it strays from the slides**

- Small deviation: it bridges enum-driven UI metadata into stored relational rows.
- This dual source pattern is more structured than the simplest example.

### Function: `insertQuestionWithOptions(...)`

**Lines:** `241-262`

**What it does**

- Inserts one question row, then inserts its four option rows linked to that question.

**How it works, line by line**

- `241-247`: function signature takes:
  - database handle
  - display order
  - question text
  - option texts
  - category IDs
- `248`: creates `ContentValues` for the question.
- `249`: stores question text.
- `250`: stores display order.
- `251`: inserts question row and captures generated `questionId`.
- `253`: starts loop over the option array.
- `254`: creates `ContentValues` for one option row.
- `255`: links option to inserted `questionId`.
- `256`: stores option text.
- `257`: stores the category foreign key for this option.
- `258`: sets point value to `1`.
- `259`: stores display order within the question.
- `260`: inserts the option row.
- `261`: loop ends.
- `262`: method ends.

**Why it exists**

- It preserves the current quiz design:
  - 1 question
  - 4 options
  - each option mapped to a category

**How it strays from the slides**

- This function is more normalized than a chapter example that might store all four options directly in a single question row.

---

## 2. `QuizRepository.java`

**Path**

- `app/src/main/java/com/mckl/assignment1/repository/QuizRepository.java`

**Purpose**

- This class performs CRUD operations and converts raw SQLite rows into app models.
- It is the bridge between the helper class and the UI/view-model code.

**What makes it different from the simplest Chapter 13 slide example**

- The chapter often places CRUD directly in the helper or in the activity.
- This implementation introduces a repository layer, which is cleaner architecturally but more advanced.

### Function: `QuizRepository(Context context)`

**Lines:** `23-25`

**What it does**

- Creates the repository and initializes the helper with application context.

**How it works**

- `23`: constructor declaration.
- `24`: creates `QuizDatabaseHelper` using `context.getApplicationContext()`.
- `25`: method ends.

**Why it exists**

- The repository needs one helper instance for all later reads and writes.

**Deviation from the chapter norm**

- Using a separate repository is cleaner than putting database calls directly in fragments or activities.

### Function: `getQuestions()`

**Lines:** `27-54`

**What it does**

- Reads all questions from SQLite in display order and returns them as `Question` objects.

**How it works**

- `28`: creates result list.
- `29`: opens readable database.
- `30-41`: runs a query on `questions`.
  - selects `question_id` and `question_text`
  - no filter
  - sorts by `display_order ASC`
- `43`: starts protected cursor use.
- `44`: loops through each returned row.
- `45`: reads `question_id`.
- `46`: reads `question_text`.
- `47`: constructs a `Question` model and fills its options by calling `getOptionsForQuestion(questionId)`.
- `49-50`: closes cursor in `finally`.
- `53`: returns the completed question list.

**Why it exists**

- The quiz screen needs a complete in-memory list of questions loaded from SQLite.

**Deviation from the chapter norm**

- The lecture example may fetch rows directly into UI widgets.
- Here, the function builds structured domain objects instead.

### Function: `getOptionsForQuestion(int questionId)`

**Lines:** `56-89`

**What it does**

- Reads all options for one question and joins them with category data.

**How it works**

- `57`: creates result list.
- `58`: opens readable database.
- `59-70`: builds raw SQL query string.
  - selects option ID, question ID, option text, category key, point value, and display order
  - reads from `options` aliased as `o`
  - joins `categories` aliased as `c`
  - filters by one question ID
  - orders by option display order
- `71`: executes the raw query with the question ID parameter.
- `73`: starts protected cursor use.
- `74`: loops through option rows.
- `75-82`: creates `QuestionOption` object from the six selected columns.
- `84-85`: closes cursor in `finally`.
- `88`: returns the option list.

**Why it exists**

- Each question must carry its own 4 answer options to the UI.

**Deviation from the chapter norm**

- This is more advanced than a simple `SELECT * FROM table`.
- It uses a join because category mapping is normalized into another table.

### Function: `getLatestQuizAttempt()`

**Lines:** `91-119`

**What it does**

- Retrieves the most recent completed attempt for the Home screen.

**How it works**

- `92`: opens readable database.
- `93-102`: builds query:
  - selects attempt ID, category key, category title, completion time
  - joins `quiz_attempts` with `categories`
  - excludes unfinished attempts by requiring `final_category_id IS NOT NULL`
  - sorts newest first
  - limits result to 1 row
- `103`: executes query.
- `105`: starts protected cursor use.
- `106`: checks whether any row exists.
- `107-112`: builds `QuizAttempt` object from the returned row.
- `114-115`: closes cursor.
- `118`: returns `null` if no completed attempt exists.

**Why it exists**

- The Home screen needs only one value: the latest stored result.

**Deviation from the chapter norm**

- Uses join + ordering + null filtering, which is more complex than the simplest lab retrieval method.

### Function: `getAllQuizAttempts()`

**Lines:** `121-149`

**What it does**

- Retrieves all attempts, newest first.

**How it works**

- `122`: creates result list.
- `123`: opens readable database.
- `124-132`: builds query:
  - selects attempt data plus category metadata
  - uses `LEFT JOIN` so even attempts without finalized category data can still appear
  - sorts newest first
- `133`: executes query.
- `135`: begins protected cursor loop.
- `136`: iterates through all rows.
- `137-142`: creates `QuizAttempt` per row.
- `144-145`: closes cursor.
- `148`: returns list.

**Why it exists**

- Supports retrieval of stored attempt history even if the UI does not currently display all attempts.

**Deviation from the chapter norm**

- More advanced than necessary for a minimum quiz app, but useful for full CRUD/read completeness.

### Function: `calculateResult(int[] scores)`

**Lines:** `151-161`

**What it does**

- Converts the in-memory score array into the winning `CareerArchetype`.

**How it works**

- `152`: initializes `maxIndex` to first category.
- `153-157`: scans remaining score slots and updates `maxIndex` whenever a larger score is found.
- `159`: gets enum array in declaration order.
- `160`: returns the enum at `maxIndex`.
- `161`: method ends.

**Why it exists**

- The UI needs a final category enum after the last question.

**Deviation from the chapter norm**

- This is not a pure database calculation.
- It keeps result calculation in memory, which is simpler for UI flow but less database-centric than a fully SQL-derived tally.

### Function: `createQuizAttempt()`

**Lines:** `163-169`

**What it does**

- Inserts a new parent row into `quiz_attempts` immediately so later answer rows have a valid foreign-key target.

**How it works**

- `164`: opens writable database.
- `165`: creates empty `ContentValues`.
- `166`: adds a placeholder empty string into `completed_at` so SQLite definitely materializes a real parent row.
- `167`: stores that placeholder value into the `ContentValues`.
- `168`: inserts the row into `quiz_attempts` and returns the inserted row ID.
- `169`: method ends.

**Why it exists**

- The app needs an `attempt_id` before inserting child rows into `attempt_answers`.

**Deviation from the chapter norm**

- The chapter normally demonstrates obvious inserts with concrete values.
- This function is a more unusual pattern because it creates a parent row first and fills in the final result later.

**Important note**

- This function is one of the more delicate parts of the design because the child `attempt_answers` rows depend on it existing first.
- The placeholder `completed_at` write was added to prevent the earlier crash scenario where the app could try to save an answer before SQLite had a concrete parent row to reference.

### Function: `insertAttemptAnswer(long attemptId, int questionId, int optionId, int awardedPoints)`

**Lines:** `171-184`

**What it does**

- Inserts or replaces one saved answer inside `attempt_answers`.

**How it works**

- `172`: opens writable database.
- `173`: creates `ContentValues`.
- `174`: stores parent attempt ID.
- `175`: stores question ID.
- `176`: stores selected option ID.
- `177`: stores awarded point value.
- `178-183`: inserts row with `CONFLICT_REPLACE`.
- `184`: method ends.

**Why it exists**

- This is the core persistence step that records which option the user chose.

**Deviation from the chapter norm**

- Using `insertWithOnConflict(..., CONFLICT_REPLACE)` is more advanced than the simplest insert examples.
- It is needed because the schema enforces one answer per question per attempt.

### Function: `updateQuizAttemptResult(long attemptId, String categoryKey, String completedAt)`

**Lines:** `186-202`

**What it does**

- Finalizes an existing attempt by writing the winning category and completion time.

**How it works**

- `187`: converts category key into numeric category ID via `getCategoryIdByKey`.
- `188-190`: aborts if the category does not exist.
- `192`: opens writable database.
- `193`: creates `ContentValues`.
- `194`: stores final category foreign key.
- `195`: stores completion timestamp.
- `196-201`: updates the matching attempt row by `attempt_id`.
- `202`: method ends.

**Why it exists**

- The app creates the attempt early, but only knows the final result after the last question.

**Deviation from the chapter norm**

- This staged parent-row lifecycle is more complex than a basic "insert completed record once" approach.

### Function: `insertCategory(...)`

**Lines:** `204-213`

**What it does**

- Inserts a category row through the repository layer.

**How it works**

- `205`: opens writable database.
- `206`: creates `ContentValues`.
- `207-211`: writes all category columns.
- `212`: inserts and returns new row ID.
- `213`: method ends.

**Why it exists**

- Supports explicit Create in CRUD.

**Deviation from the chapter norm**

- No major deviation beyond being exposed through repository.

### Function: `updateCategory(...)`

**Lines:** `215-228`

**What it does**

- Updates one category row identified by `category_key`.

**How it works**

- `216`: opens writable database.
- `217`: creates `ContentValues`.
- `218-221`: stores new title, field, description, and examples.
- `222-227`: updates the row whose key matches the passed value.
- `228`: method ends.

**Why it exists**

- Supports Update in CRUD.

**Deviation from the chapter norm**

- More complete than the visible UI requires.

### Function: `deleteCategory(String categoryKey)`

**Lines:** `230-237`

**What it does**

- Deletes one category row by key.

**How it works**

- `231`: opens writable database.
- `232-236`: deletes matching row.
- `237`: method ends.

**Why it exists**

- Supports Delete in CRUD.

**Deviation from the chapter norm**

- Again, more repository completeness than current UI usage.

### Function: `insertQuestion(String questionText, int displayOrder)`

**Lines:** `239-245`

**What it does**

- Inserts one new question row.

**How it works**

- `240`: opens writable database.
- `241`: creates `ContentValues`.
- `242`: stores text.
- `243`: stores display order.
- `244`: inserts row and returns new ID.
- `245`: method ends.

### Function: `updateQuestion(int questionId, String questionText, int displayOrder)`

**Lines:** `247-258`

**What it does**

- Updates a question row by ID.

**How it works**

- `248`: opens writable database.
- `249`: creates `ContentValues`.
- `250`: stores text.
- `251`: stores display order.
- `252-257`: updates row by `question_id`.
- `258`: method ends.

### Function: `deleteQuestion(int questionId)`

**Lines:** `260-267`

**What it does**

- Deletes one question row by ID.

**How it works**

- `261`: opens writable database.
- `262-266`: deletes question by `question_id`.
- `267`: method ends.

**Deviation from the chapter norm for the question CRUD group**

- The chapter often demonstrates only the CRUD actually exposed in the lab UI.
- Here, full CRUD exists in repository even though the app UI does not expose question maintenance.

### Function: `insertOption(...)`

**Lines:** `269-283`

**What it does**

- Inserts one option row linked to a question and a category.

**How it works**

- `270`: resolves `categoryKey` into numeric category ID.
- `271-273`: aborts with `-1` if category is missing.
- `275`: opens writable database.
- `276`: creates `ContentValues`.
- `277`: stores parent question ID.
- `278`: stores option text.
- `279`: stores category foreign key.
- `280`: stores point value.
- `281`: stores display order.
- `282`: inserts row and returns new ID.
- `283`: method ends.

**Why it exists**

- Supports explicit option creation and preserves normalized option/category mapping.

### Function: `updateOption(...)`

**Lines:** `285-303`

**What it does**

- Updates one option row.

**How it works**

- `286`: resolves category key to category ID.
- `287-289`: aborts if category is missing.
- `291`: opens writable database.
- `292`: creates `ContentValues`.
- `293-296`: stores updated option fields.
- `297-302`: updates row by option ID.
- `303`: method ends.

### Function: `deleteOption(int optionId)`

**Lines:** `305-312`

**What it does**

- Deletes one option row by ID.

**How it works**

- `306`: opens writable database.
- `307-311`: deletes row.
- `312`: method ends.

**Deviation from the chapter norm for the option CRUD group**

- Separate option CRUD is a direct result of using normalized options instead of four flat option columns.

### Function: `updateAttemptAnswer(...)`

**Lines:** `314-326`

**What it does**

- Updates an already-saved answer row for one question within one attempt.

**How it works**

- `315`: opens writable database.
- `316`: creates `ContentValues`.
- `317`: stores updated option ID.
- `318`: stores updated awarded points.
- `319-325`: updates the row identified by both `attempt_id` and `question_id`.
- `326`: method ends.

**Why it exists**

- Supports Update in CRUD for persisted answer history.

**Deviation from the chapter norm**

- More elaborate than a basic quiz app, but consistent with storing detailed answer history.

### Function: `deleteQuizAttempt(long attemptId)`

**Lines:** `328-335`

**What it does**

- Deletes one attempt row.

**How it works**

- `329`: opens writable database.
- `330-334`: deletes row by attempt ID.
- `335`: method ends.

**Why it exists**

- Supports Delete in CRUD.

**Extra behavior**

- Because the `attempt_answers` table uses `ON DELETE CASCADE`, deleting the attempt also deletes its child answers.

### Function: `getCategoryIdByKey(String categoryKey)`

**Lines:** `337-358`

**What it does**

- Converts human-readable enum-like category keys into the numeric foreign key stored in SQLite.

**How it works**

- `338`: opens readable database.
- `339-347`: queries `categories` for `category_id` where `category_key` matches.
- `349`: begins protected cursor use.
- `350`: checks whether a row exists.
- `351`: returns the numeric `category_id`.
- `353-354`: closes cursor.
- `357`: returns `-1L` if no row exists.
- `358`: method ends.

**Why it exists**

- The UI and view-model code work with enum keys like `ARCHITECT`.
- The database relations use numeric IDs.

**Deviation from the chapter norm**

- This lookup is needed only because the schema is more normalized than the simplest classroom example.

---

## 3. `QuizViewModel.java`

**Path**

- `app/src/main/java/com/mckl/assignment1/viewmodel/QuizViewModel.java`

**Purpose**

- Orchestrates quiz progress.
- Reads questions from repository.
- Tracks in-memory scores.
- Persists answers and final results into SQLite.

**What makes it different from the simplest Chapter 13 slide example**

- It mixes app state management and persistence coordination.
- It keeps a live in-memory tally while also saving answer rows to SQLite.
- This is a pragmatic hybrid design, not a pure "calculate from SQL each time" design.

### Function: `QuizViewModel(@NonNull Application application)`

**Lines:** `33-38`

**What it does**

- Initializes repository access and preloads all quiz questions.

**How it works**

- `34`: calls parent constructor.
- `35`: creates repository.
- `36`: loads all questions from SQLite immediately.
- `37`: pushes the first question into `currentQuestion`.
- `38`: constructor ends.

**Why it exists**

- The fragment needs the current question immediately after observation starts.

### Function: `getCurrentQuestionIndex()`

**Lines:** `40-42`

**What it does**

- Exposes the current question index as observable state.

**How it works**

- `41`: returns `currentQuestionIndex`.

### Function: `getCurrentQuestion()`

**Lines:** `44-46`

**What it does**

- Exposes the current `Question` model for the fragment.

### Function: `getResult()`

**Lines:** `48-50`

**What it does**

- Exposes the final quiz result for navigation to the result screen.

### Function: `getTotalQuestions()`

**Lines:** `52-54`

**What it does**

- Returns the total number of loaded questions.

**Deviation from the chapter norm for the getter group**

- These are standard ViewModel accessors. No meaningful deviation.

### Function: `answerQuestion(int optionPosition)`

**Lines:** `56-88`

**What it does**

- Handles one answer tap from the UI.
- Validates current state.
- Creates an attempt if needed.
- Updates in-memory score.
- Persists the selected answer.
- Advances quiz or finalizes result.

**How it works**

- `57`: gets current question from LiveData.
- `58`: gets current index.
- `59-61`: returns early if state is invalid.
- `63`: reads the question's option list.
- `64-66`: returns early if tapped index is outside available options.
- `68`: selects the tapped option model.
- `69-71`: if no attempt exists yet, creates one now.
- `73`: converts option's category key into enum ordinal.
- `74`: increments in-memory score by that option's point value.
- `75-80`: writes the selected answer into `attempt_answers`.
- `82-85`: if there are more questions, increment index and load next question.
- `86`: otherwise finalize quiz result.
- `88`: method ends.

**Why it exists**

- This is the main control point for the live quiz flow.

**Deviation from the chapter norm**

- Uses both memory and SQLite:
  - memory for live tally
  - SQLite for persisted answer history
- That is more complex than a pure demo approach, but it keeps the UI responsive.

### Function: `updateQuestion()`

**Lines:** `90-95`

**What it does**

- Synchronizes `currentQuestion` LiveData with the current question index.

**How it works**

- `91`: reads current index.
- `92`: validates index against list bounds.
- `93`: pushes the selected `Question` object into LiveData.
- `95`: method ends.

**Why it exists**

- Keeps the fragment's question observer simple.

### Function: `calculateAndSaveResult()`

**Lines:** `97-107`

**What it does**

- Computes the final winning category and saves it into the current attempt.

**How it works**

- `98`: asks repository to convert the score array into a final enum.
- `99`: generates formatted timestamp string.
- `101-103`: creates attempt if one somehow does not yet exist.
- `105`: updates the attempt row with final category and completion time.
- `106`: publishes final result via LiveData.
- `107`: method ends.

**Why it exists**

- This finalizes the staged attempt lifecycle that began when the first answer was recorded.

**Deviation from the chapter norm**

- Again, this is more lifecycle-aware than the simplest CRUD example.

### Function: `onCleared()`

**Lines:** `109-113`

**What it does**

- Clears the transient in-memory score array when the ViewModel is destroyed.

**How it works**

- `111`: calls parent cleanup.
- `112`: fills score array with zeros.

**Why it exists**

- Prevents old in-memory state from lingering across ViewModel destruction.

---

## 4. `HomeViewModel.java`

**Path**

- `app/src/main/java/com/mckl/assignment1/viewmodel/HomeViewModel.java`

**Purpose**

- Loads the latest saved attempt for the Home screen.

**What makes it different from the simplest Chapter 13 slide example**

- This adds a ViewModel layer for a simple one-value fetch.
- It is architecturally cleaner, but more layered than a basic example.

### Function: `HomeViewModel(@NonNull Application application)`

**Lines:** `20-24`

**What it does**

- Initializes repository and immediately loads the latest attempt.

**How it works**

- `21`: parent constructor call.
- `22`: repository creation.
- `23`: refreshes latest attempt from SQLite.
- `24`: constructor ends.

### Function: `getLatestAttempt()`

**Lines:** `26-28`

**What it does**

- Exposes the latest attempt LiveData.

### Function: `refreshLatestAttempt()`

**Lines:** `30-32`

**What it does**

- Requeries SQLite and publishes the newest completed attempt.

**How it works**

- `31`: sets LiveData value using repository query result.

**Why it exists**

- The Home screen calls it again in `onResume()` so returning from the result screen refreshes the `Last Result` text.

---

## 5. `Question.java`

**Path**

- `app/src/main/java/com/mckl/assignment1/model/Question.java`

**Purpose**

- Represents one question plus its loaded option list.

**What makes it slightly different from a basic model**

- It performs defensive copying and exposes an unmodifiable list.

### Function: `Question(int questionId, String text, List<QuestionOption> options)`

**Lines:** `15-19`

**What it does**

- Builds one immutable-ish `Question` object.

**How it works**

- `16`: stores `questionId`.
- `17`: stores text.
- `18`: creates a new `ArrayList` copy of the passed options instead of keeping the caller's list reference.
- `19`: constructor ends.

**Why it exists**

- Keeps the question self-contained once built by the repository.

### Function: `getQuestionId()`

**Lines:** `21-23`

**What it does**

- Returns database ID of the question.

### Function: `getText()`

**Lines:** `25-27`

**What it does**

- Returns question text.

### Function: `getOptions()`

**Lines:** `29-31`

**What it does**

- Returns the question's options as an unmodifiable list.

**How it works**

- `30`: wraps the internal list using `Collections.unmodifiableList(options)`.

**Deviation from the chapter norm**

- This is slightly more defensive than the simplest data model pattern.

---

## 6. `QuestionOption.java`

**Path**

- `app/src/main/java/com/mckl/assignment1/model/QuestionOption.java`

**Purpose**

- Represents one option row loaded from SQLite.

### Function: `QuestionOption(...)`

**Lines:** `14-28`

**What it does**

- Stores all option-level data needed by the UI and scoring logic.

**How it works**

- `15-20`: constructor parameters define all fields.
- `22`: stores option ID.
- `23`: stores parent question ID.
- `24`: stores option text.
- `25`: stores category key string.
- `26`: stores point value.
- `27`: stores display order.
- `28`: constructor ends.

**Why it exists**

- The repository needs a model richer than plain text because each option also carries scoring metadata.

### Getter Functions

- `getOptionId()` lines `30-32`
- `getQuestionId()` lines `34-36`
- `getText()` lines `38-40`
- `getCategoryKey()` lines `42-44`
- `getPointValue()` lines `46-48`
- `getDisplayOrder()` lines `50-52`

**What they do**

- Each returns one field exactly as stored in the constructor.

**Deviation from the chapter norm**

- The model is more detailed than a beginner example because the option itself carries both relational and scoring data.

---

## 7. `QuizAttempt.java`

**Path**

- `app/src/main/java/com/mckl/assignment1/model/QuizAttempt.java`

**Purpose**

- Represents one persisted attempt summary row as used by the Home screen and repository reads.

### Function: `QuizAttempt(long attemptId, String archetypeKey, String archetypeTitle, String completedAt)`

**Lines:** `12-17`

**What it does**

- Creates one immutable attempt summary object.

**How it works**

- `13`: stores attempt ID.
- `14`: stores category key.
- `15`: stores category title.
- `16`: stores completion time.
- `17`: constructor ends.

### Getter Functions

- `getAttemptId()` lines `19-21`
- `getArchetypeKey()` lines `23-25`
- `getArchetypeTitle()` lines `27-29`
- `getCompletedAt()` lines `31-33`

**What they do**

- Return the stored summary values.

**Deviation from the chapter norm**

- Not much deviation. It is a normal read model introduced because the app persists attempt history.

---

## 8. `HomeFragment.java`

**Path**

- `app/src/main/java/com/mckl/assignment1/ui/HomeFragment.java`

**Purpose**

- Displays the intro screen and the latest saved result.
- Starts the quiz when the user taps the button.

**What makes it different from the old non-database version**

- It no longer reads `SharedPreferences` directly.
- It now depends on `HomeViewModel` and SQLite-backed repository reads.

### Function: `onCreateView(...)`

**Lines:** `26-31`

**What it does**

- Inflates the view binding for the Home fragment.

**How it works**

- `29`: inflates `FragmentHomeBinding`.
- `30`: returns root view.

### Function: `onViewCreated(...)`

**Lines:** `33-43`

**What it does**

- Wires the fragment to its ViewModel and click behavior.

**How it works**

- `35`: standard fragment lifecycle call.
- `36`: creates `HomeViewModel`.
- `38`: observes latest attempt LiveData and delegates rendering to `renderLatestAttempt`.
- `40-42`: sets Start button click handler to navigate to quiz fragment.

**Why it exists**

- It is the glue between Home UI and SQLite-backed result retrieval.

### Function: `onResume()`

**Lines:** `45-51`

**What it does**

- Refreshes the latest result whenever the fragment comes back into view.

**How it works**

- `47`: standard lifecycle call.
- `48-50`: if the ViewModel exists, asks it to refresh from repository.

**Why it exists**

- Returning from the result screen should update `Last Result` immediately.

### Function: `renderLatestAttempt(QuizAttempt attempt)`

**Lines:** `53-61`

**What it does**

- Converts a `QuizAttempt` object into the visible `Last Result` text.

**How it works**

- `54`: checks that attempt object, title, and completion time are all non-null.
- `55-57`: formats and displays the latest result string.
- `58-60`: otherwise shows `Last Result: None`.

**Why it exists**

- Keeps display logic separate from lifecycle and navigation logic.

### Function: `onDestroyView()`

**Lines:** `63-67`

**What it does**

- Clears the binding reference when the fragment view is destroyed.

**Why it exists**

- Prevents holding a stale view binding after fragment view teardown.

---

## 9. `QuizFragment.java`

**Path**

- `app/src/main/java/com/mckl/assignment1/ui/QuizFragment.java`

**Purpose**

- Displays question text, answer options, and progress.
- Sends user selections into `QuizViewModel`.

**What makes it different from the old non-database version**

- It no longer assumes the repository provides hardcoded array-backed questions.
- It now renders `Question` objects that already came from SQLite.

### Function: `onCreateView(...)`

**Lines:** `26-31`

**What it does**

- Inflates the view binding for the quiz screen.

### Function: `onViewCreated(...)`

**Lines:** `33-60`

**What it does**

- Connects the quiz screen to ViewModel data and button actions.

**How it works**

- `35`: standard lifecycle call.
- `36`: creates `QuizViewModel`.
- `38`: observes current question and delegates UI binding to `bindQuestion`.
- `40-45`: observes current question index and updates:
  - progress indicator
  - visible question number text
- `47-54`: observes final result LiveData.
  - if non-null, packages result enum name into `Bundle`
  - navigates to `ResultFragment`
- `56-59`: wires all four answer buttons to `viewModel.answerQuestion(...)`.

**Why it exists**

- This is the main UI event-binding function for the live quiz.

### Function: `bindQuestion(Question question)`

**Lines:** `62-72`

**What it does**

- Copies the current `Question` model into visible UI controls.

**How it works**

- `63-65`: exits early if question is null or if fewer than 4 options exist.
- `67`: sets question text.
- `68-71`: sets all four answer button labels from the loaded options.

**Why it exists**

- Keeps the observer body small and focused.

**Deviation from the chapter norm**

- Uses a prebuilt model object with nested option objects rather than binding raw cursor values directly into UI.

### Function: `onDestroyView()`

**Lines:** `74-78`

**What it does**

- Clears the fragment binding when the view is destroyed.

---

## High-Level Deviations From the Chapter Slides

These are the main places where the implementation is more complex than the simplest Chapter 13 examples.

### 1. Normalized schema instead of one flat table

- The chapter often demonstrates a smaller, flatter structure.
- This implementation uses:
  - `categories`
  - `questions`
  - `options`
  - `quiz_attempts`
  - `attempt_answers`

**Why**

- The assignment wording explicitly asked to store questions, options, categories, and points.
- A normalized design makes those relationships explicit.

### 2. Separate `options` table

- In a simpler teaching example, the four options might be stored directly inside the question row.
- Here they are separate rows with category and point metadata.

**Why**

- Each option needs:
  - question link
  - category link
  - point value
  - display order

### 3. Repository layer

- The chapter often shows CRUD methods directly inside the helper or activity.
- Here the helper owns schema and the repository owns CRUD.

**Why**

- Cleaner separation of responsibilities.
- Easier to connect with ViewModels and Fragments.

### 4. Hybrid score handling

- SQLite stores answer rows and points.
- The ViewModel still maintains an in-memory `scores[]` array during the live session.

**Why**

- Fast live navigation and result calculation.
- Still persists the answer trail into the database.

### 5. Staged attempt lifecycle

- The app conceptually creates the attempt before the quiz is fully complete.
- The final category and completion timestamp are only written later.

**Why**

- Child answer rows need a parent attempt context.
- This is more lifecycle-aware than a simple "insert final record once at the end" pattern.

---

## Final Summary

The database work does follow the core chapter principles:

- `SQLiteOpenHelper`
- `SQLiteDatabase`
- `ContentValues`
- `Cursor`
- `onCreate()`
- `onUpgrade()`

The main differences are architectural:

- more tables
- more relationships
- explicit option/category/point modeling
- repository abstraction
- persisted answer history

Those differences make the implementation more complex than the slides, but they were chosen to fit the assignment requirements more closely and to make the database actually responsible for quiz content and result persistence.
