# Workout Logging with Room Persistence in Gym3D

Add an on-device Room database persistence layer to Gym3D that allows users to record, review, and auto-fill completed sets, repetitions, resistance weights, or cardio metrics directly within each equipment's 3D detail screen, complete with an interactive rest countdown timer.

## User Review & Critical Decisions

> [!IMPORTANT]
> Based on your responses, the logging architecture is configured as follows:

- **Location (Confirmed)**: Dedicated "Workout Log" tab positioned directly inside each equipment's 3D detail screen, allowing users to inspect the 3D model, review setup pins, and log sets without leaving the equipment view.
- **Adaptive Tracking Fields (Confirmed)**:
  - **Strength & Cable Machines**: `Set Number`, `Weight (kg / lbs)`, `Reps Completed`, `RPE / Notes`.
  - **Cardio Machines**: `Interval / Round`, `Duration (Minutes:Seconds)`, `Distance (km / mi)`, `Incline / Resistance Level / Pace`, and `Heart Rate / Calories`.
- **Companion Tools (Confirmed)**:
  - **Previous Set Auto-Fill**: Automatically fills in the weight and reps from the user's last recorded set for that equipment to speed up logging.
  - **Interactive Rest Timer**: Built-in 60s / 90s / 120s / custom rest countdown with progress ring and vibration / visual notification upon completion.
  - **Session History & PR Badges**: Displays past workout dates, completed sets history, total volume (weight × reps), and Personal Record (PR) badges.

---

## 1. Overview & Core Concept

- **What It Does**: Bridges 3D equipment visualization with real-world gym performance tracking. After users check the 3D form demonstration and calibration pins for a machine (e.g., *Cable Lat Pulldown* or *Squat Rack*), they switch to the "Workout Log" tab to log their work sets, start rest timers, and see their historical volume progression over time.
- **Target Audience**: Lifters and gym members who want an integrated tool where they can look up proper equipment biomechanics and immediately log their working weights and reps in one seamless offline app.
- **Key Value**: Replaces separate notebook/generic tracker apps with equipment-contextual tracking backed by local Room database storage that works 100% offline.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Viewing & Logging Sets in Detail Screen**:
   - The user opens an equipment screen (e.g., *Barbell Flat Bench Press*).
   - In the tab bar, a new **"Log (X Sets)"** tab appears next to *3D Guide*, *Setup & Pins*, *Muscles*, and *Form Mistakes*.
   - Tapping **"Log"** shows:
     - Today's active session set table (Set 1, Set 2, Set 3...).
     - Pre-populated suggested weight and reps based on the user's previous session.
     - Single-tap **"+ Add Set"** with quick +/- stepper buttons for weight (+2.5kg / +5kg) and reps (+1).
     - Checkmark button to mark the set as "Completed", which automatically triggers the rest timer.
2. **Rest Timer HUD**:
   - Marking a set completed launches a non-intrusive floating or embedded rest timer (e.g., 90s countdown with pause, +30s, and skip buttons).
   - A circular animated progress indicator displays remaining rest time with haptic visual cues when finished.
3. **Cardio Machine Adaptive Form**:
   - When viewing cardio gear (e.g., *Commercial Treadmill*, *Stationary Bike*, *Rowing Machine*), the logging form seamlessly shifts to Duration (min:sec), Distance (km), Speed/Incline, and Calories.
4. **Historical Log & Personal Records (PR)**:
   - Below the active logging card, an expandable "Previous Sessions History" card displays past logged dates, total tonnage volume, and personal best sets.
   - Users can swipe or tap to delete erroneous entries.

### Visual Identity & Theme Alignment

- Preserves Gym3D's **Titanium Slate & Electric Lime** aesthetic:
  - `Set Number Badge`: Cyber Emerald pill indicator.
  - `Completed Set Row`: Subtle dark surface highlight with glowing green checkmark.
  - `Rest Timer`: Neon Cyan circular countdown gauge with high-contrast digital timer.
  - `PR Badge`: Safety Amber trophy tag.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Room SQLite Database with Android KSP
- **Chosen Approach**: Room Database (`GymDatabase`) with `WorkoutSetEntity`, `WorkoutSetDao`, and reactive Kotlin `Flow`.
- **Why**:
  - Guaranteed offline reliability in gym basements where cell reception is unavailable.
  - Room and KSP dependencies are already integrated in `build.gradle.kts`.
  - Zero network latency, instant CRUD performance, and automatic reactive UI updates via `collectAsStateWithLifecycle`.

### Decision 2: In-Context Equipment Logging vs. Separate Screen
- **Chosen Approach**: Dedicated tab embedded directly within each equipment's detail view.
- **Why**: Keeps the user focused on the machine they are currently standing in front of. They can toggle between inspecting the 3D model/pin calibration and logging their set with a single swipe or tab click.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   EquipmentDetailScreen                │
│                                                        │
│  [3D Guide]  [Setup & Pins]  [Muscles]  [Workout Log]  │
│                                               │        │
│                                               ▼        │
│                          ┌──────────────────────────┐  │
│                          │  EquipmentWorkoutLogTab  │  │
│                          │  • Set Logging Table     │  │
│                          │  • Rest Countdown Timer  │  │
│                          │  • History & PR Badges   │  │
│                          └────────────┬─────────────┘  │
└───────────────────────────────────────┼────────────────┘
                                        ▼
┌────────────────────────────────────────────────────────┐
│                 GymEquipmentViewModel                  │
│  • Exposes StateFlow<List<WorkoutSetEntity>> by equip  │
│  • Rest timer clock & background coroutine countdown   │
│  • Auto-fills next set from previous values            │
│  • addSet(), updateSet(), deleteSet(), completeSet()   │
└───────────────────────────────────────┬────────────────┘
                                        ▼
┌────────────────────────────────────────────────────────┐
│                  WorkoutRepository                     │
│  • Abstract interface between Room DAO and ViewModel   │
└───────────────────────────────────────┬────────────────┘
                                        ▼
┌────────────────────────────────────────────────────────┐
│                   GymDatabase (Room)                   │
│                                                        │
│  ┌───────────────────────┐  ┌───────────────────────┐  │
│  │   WorkoutSetEntity    │  │     WorkoutSetDao     │  │
│  │  • id (PrimaryKey)    │  │  • getSetsByEquip()   │  │
│  │  • equipmentId        │  │  • insertSet()        │  │
│  │  • setNumber          │  │  • updateSet()        │  │
│  │  • weightKg / reps    │  │  • deleteSet()        │  │
│  │  • duration / distance│  │  • getPreviousSet()   │  │
│  │  • isCompleted        │  │  • getRecentHistory() │  │
│  │  • timestamp          │  └───────────────────────┘  │
│  └───────────────────────┘                             │
└────────────────────────────────────────────────────────┘
```

### Room Schema Design

```kotlin
@Entity(
    tableName = "workout_sets",
    indices = [Index(value = ["equipmentId", "timestamp"])]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipmentId: String,
    val setNumber: Int,
    val weightKg: Float = 0f,
    val reps: Int = 0,
    val durationSeconds: Int = 0,
    val distanceKm: Float = 0f,
    val inclineOrResistance: Float = 0f,
    val rpe: Float = 8f,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
```
