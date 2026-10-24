You are a Senior Android UI/UX Architect. I am working on **Sprint 2 (Core UI & Emergency Browsing)** of **"FirstAid Hero"**, an offline-first Arabic medical emergency Android application written in **pure Java** using **MVVM** and **Room**.

### 1. Existing Codebase Context (DO NOT RECREATE THESE)
Sprint 1 is 100% complete. The following classes already exist and must be used as-is:
* **Package Root:** `com.svu.firstaidhero`
* **Entities (`com.svu.firstaidhero.database.entities`):**
  * `Emergency`: `getCaseId()`, `getTitle()`, `getSymptoms()`, `getDangerLevel()` (returns `"High"` or `"Minor"`), `getKeywords()`, `isCommon()` (returns `boolean`).
  * `Step`: `getStepId()`, `getCaseId()`, `getStepNumber()`, `getInstructionText()`.
  * `CustomContact`: `getContactId()`, `getName()`, `getPhoneNumber()`, `getRelationship()`.
  * `TreatedCase`: `getLogId()`, `getDescription()`, `getDateTreated()`, `getProceduralNotes()`, `getPhotoUri()`.
* **ViewModels (`com.svu.firstaidhero.viewmodels`):**
  * `EmergencyViewModel`:
    * `LiveData<List<Emergency>> getAllEmergencies()`
    * `LiveData<List<Emergency>> searchEmergencies(String query)`
    * `LiveData<Emergency> getEmergencyById(int caseId)`
    * `LiveData<List<Step>> getStepsForEmergency(int caseId)`
  * `ContactAndLogViewModel`:
    * `LiveData<List<CustomContact>> getAllContacts()`
    * `void insertContact(CustomContact contact)`
    * `LiveData<List<TreatedCase>> getAllTreatedCases()`
    * `void insertTreatedCase(TreatedCase treatedCase)`

---

### 2. Strict Architectural & UI Rules
1. **Language:** Pure Java only (NO Kotlin).
2. **MVVM Enforcement:** Activities and Fragments must NEVER access `AppDatabase`, `DAOs`, or `FirstAidRepository` directly. Always use `new ViewModelProvider(this).get(...)`.
3. **Arabic RTL Enforcement:**
   * All UI text, hints, and labels must be in **Arabic**.
   * Enforce Right-to-Left layout direction (`android:layoutDirection="rtl"` in XML and `getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL)` in Activities).
   * Always use `Start`/`End` attributes instead of `Left`/`Right`.
4. **Data Formatting & Sanitization (`TextSanitizer.java`):**
   * Some database records imported from JSON contain raw array formatting in `title` and `symptoms` (e.g., `["كيف أعالج الحروق؟", "حروق"]`) or pipe separators (`|`).
   * Before binding text to any `TextView`, clean the string: if it starts with `[`, parse/strip `["`, `"]`, and quotes; for `title`, display the last (shortest/cleanest) item in the array; for `symptoms`, replace `|` and `,` with clean bullet points (`• `).
   * Map `danger_level`:
     * `"High"` -> Display Arabic badge **"حالة حرجة"** with Red background (`#D32F2F`).
     * `"Minor"` -> Display Arabic badge **"إصابة بسيطة"** with Green/Teal background (`#388E3C`).

---

### 3. Your Mission: Implement Sprint 2 Deliverables

Please inspect my project structure first, then implement the following components step-by-step:

#### Task 1: Text Sanitizer Utility
* Create `com.svu.firstaidhero.utils.TextSanitizer.java`:
  * `public static String cleanTitle(String rawTitle)`
  * `public static String cleanSymptoms(String rawSymptoms)`
  * `public static String getArabicDangerLevel(String dangerLevel)`

#### Task 2: RecyclerView Adapters (`com.svu.firstaidhero.views.adapters`)
1. `EmergencyAdapter.java` + `res/layout/item_emergency.xml`:
   * MaterialCardView item showing the cleaned Arabic title, a 2-line preview of cleaned symptoms, and a color-coded Danger Level badge (`حالة حرجة` vs `إصابة بسيطة`).
   * Include an `OnEmergencyClickListener` interface that passes the clicked `Emergency` (or `case_id`) to open the detail screen.
2. `StepAdapter.java` + `res/layout/item_step.xml`:
   * Displays each `Step` in chronological order with a prominent circular badge for `step_number` (1, 2, 3...) and clear, readable `instruction_text`.

#### Task 3: Main Emergency Dashboard & Search (`MainActivity.java` + `activity_main.xml`)
* Update `AndroidManifest.xml` to ensure `android:supportsRtl="true"`.
* Design `res/layout/activity_main.xml` with:
  * A top Arabic header / Toolbar (`المسعف الأول - FirstAid Hero`).
  * A search bar (`SearchView` or `EditText` with hint `"ابحث عن حالة طارئة أو عرض (مثل: حروق، اختناق)..."`).
  * Filter chips (`ChipGroup`) for: **"الكل"** (All), **"حالات شائعة"** (`isCommon() == true`), and **"حالات حرجة"** (`"High".equalsIgnoreCase(dangerLevel)`).
  * A `RecyclerView` displaying the emergencies, plus an empty-state `TextView` (`"لا توجد نتائج مطابقة"`).
* Connect `MainActivity` to `EmergencyViewModel` to observe `getAllEmergencies()` and trigger `searchEmergencies(query)` dynamically as the user types.

#### Task 4: Emergency Detail Screen (`EmergencyDetailActivity.java` + `activity_emergency_detail.xml`)
* Create `com.svu.firstaidhero.views.activities.EmergencyDetailActivity` and register it in `AndroidManifest.xml`.
* Pass `EXTRA_CASE_ID` via `Intent` when an item is clicked in `MainActivity`.
* Observe `getEmergencyById(caseId)` and `getStepsForEmergency(caseId)` from `EmergencyViewModel`.
* Display the emergency title, danger badge, full symptoms list, and the `RecyclerView` of ordered first-aid steps using `StepAdapter`.

After creating these files, run a Gradle build check (`:app:assembleDebug`) to verify there are no compilation or resource errors.