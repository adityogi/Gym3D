# Science-Based Fitness Coach & 3D Biomechanics (BWS+ Architecture)

Transform Gym3D into an evidence-based personal trainer and adaptive nutrition engine inspired by Built With Science+ (BWS+), combining 3D equipment visualization with intelligent periodization, automated progressive overload, adaptive macro adjustments, and an AI science coach.

## User Review & Critical Decisions

> [!IMPORTANT]
> Based on your confirmed choices during the clarification interview, the architecture is locked as follows:

- **Navigation Structure (Confirmed)**: Full 5-tab Material 3 bottom navigation:
  1. `Today`: Daily macro rings, active streak, daily science knowledge booster, and today's scheduled split workout.
  2. `Workouts`: Active periodized split routine (PPL, Upper/Lower, Full Body), 3D equipment visualizer, progressive overload cues, exercise swaps, and set logger.
  3. `Nutrition`: Dynamic Mifflin-St Jeor TDEE tracker, weekly calorie/macro flux check-in, and multi-meal food logger.
  4. `Analytics`: Body recomposition detector, 1RM strength curves, volume landmarks (MEV/MAV/MRV), and body metrics.
  5. `Coach`: Hybrid Science Coach equipped with pre-compiled peer-reviewed literature knowledge cards (Schoenfeld, Helms, Aragon) and live Gemini AI chat (`gemini-3.5-flash`).
- **Onboarding & Split Allocation (Confirmed)**: Interactive 5-step diagnostic quiz (Biological Sex, Age, Height, Weight, Estimated Body Fat %, Training Experience, Location/Gear, Lagging Muscle Prioritizer) that generates a custom periodized split with an option to retake or adjust anytime in Settings.
- **Coach Assistant (Confirmed)**: Hybrid architecture delivering instant answers from an offline sports science knowledge base plus live generative consultation using the Gemini API.

---

## 1. Overview & Core Concept

- **What It Does**: Unifies evidence-based training, nutrition, and biomechanics into a single pocket coach. Rather than following static spreadsheets, users receive dynamically adjusted workouts and nutritional recommendations calibrated to their weekly progress.
- **Target Audience**: Natural lifters and fitness enthusiasts who want rigorous scientific principles (progressive overload, volume landmarks, energy balance) executed seamlessly with 3D biomechanics form verification.
- **Key Value**: Replaces guesswork with algorithms: automated weight/rep progression rules, precise exercise swaps matching muscular divisions, and adaptive weekly caloric adjustments that prevent metabolic plateaus.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Diagnostic Onboarding & Split Assignment**:
   - On first launch, the user completes the 5-step diagnostic onboarding:
     - Biometrics: Sex, Age, Height, Weight.
     - Body Fat visual selector (8% to 35%+).
     - Goal: Hypertrophy (Lean Bulk), Fat Loss (Cut), or Body Recomposition.
     - Schedule: 3 Days (Full Body A/B/C), 4 Days (Upper/Lower), 5 Days (Push/Pull/Legs/Upper/Lower), 6 Days (PPL x 2).
     - Muscle Group Prioritizer: Choose up to 2 lagging muscle groups (e.g., Upper Chest, Side Delts, Lats, Glutes) to allocate 3-4 extra weekly sets while moderating opposing volume.
2. **Today Dashboard**:
   - Displays daily caloric and macronutrient progress rings (Protein, Carbs, Fat, Total Calories).
   - Card for "Today's Scheduled Session" with estimated completion time and target muscle tags.
   - **Daily 2-Minute Science Booster**: Micro-learning cards with a 1-question comprehension check to build exercise literacy.
3. **Autonomous Progressive Overload & Workout Logger**:
   - Each exercise in the workout features target rep boundaries (e.g., 8–12 reps at RIR 1–2).
   - **Double Progression Trigger**: When a user hits the top of the rep range across all prescribed sets, an automated prompt highlights: *"Target Achieved! Increase load by +2.5 kg / 5 lbs next session."*
   - **Plateau / Deload Alert**: If load or reps stall for 3 consecutive weeks, a 1-week deload protocol (50% volume, -10% load) is recommended.
   - **Biomechanical Exercise Swap**: Tap "Swap" to choose from 3 biomechanically verified alternatives sharing the exact resistance profile and muscular division (e.g., Barbell Incline Press $\leftrightarrow$ Incline Dumbbell Press $\leftrightarrow$ Low-to-High Cable Fly).
   - Directly linked to Gym3D's interactive 3D model studio and form check cards.
4. **Adaptive Nutrition Engine & Weekly Flux**:
   - Computes baseline TDEE using Mifflin-St Jeor:
     $$\text{BMR} = 10 \times \text{weight} + 6.25 \times \text{height} - 5 \times \text{age} + s$$
   - Protein: $2.2\text{ g/kg}$ ($1.0\text{ g/lb}$); Fat: $25\%$ of calories; Carbs: remainder.
   - Weekly Check-in Loop: Compares 7-day rolling scale weight against targets (e.g., $-0.5\%/\text{week}$ for cutting). If stalled, auto-decrements calories by $125\text{ kcal}$; if losing too rapidly ($>1.2\%$), auto-increments by $100\text{ kcal}$ to protect muscle tissue.
5. **Body Recomposition Detector & Analytics**:
   - Monitors 3 streams simultaneously: 7-day rolling weight average, waist circumference, and estimated 1RM compound strength (Brzycki formula).
   - Fires a **"Body Recomposition Milestone Unlocked"** badge when scale weight is steady within $\pm 1\text{ lb}$ while waist shrinks $\ge 0.5\text{ in}$ and compound 1RMs increase $\ge 5\%$ over 4 weeks.
6. **Science AI Coach ("Jeremy AI")**:
   - Quick prompt chips: *"How do I break a bench plateau?"*, *"Optimal warm-up protocol"*, *"Should I eat before morning lifting?"*, *"Creatine timing and dosing"*.
   - Generates science-backed responses referencing peer-reviewed literature (Schoenfeld, Helms, Aragon, Phillips) using `gemini-3.5-flash` with offline fallback.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Room Database Schema Expansion vs. Cloud
- **Chosen Approach**: Store user onboarding profiles, periodized workout splits, nutrition logs, daily check-ins, and body recomp measurements in the local Room SQLite database (`GymDatabase`).
- **Why**: Keeps the application completely functional offline in gym environments, ensures lightning-fast queries, and protects private body metric data.

### Decision 2: Hybrid Offline Science Knowledge Base + Gemini API
- **Chosen Approach**: Ship an offline evidence-based sports science engine with 20+ verified research cards, supplemented by live Gemini API (`gemini-3.5-flash`) calls when network/API keys are available.
- **Why**: Ensures users never encounter blank screens or errors if offline or without an active API key, while delivering full conversational AI depth when online.

---

## 4. Technical Architecture & Component Hierarchy

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │              MainNavigationHost                  │  │
│  │  ┌───────────┬───────────┬───────────┬────────┐  │  │
│  │  │  Today    │  Workout  │ Nutrition │Analytics│  │  │
│  │  │ Dashboard │ & 3D Gym  │ & TDEE    │& Recomp│  │  │
│  │  └───────────┴───────────┴───────────┴────────┘  │  │
│  │  ┌────────────────────────────────────────────┐  │  │
│  │  │               Coach AI Screen              │  │  │
│  │  └────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────┐  │  │
│  │  │            Onboarding Quiz Dialog          │  │  │
│  │  └────────────────────────────────────────────┘  │  │
│  └─────────────────────────┬────────────────────────┘  │
└────────────────────────────┼───────────────────────────┘
                             ▼
┌────────────────────────────────────────────────────────┐
│                   ViewModel Layer                      │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │              ScienceCoachViewModel               │  │
│  │  • User Profile & Diagnostic Assessment State    │  │
│  │  • Generated Periodized Split & Workout Program  │  │
│  │  • Progressive Overload & Exercise Swap Engine   │  │
│  │  • Adaptive Nutrition BMR/TDEE & Weekly Check-ins│  │
│  │  • Body Recomp Calculations (Weight + Waist + 1RM)│ │
│  │  • Gemini AI Coach Service & Offline Q&A Engine  │  │
│  └─────────────────────────┬────────────────────────┘  │
└────────────────────────────┼───────────────────────────┘
                             ▼
┌────────────────────────────────────────────────────────┐
│                     Data Layer                         │
│                                                        │
│  ┌───────────────────────┐  ┌───────────────────────┐  │
│  │  GymDatabase (Room)   │  │  SportsScienceEngine   │  │
│  │  • UserProfileEntity  │  │  • Exercise Swap Maps  │  │
│  │  • WorkoutSetEntity   │  │  • Volume Landmarks   │  │
│  │  • NutritionLogEntity │  │  • Research Articles   │  │
│  │  • BodyMetricEntity   │  │  • Double Progression  │  │
│  └───────────────────────┘  └───────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

### Core Entities & Data Structures

- `UserProfileEntity`: Age, Sex, Height, Weight, Body Fat %, Goal (Bulk/Cut/Recomp), Experience, Split Preference (3-6 days), Prioritized Muscles, Weekly Target Calories, Protein, Carbs, Fats.
- `NutritionLogEntity`: Date, Meal Name, Calories, Protein, Carbs, Fats.
- `BodyMetricEntity`: Timestamp, Weight, Waist Circumference, Compound 1RM.
- `PeriodizedSplit`: Workouts with designated exercises, target rep brackets (e.g. 8–12), volume landmarks (MEV/MAV/MRV), and verified biomechanical alternatives.
