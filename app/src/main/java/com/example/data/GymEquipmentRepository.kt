package com.example.data

import com.example.model.Equipment
import com.example.model.EquipmentCategory
import com.example.model.FormMistake
import com.example.model.MuscleActivation
import com.example.model.MuscleGroup
import com.example.model.MuscleRole
import com.example.model.PinAdjustment
import com.example.model.SetupStep

object GymEquipmentRepository {

    val allEquipment: List<Equipment> = listOf(
        // 1. Barbell Flat Bench Press
        Equipment(
            id = "bench_press",
            name = "Barbell Flat Bench Press",
            subtitle = "The foundational upper-body compound press",
            category = EquipmentCategory.FREE_WEIGHTS,
            difficulty = "Intermediate",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.ARMS, MuscleGroup.SHOULDERS),
            description = "The barbell bench press builds chest thickness, anterior deltoid strength, and triceps power. The 3D model shows proper arch, elbow tuck (approx 45-60 degrees), and the curved J-path bar trajectory.",
            setupSteps = listOf(
                SetupStep(
                    stepNumber = 1,
                    title = "Bench Alignment & Eyes Under Bar",
                    instruction = "Lie flat with eyes directly aligned beneath the racked bar. Plant both feet firmly into the gym floor.",
                    recommendedValue = "5-Point Contact (Head, Shoulders, Glutes, Left Foot, Right Foot)"
                ),
                SetupStep(
                    stepNumber = 2,
                    title = "Safety Bar Height Pin",
                    instruction = "Set the horizontal safety spotter pins 1-2 inches below your chest height when arched so you can roll the bar clear if you fail.",
                    pinId = "safety_pin_l",
                    recommendedValue = "Hole #4 (Sternum clearance)"
                ),
                SetupStep(
                    stepNumber = 3,
                    title = "Grip Width & Wrist Stance",
                    instruction = "Grip the bar slightly wider than shoulder width (using the knurling ring guides). Wrap thumbs securely (no suicide grip) with wrists straight.",
                    recommendedValue = "1.5x Shoulder Width"
                ),
                SetupStep(
                    stepNumber = 4,
                    title = "Scapular Retraction",
                    instruction = "Squeeze shoulder blades back and down into the pad like pinching a pencil between them. Maintain a natural lumbar arch.",
                    recommendedValue = "Retracted & Depressed Scapulae"
                )
            ),
            mistakes = listOf(
                FormMistake(
                    title = "Flaring Elbows to 90°",
                    severity = "High Risk",
                    mistakeDetail = "Flaring upper arms perpendicular to the torso places excessive impingement torque on the rotator cuff and anterior shoulder capsule.",
                    correctiveAction = "Tuck elbows to roughly 45° to 60° relative to your torso. Think of shaping an arrow rather than a 'T'.",
                    injuryRisk = "Subacromial shoulder impingement and rotator cuff strains"
                ),
                FormMistake(
                    title = "Bouncing the Bar Off the Ribs",
                    severity = "High Risk",
                    mistakeDetail = "Dropping the bar rapidly and bouncing it off the sternum relies on rib elasticity rather than muscular eccentric tension.",
                    correctiveAction = "Control the descent for a 2-second negative. Lightly touch the lower sternum with a momentary pause before driving up.",
                    injuryRisk = "Sternal bruising, rib cartilage tears, wrist sprains"
                ),
                FormMistake(
                    title = "Lifting Glutes Off the Bench",
                    severity = "Moderate Risk",
                    mistakeDetail = "Excessively hyperextending the lower back so glutes float in the air turns the exercise into a decline press and stresses the lumbar spine.",
                    correctiveAction = "Keep glutes glued to the bench pad while creating leg drive by pushing through heels into the floor.",
                    injuryRisk = "Lumbar spine facet joint strain"
                )
            ),
            muscleActivations = listOf(
                MuscleActivation(MuscleGroup.CHEST, MuscleRole.PRIMARY_AGONIST, 95, "Pectoralis major (sternal & clavicular heads) under peak contraction at midpoint"),
                MuscleActivation(MuscleGroup.ARMS, MuscleRole.SYNERGIST, 75, "Triceps brachii (lateral and medial heads) locks out the final pressing 30%"),
                MuscleActivation(MuscleGroup.SHOULDERS, MuscleRole.SYNERGIST, 65, "Anterior deltoids drive flexion through shoulder joint"),
                MuscleActivation(MuscleGroup.BACK, MuscleRole.STABILIZER, 45, "Latissimus dorsi and rhomboids provide a firm pressing shelf")
            ),
            pins = listOf(
                PinAdjustment("safety_pin_l", "Left Safety Bar Notch", "Hole 4", "1 to 8", "Set 1 inch below arched chest level"),
                PinAdjustment("safety_pin_r", "Right Safety Bar Notch", "Hole 4", "1 to 8", "Must match left side exactly")
            ),
            defaultCameraYaw = 40f,
            defaultCameraPitch = 22f
        ),

        // 2. Power Squat Rack / Barbell Back Squat
        Equipment(
            id = "squat_rack",
            name = "Power Squat Rack",
            subtitle = "Full-body compound barbell cage",
            category = EquipmentCategory.FREE_WEIGHTS,
            difficulty = "Intermediate",
            primaryMuscle = MuscleGroup.LEGS,
            secondaryMuscles = listOf(MuscleGroup.CORE, MuscleGroup.BACK),
            description = "The power cage enables safe, heavy barbell squats with dual steel safety spotter bars. Visualizes the hip-hinge mechanics, knee flexion alignment tracking over toes, and parallel depth.",
            setupSteps = listOf(
                SetupStep(
                    stepNumber = 1,
                    title = "J-Cup Bar Rest Height",
                    instruction = "Set the bar hooks so the barbell sits at mid-chest level. You should never have to tiptoe to unrack or rerack the bar.",
                    recommendedValue = "Mid-Sternum / Collarbone notch"
                ),
                SetupStep(
                    stepNumber = 2,
                    title = "Safety Spotter Arms",
                    instruction = "Position the horizontal safety arms so they sit 2 inches below your lowest point at full parallel squat depth.",
                    pinId = "safety_spotter",
                    recommendedValue = "Hole #11"
                ),
                SetupStep(
                    stepNumber = 3,
                    title = "Bar Placement (High Bar vs Low Bar)",
                    instruction = "Rest the bar across upper trapezius (high bar) or across rear deltoids (low bar). Pull the bar down into your back to engage lats.",
                    recommendedValue = "Upper Trapezius Shelf"
                ),
                SetupStep(
                    stepNumber = 4,
                    title = "Foot Stance & Stride",
                    instruction = "Take 2-3 clean steps back. Feet shoulder-width apart, toes flared slightly out 15-30 degrees.",
                    recommendedValue = "Shoulder-width with 20° toe out"
                )
            ),
            mistakes = listOf(
                FormMistake(
                    title = "Knee Valgus (Knees Caving Inward)",
                    severity = "High Risk",
                    mistakeDetail = "Allowing knees to collapse inward on the ascent due to weak gluteus medius recruitment.",
                    correctiveAction = "Actively push knees outward in line with the second toe throughout the descent and drive.",
                    injuryRisk = "MCL and meniscus shear strain, patellofemoral pain syndrome"
                ),
                FormMistake(
                    title = "Good-Morning Squat (Hips Shoot Up Early)",
                    severity = "High Risk",
                    mistakeDetail = "Hips rise faster than the chest out of the hole, turning the movement into a stiff-legged hip extension.",
                    correctiveAction = "Drive your traps up into the bar simultaneously with your legs to rise as one cohesive unit.",
                    injuryRisk = "Lumbar herniation and erector spinae spasms"
                ),
                FormMistake(
                    title = "Heels Lifting Off the Floor",
                    severity = "Moderate Risk",
                    mistakeDetail = "Weight shifting forward onto toes due to tight ankle dorsiflexion.",
                    correctiveAction = "Distribute pressure evenly across the tripod foot (heel, big toe knuckle, pinky toe knuckle).",
                    injuryRisk = "Patellar tendon irritation"
                )
            ),
            muscleActivations = listOf(
                MuscleActivation(MuscleGroup.LEGS, MuscleRole.PRIMARY_AGONIST, 95, "Quadriceps (rectus femoris, vastus lateralis) & Gluteus Maximus drive knee & hip extension"),
                MuscleActivation(MuscleGroup.CORE, MuscleRole.STABILIZER, 80, "Transverse abdominis and obliques brace intra-abdominal pressure against the spine"),
                MuscleActivation(MuscleGroup.BACK, MuscleRole.STABILIZER, 70, "Erector spinae maintain rigid spinal upright posture"),
                MuscleActivation(MuscleGroup.LEGS, MuscleRole.SYNERGIST, 65, "Hamstrings and adductor magnus stabilize the pelvis at deep parallel")
            ),
            pins = listOf(
                PinAdjustment("safety_spotter", "Safety Spotter Arms", "Hole 11", "1 to 20", "Align 2 inches below femur at parallel")
            ),
            defaultCameraYaw = 35f,
            defaultCameraPitch = 18f
        ),

        // 3. Dumbbell Incline Bench
        Equipment(
            id = "incline_dumbbell",
            name = "Dumbbell Incline Bench Press",
            subtitle = "Adjustable angle upper-chest builder",
            category = EquipmentCategory.FREE_WEIGHTS,
            difficulty = "Beginner Friendly",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
            description = "Features an adjustable pin backrest to target clavicular (upper) pectoral fibers. Visualizes dumbbell convergence without clanking at peak contraction.",
            setupSteps = listOf(
                SetupStep(
                    stepNumber = 1,
                    title = "Bench Incline Pin Selection",
                    instruction = "Pull the spring pin under the backrest and select 30° to 45°. Angles above 45° recruit predominantly anterior deltoids.",
                    pinId = "incline_pin",
                    recommendedValue = "30° to 45° Notch"
                ),
                SetupStep(
                    stepNumber = 2,
                    title = "Seat Angle Adjustment",
                    instruction = "Tilt the seat pad slightly upward by one notch to prevent your hips from sliding forward during pressing.",
                    recommendedValue = "1st Incline Notch (+15°)"
                ),
                SetupStep(
                    stepNumber = 3,
                    title = "Kicking Dumbbells to Shoulders",
                    instruction = "Rest dumbbells on knees while seated, then kick one knee up at a time as you lean back onto the pad.",
                    recommendedValue = "Controlled Knee-Kick"
                )
            ),
            mistakes = listOf(
                FormMistake(
                    title = "Clanking Dumbbells at the Top",
                    severity = "Efficiency Loss",
                    mistakeDetail = "Smashing dumbbells together at the top removes tension from the chest at peak contraction.",
                    correctiveAction = "Stop dumbbells 2-3 inches apart overhead while maintaining deliberate chest squeeze.",
                    injuryRisk = "Wrist jarring and loss of muscular tension"
                ),
                FormMistake(
                    title = "Setting Bench Angle Too Steep (>60°)",
                    severity = "Moderate Risk",
                    mistakeDetail = "Too steep an incline shifts emphasis away from pectorals onto the shoulder joints.",
                    correctiveAction = "Keep angle between 30° and 45° for optimal clavicular pectoral activation.",
                    injuryRisk = "Anterior shoulder impingement"
                )
            ),
            muscleActivations = listOf(
                MuscleActivation(MuscleGroup.CHEST, MuscleRole.PRIMARY_AGONIST, 90, "Clavicular (upper) head of pectoralis major"),
                MuscleActivation(MuscleGroup.SHOULDERS, MuscleRole.SYNERGIST, 70, "Anterior deltoids assist arm elevation"),
                MuscleActivation(MuscleGroup.ARMS, MuscleRole.SYNERGIST, 65, "Triceps brachii powers elbow lockout")
            ),
            pins = listOf(
                PinAdjustment("incline_pin", "Backrest Angle Pin", "30°", "Flat (0°) to 75°", "Choose 30° for upper chest isolation")
            ),
            defaultCameraYaw = 42f,
            defaultCameraPitch = 20f
        ),

        // 4. Cable Lat Pulldown Machine
        Equipment(
            id = "lat_pulldown",
            name = "Cable Lat Pulldown Machine",
            subtitle = "Selectorized overhead back width builder",
            category = EquipmentCategory.CABLE_MACHINES,
            difficulty = "Beginner Friendly",
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = listOf(MuscleGroup.ARMS, MuscleGroup.SHOULDERS),
            description = "The lat pulldown machine uses a top pulley with a selectorized weight stack and padded knee rollers to build back width (V-taper) and bicep pulling strength.",
            setupSteps = listOf(
                SetupStep(
                    stepNumber = 1,
                    title = "Thigh Roller Pad Height Pin",
                    instruction = "Pull the spring knob on the thigh pads and adjust so the rollers lock your thighs firmly down with feet flat on the floor.",
                    pinId = "thigh_pin",
                    recommendedValue = "Notch 3 (Snug against thighs)"
                ),
                SetupStep(
                    stepNumber = 2,
                    title = "Weight Stack Selector Pin",
                    instruction = "Insert the magnetic selector pin into the desired plate weight on the vertical weight stack.",
                    pinId = "weight_pin",
                    recommendedValue = "50% Bodyweight for warm-up"
                ),
                SetupStep(
                    stepNumber = 3,
                    title = "Wide Grip Bar Attachment",
                    instruction = "Grip the bar on the outer bends slightly wider than shoulder width. Sit down smoothly into the locked thigh pads.",
                    recommendedValue = "Overhand Thumbless or Closed Grip"
                )
            ),
            mistakes = listOf(
                FormMistake(
                    title = "Pulling Behind the Neck",
                    severity = "High Risk",
                    mistakeDetail = "Pulling the bar behind the neck forces extreme cervical spine flexion and excessive external shoulder rotation.",
                    correctiveAction = "Always pull the bar down in front of your chin to lightly touch your upper collarbone.",
                    injuryRisk = "Cervical disc compression and rotator cuff impingement"
                ),
                FormMistake(
                    title = "Swinging Torso Backward 45°",
                    severity = "Efficiency Loss",
                    mistakeDetail = "Leaning back excessively uses hip momentum and turns the movement into a sloppy row.",
                    correctiveAction = "Maintain a slight 10-15° torso lean with chest proud, driving elbows straight down towards your back pockets.",
                    injuryRisk = "Lower back hyperextension"
                )
            ),
            muscleActivations = listOf(
                MuscleActivation(MuscleGroup.BACK, MuscleRole.PRIMARY_AGONIST, 95, "Latissimus dorsi fibers driving scapular downward rotation and adduction"),
                MuscleActivation(MuscleGroup.BACK, MuscleRole.SYNERGIST, 75, "Teres major, rhomboids, and lower trapezius"),
                MuscleActivation(MuscleGroup.ARMS, MuscleRole.SYNERGIST, 70, "Biceps brachii and brachialis flexing the elbow")
            ),
            pins = listOf(
                PinAdjustment("thigh_pin", "Thigh Roller Pin", "Notch 3", "1 to 6", "Snug against thighs so legs cannot lift"),
                PinAdjustment("weight_pin", "Weight Selector Pin", "60 kg", "5 kg to 120 kg", "Magnetic locking pin into weight stack")
            ),
            defaultCameraYaw = 38f,
            defaultCameraPitch = 15f
        ),

        // 5. Seated Cable Row
        Equipment(
            id = "seated_row",
            name = "Seated Cable Row",
            subtitle = "Horizontal pulling machine for back thickness",
            category = EquipmentCategory.CABLE_MACHINES,
            difficulty = "Beginner Friendly",
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = listOf(MuscleGroup.ARMS, MuscleGroup.CORE),
            description = "Features a low pulley, footplates, and a sliding rail. Visualizes upright spinal posture, full lat stretch on the forward reach, and powerful scapular retraction.",
            setupSteps = listOf(
                SetupStep(
                    stepNumber = 1,
                    title = "Foot Placement on Footplates",
                    instruction = "Place feet on angled footplates with knees softly bent (never locked out).",
                    recommendedValue = "Mid-Foot Center, Knees 15° Bent"
                ),
                SetupStep(
                    stepNumber = 2,
                    title = "Grip & Unrack",
                    instruction = "Grip V-bar or straight handle, keep back flat, and push through legs to slide into starting upright position.",
                    recommendedValue = "Upright Torso, Neutral Spine"
                ),
                SetupStep(
                    stepNumber = 3,
                    title = "Row Path to Navel",
                    instruction = "Pull the handle horizontally toward your belly button while driving elbows close to your ribcage.",
                    recommendedValue = "Direct to Lower Abdomen"
                )
            ),
            mistakes = listOf(
                FormMistake(
                    title = "Rounding the Lumbar Spine on the Reach",
                    severity = "High Risk",
                    mistakeDetail = "Slumping shoulders and rounding the lower back at the stretch exposes discs to severe shear stress.",
                    correctiveAction = "Hinge strictly at the hips while maintaining a neutral, braced spine.",
                    injuryRisk = "Lumbar herniation and disc bulge"
                ),
                FormMistake(
                    title = "Shrugging Shoulders Into Ears",
                    severity = "Efficiency Loss",
                    mistakeDetail = "Upper traps taking over the row instead of the middle/lower back.",
                    correctiveAction = "Keep shoulders pressed down away from your ears before pulling.",
                    injuryRisk = "Neck tightness and upper trap spasms"
                )
            ),
            muscleActivations = listOf(
                MuscleActivation(MuscleGroup.BACK, MuscleRole.PRIMARY_AGONIST, 90, "Middle trapezius, rhomboids, and lower latissimus dorsi"),
                MuscleActivation(MuscleGroup.BACK, MuscleRole.SYNERGIST, 70, "Rear deltoids and erector spinae"),
                MuscleActivation(MuscleGroup.ARMS, MuscleRole.SYNERGIST, 65, "Brachioradialis and biceps")
            ),
            pins = listOf(
                PinAdjustment("weight_pin", "Weight Pin", "55 kg", "5 kg to 100 kg", "Select weight with magnetic pin")
            ),
            defaultCameraYaw = 45f,
            defaultCameraPitch = 20f
        ),

        // 6. Cable Pec Fly / Functional Trainer
        Equipment(
            id = "cable_fly",
            name = "Dual Cable Pec Fly / Crossover",
            subtitle = "Multi-angle chest isolation & crossover machine",
            category = EquipmentCategory.CABLE_MACHINES,
            difficulty = "Intermediate",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.CORE),
            description = "Dual height-adjustable cable towers deliver constant tension throughout the entire horizontal adduction range of motion. Shows proper slight elbow bend and torso brace.",
            setupSteps = listOf(
                SetupStep(
                    stepNumber = 1,
                    title = "Carriage Pulley Height Pin",
                    instruction = "Release the carriage pull-pin and align both left and right pulleys at chest height (or high for decline fly, low for incline fly).",
                    pinId = "pulley_l",
                    recommendedValue = "Notch 8 (Mid-Chest Level)"
                ),
                SetupStep(
                    stepNumber = 2,
                    title = "Handle Attachment & Stance",
                    instruction = "Clip single D-handles onto carabiners. Step forward into a staggered lunge stance for a solid base.",
                    recommendedValue = "Staggered Lunge Stance"
                ),
                SetupStep(
                    stepNumber = 3,
                    title = "Fixed Elbow Bend",
                    instruction = "Maintain a soft 10-15° bend in both elbows like hugging a wide tree trunk.",
                    recommendedValue = "Fixed 15° Soft Bend"
                )
            ),
            mistakes = listOf(
                FormMistake(
                    title = "Turning Flyes Into a Press",
                    severity = "Efficiency Loss",
                    mistakeDetail = "Bending and straightening elbows excessively turns the isolation movement into a pressing exercise.",
                    correctiveAction = "Lock the elbow angle statically; all motion must occur strictly at the shoulder joint.",
                    injuryRisk = "Reduced chest isolation and wrist strain"
                ),
                FormMistake(
                    title = "Over-Stretching Arms Past Torso Plane",
                    severity = "High Risk",
                    mistakeDetail = "Letting heavy cables pull hands far behind the back over-stretches the anterior shoulder capsule.",
                    correctiveAction = "Stop the eccentric stretch once your elbows align with your torso plane.",
                    injuryRisk = "Anterior capsule laxity and shoulder dislocations"
                )
            ),
            muscleActivations = listOf(
                MuscleActivation(MuscleGroup.CHEST, MuscleRole.PRIMARY_AGONIST, 95, "Sternal head of pectoralis major (peak horizontal adduction)"),
                MuscleActivation(MuscleGroup.SHOULDERS, MuscleRole.SYNERGIST, 60, "Anterior deltoids"),
                MuscleActivation(MuscleGroup.CORE, MuscleRole.STABILIZER, 70, "Core musculature stabilizing against bilateral cable pull")
            ),
            pins = listOf(
                PinAdjustment("pulley_l", "Left Pulley Carriage", "Notch 8", "1 to 16", "Chest level for standard fly"),
                PinAdjustment("pulley_r", "Right Pulley Carriage", "Notch 8", "1 to 16", "Ensure matching height")
            ),
            defaultCameraYaw = 32f,
            defaultCameraPitch = 16f
        ),

        // 7. Commercial Treadmill
        Equipment(
            id = "treadmill",
            name = "Commercial Motorized Treadmill",
            subtitle = "Cardiovascular running & incline trainer",
            category = EquipmentCategory.CARDIO,
            difficulty = "Beginner Friendly",
            primaryMuscle = MuscleGroup.FULL_BODY,
            secondaryMuscles = listOf(MuscleGroup.LEGS, MuscleGroup.CORE),
            description = "High-end running deck with powered motorized belt, motorized incline ramp, and digital console. Highlights safety clip attachment and midfoot strike mechanics.",
            setupSteps = listOf(
                SetupStep(
                    stepNumber = 1,
                    title = "Red Emergency Lanyard Clip",
                    instruction = "Always clip the magnetic safety cord to your waistband or shirt before pressing start. If you fall, it instantly stops the belt.",
                    pinId = "safety_clip",
                    recommendedValue = "Clipped to Waistband"
                ),
                SetupStep(
                    stepNumber = 2,
                    title = "Straddle Belt on Startup",
                    instruction = "Stand with feet on the solid side deck rails before hitting 'Quick Start'. Step onto the belt only once it moves at walking speed.",
                    recommendedValue = "Straddle Side Footrails"
                ),
                SetupStep(
                    stepNumber = 3,
                    title = "Incline & Speed Calibration",
                    instruction = "Set incline to at least 1.0% to mirror outdoor wind resistance and reduce patellar tendon impact.",
                    pinId = "incline_ctrl",
                    recommendedValue = "1.0% Incline Default"
                )
            ),
            mistakes = listOf(
                FormMistake(
                    title = "Holding Onto Handrails While Running",
                    severity = "Efficiency Loss",
                    mistakeDetail = "Gripping the front or side handrails alters natural running biomechanics, burns fewer calories, and causes slouching.",
                    correctiveAction = "Let arms swing naturally at 90° angles synchronized with opposite leg strides.",
                    injuryRisk = "Upper neck stiffness and hip impingement"
                ),
                FormMistake(
                    title = "Over-striding (Heavy Heel Striking)",
                    severity = "Moderate Risk",
                    mistakeDetail = "Landing with feet far ahead of center-of-mass sends shockwaves straight up tibia into knees.",
                    correctiveAction = "Aim for a cadence of 160-180 steps/min, landing with feet directly beneath hips on the midfoot.",
                    injuryRisk = "Shin splints and runner's knee"
                )
            ),
            muscleActivations = listOf(
                MuscleActivation(MuscleGroup.FULL_BODY, MuscleRole.PRIMARY_AGONIST, 95, "Cardiovascular and aerobic pulmonary endurance"),
                MuscleActivation(MuscleGroup.LEGS, MuscleRole.PRIMARY_AGONIST, 85, "Calves (gastrocnemius/soleus), quads, and glutes"),
                MuscleActivation(MuscleGroup.CORE, MuscleRole.STABILIZER, 60, "Spinal erectors and abdominals maintaining upright running posture")
            ),
            pins = listOf(
                PinAdjustment("safety_clip", "Magnetic Safety Lanyard", "Attached", "On/Off", "Must clip to shirt or waistband"),
                PinAdjustment("incline_ctrl", "Incline Control", "1.0%", "0% to 15%", "Minimum 1.0% recommended for natural gait")
            ),
            defaultCameraYaw = 40f,
            defaultCameraPitch = 22f
        ),

        // 8. Stationary Exercise Bike
        Equipment(
            id = "exercise_bike",
            name = "Stationary Ergometer Exercise Bike",
            subtitle = "Low-impact lower-body cardiovascular conditioner",
            category = EquipmentCategory.CARDIO,
            difficulty = "Beginner Friendly",
            primaryMuscle = MuscleGroup.LEGS,
            secondaryMuscles = listOf(MuscleGroup.FULL_BODY, MuscleGroup.CORE),
            description = "Flywheel-driven cycling ergometer. Visualizes seat height alignment (hip level), pedal stroke circles, and handlebar posture.",
            setupSteps = listOf(
                SetupStep(
                    stepNumber = 1,
                    title = "Saddle Height Adjustment",
                    instruction = "Stand beside bike. Loosen the seat pin and align the top of the saddle with your hip bone (iliac crest).",
                    pinId = "seat_pin",
                    recommendedValue = "Hip Bone Level (Iliac Crest)"
                ),
                SetupStep(
                    stepNumber = 2,
                    title = "Knee Angle at Bottom of Pedal Stroke",
                    instruction = "Sit on bike. Place heel on pedal at bottom dead center; leg should be almost fully straight, giving a slight 25-30° bend when clipped in.",
                    recommendedValue = "25° to 30° Soft Knee Flexion"
                ),
                SetupStep(
                    stepNumber = 3,
                    title = "Handlebar Height & Fore/Aft",
                    instruction = "Adjust handlebar height so your spine remains relaxed with a slight elbow bend (not hunched).",
                    pinId = "handle_pin",
                    recommendedValue = "Level with or slightly above saddle"
                )
            ),
            mistakes = listOf(
                FormMistake(
                    title = "Saddle Too Low (Squat Riding)",
                    severity = "High Risk",
                    mistakeDetail = "Riding with a seat that is too low prevents full knee extension and creates huge compressive forces on the patellofemoral joint.",
                    correctiveAction = "Raise the seat so your knee is only softly bent (not cramped) at the bottom of the stroke.",
                    injuryRisk = "Anterior knee pain and patellar tendinopathy"
                ),
                FormMistake(
                    title = "Death-Gripping Handlebars",
                    severity = "Efficiency Loss",
                    mistakeDetail = "Putting all upper-body weight into wrists causes numbness and neck tension.",
                    correctiveAction = "Rest hands gently on grips; support upper body weight through core activation.",
                    injuryRisk = "Carpal tunnel tingling and trapezius stiffness"
                )
            ),
            muscleActivations = listOf(
                MuscleActivation(MuscleGroup.LEGS, MuscleRole.PRIMARY_AGONIST, 90, "Quadriceps drive downstroke (12 to 5 o'clock)"),
                MuscleActivation(MuscleGroup.LEGS, MuscleRole.SYNERGIST, 75, "Glutes and hamstrings pull through bottom and upstroke"),
                MuscleActivation(MuscleGroup.FULL_BODY, MuscleRole.PRIMARY_AGONIST, 90, "Cardiovascular aerobic conditioning")
            ),
            pins = listOf(
                PinAdjustment("seat_pin", "Seat Post Height Pin", "Level 7", "1 to 14", "Set to standing hip-bone level"),
                PinAdjustment("handle_pin", "Handlebar Reach Knob", "Neutral", "Forward/Back", "Ensure slight elbow bend")
            ),
            defaultCameraYaw = 38f,
            defaultCameraPitch = 18f
        ),

        // 9. Concept Rowing Machine
        Equipment(
            id = "rowing_machine",
            name = "Concept Air Rowing Ergometer",
            subtitle = "Total-body power and endurance ergometer",
            category = EquipmentCategory.CARDIO,
            difficulty = "Intermediate",
            primaryMuscle = MuscleGroup.FULL_BODY,
            secondaryMuscles = listOf(MuscleGroup.BACK, MuscleGroup.LEGS, MuscleGroup.ARMS),
            description = "The indoor rowing machine provides a full-body workout engaging 86% of the body's musculature. Visualizes the four distinct stroke phases: Catch, Drive (Legs-Hips-Arms), Finish, and Recovery (Arms-Hips-Legs).",
            setupSteps = listOf(
                SetupStep(
                    stepNumber = 1,
                    title = "Footplate Heel Cup Adjustment",
                    instruction = "Adjust footplate setting so the strap crosses firmly over the widest part of your foot (across the shoelace base).",
                    pinId = "strap_pin",
                    recommendedValue = "Notch 3-4 (Strap over midfoot ball)"
                ),
                SetupStep(
                    stepNumber = 2,
                    title = "Damper Setting Lever",
                    instruction = "Set the flywheel damper lever between 3 and 5. Settings 8-10 simulate a heavy barge and cause premature fatigue.",
                    pinId = "damper_pin",
                    recommendedValue = "Damper Setting 4 to 5 (Drag 115-130)"
                ),
                SetupStep(
                    stepNumber = 3,
                    title = "Stroke Sequence Order",
                    instruction = "Drive: Legs push first, then hip swing back (1 o'clock), then arms pull handle to ribs. Recovery: Reverse order exactly (Arms, Hips, Legs).",
                    recommendedValue = "Legs -> Core -> Arms Sequence"
                )
            ),
            mistakes = listOf(
                FormMistake(
                    title = "Bending Knees Early on Recovery (Shooting the Slide)",
                    severity = "Efficiency Loss",
                    mistakeDetail = "Bending knees before hands pass forward forces the rower to hurdle the handle over their knees.",
                    correctiveAction = "Extend arms fully, pivot torso forward from hips, and ONLY THEN bend knees to roll forward.",
                    injuryRisk = "Disrupted stroke rhythm and shin bumping"
                ),
                FormMistake(
                    title = "Setting Damper to 10 Thinking It Builds More Muscle",
                    severity = "Moderate Risk",
                    mistakeDetail = "Heavy damper settings overload lower back spinal erectors rather than training smooth aerobic power.",
                    correctiveAction = "Keep damper on 4-5. Regulate resistance through how hard you accelerate with your legs.",
                    injuryRisk = "Lumbar strain"
                )
            ),
            muscleActivations = listOf(
                MuscleActivation(MuscleGroup.FULL_BODY, MuscleRole.PRIMARY_AGONIST, 95, "Total-body aerobic power recruiting 86% of muscle groups"),
                MuscleActivation(MuscleGroup.LEGS, MuscleRole.PRIMARY_AGONIST, 90, "Quads and glutes deliver 60% of stroke power drive"),
                MuscleActivation(MuscleGroup.BACK, MuscleRole.SYNERGIST, 80, "Lats, rhomboids, and erectors transfer power and finish stroke"),
                MuscleActivation(MuscleGroup.ARMS, MuscleRole.SYNERGIST, 65, "Forearms, biceps, and rear delts pull handle home")
            ),
            pins = listOf(
                PinAdjustment("damper_pin", "Damper Air Flow Lever", "Setting 4", "1 to 10", "Optimal drag factor 125"),
                PinAdjustment("strap_pin", "Footplate Notch", "Hole 3", "1 to 7", "Strap across ball of foot")
            ),
            defaultCameraYaw = 45f,
            defaultCameraPitch = 20f
        )
    )

    fun getEquipmentById(id: String): Equipment {
        return allEquipment.firstOrNull { it.id == id } ?: allEquipment.first()
    }
}
