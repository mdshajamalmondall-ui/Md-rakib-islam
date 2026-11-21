package com.example.data.knowledge

data class BookLesson(
    val unitNumber: Int,
    val lessonNumber: Int,
    val citation: String,
    val title: String,
    val keywords: List<String>,
    val summaryContent: String
)

data class BookTopicPrompt(
    val labelBn: String,
    val queryPrompt: String
)

data class RetrievalResult(
    val hasBookMatch: Boolean,
    val citationLabel: String?,
    val contextBlock: String
)

/**
 * Internal knowledge base extracted from the user-provided textbook:
 * "BSN 3411: Medical Surgical Nursing" (Bangladesh Open University, Units 1–4, 15 Lessons).
 * Used exclusively by the AI answering pipeline and never exposed as a downloadable file.
 */
object MedicalSurgicalKnowledgeBase {

    val quickTopics: List<BookTopicPrompt> = listOf(
        BookTopicPrompt(
            labelBn = "নার্সিং প্রসেস",
            queryPrompt = "নার্সিং প্রসেসের ৫টি ধাপ ও Nursing Care Plan সহজভাবে ব্যাখ্যা করো।"
        ),
        BookTopicPrompt(
            labelBn = "পেপটিক আলসার",
            queryPrompt = "পেপটিক আলসারের কারণ, লক্ষণ ও মেডিকেল সার্জিক্যাল নার্সিং ম্যানেজমেন্ট কী?"
        ),
        BookTopicPrompt(
            labelBn = "বার্ন ম্যানেজমেন্ট",
            queryPrompt = "পোড়া বা Burn রোগীর জরুরি ১১ ধাপের নার্সিং ম্যানেজমেন্ট ও হাইপোভোলেমিক স্টেজ ব্যাখ্যা করো।"
        ),
        BookTopicPrompt(
            labelBn = "প্রি ও পোস্ট-অপ কেয়ার",
            queryPrompt = "সার্জারির আগে (Pre-operative) ও পরে (Post-operative) রোগীর নার্সিং কেয়ার কী কী?"
        ),
        BookTopicPrompt(
            labelBn = "কোমা ও ICP",
            queryPrompt = "Increased ICP এবং অচেতন (Comatose) রোগীর নার্সিং কেয়ার সম্পর্কে বলো।"
        ),
        BookTopicPrompt(
            labelBn = "হার্ট ফেইলিউর (CCF)",
            queryPrompt = "Congestive Cardiac Failure (CCF) ও Angina রোগীর লক্ষণ এবং কার্ডিয়াক নার্সিং কেয়ার কী?"
        ),
        BookTopicPrompt(
            labelBn = "রেনাল ফেইলিউর (ARF/CRF)",
            queryPrompt = "Acute Renal Failure (ARF) এর ৩টি ধাপ ও ডায়ালাইসিস-পূর্ব নার্সিং কেয়ার ব্যাখ্যা করো।"
        ),
        BookTopicPrompt(
            labelBn = "কেমোথেরাপি কেয়ার",
            queryPrompt = "ক্যান্সার রোগীর কেমোথেরাপি ও রেডিওথেরাপির পার্শ্বপ্রতিক্রিয়া ও নার্সিং কেয়ার কী?"
        )
    )

    private val lessons: List<BookLesson> = listOf(
        BookLesson(
            unitNumber = 1,
            lessonNumber = 1,
            citation = "Unit 1, Lesson 1",
            title = "Concept of Medical Surgical Nursing Care & Nursing Theories",
            keywords = listOf(
                "nursing", "medical surgical", "nightingale", "henderson", "peplau", "orem",
                "roy", "king", "rogers", "abdellah", "johnson", "roper", "theory", "model",
                "philosophy", "নার্সিং", "থিওরি", "হেন্ডারসন", "নাইটিঙ্গেল", "ওরেম", "রয়"
            ),
            summaryContent = """
                [BSN 3411 — Unit 1, Lesson 1: Concept of Nursing Care]
                - Medical Surgical Nursing deals with holistic care of adult patients with medical or surgical health problems.
                - Florence Nightingale (1860): Focuses on putting the patient in the best condition for nature to act upon him (environment, ventilation, warmth, light, hygiene).
                - Virginia Henderson (1955): Assisting the individual, sick or well, in performing 14 fundamental activities contributing to health or recovery until gaining independence.
                - Bangladesh Nursing Philosophy: Care based on dignity, holistic physical/psychological/social/spiritual needs, regardless of race, religion, or socioeconomic status.
                - Key Nursing Theories/Models: Peplau (Interpersonal), Abdellah (21 Nursing Problems), Henderson (14 Basic Needs), Johnson (Behavioral System), King (Goal Attainment), Orem (Self-Care Deficit), Roy (Adaptation Model), Roper-Logan-Tierney (Activities of Living), Martha Rogers (Unitary Human Beings).
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 1,
            lessonNumber = 2,
            citation = "Unit 1, Lesson 2",
            title = "The Nursing Process & Nursing Care Plan",
            keywords = listOf(
                "nursing process", "assessment", "diagnosis", "planning", "implementation",
                "evaluation", "care plan", "nanda", "নার্সিং প্রসেস", "কেয়ার প্ল্যান", "অ্যাসেসমেন্ট",
                "ডায়াগনোসিস", "প্ল্যানিং", "ইভালুয়েশন"
            ),
            summaryContent = """
                [BSN 3411 — Unit 1, Lesson 2: The Nursing Process]
                - 5 cyclic, systematic, problem-solving steps:
                  1. Assessment: Subjective & objective data collection (history, physical exam, vitals, lab reports).
                  2. Nursing Diagnosis: Clinical judgment about patient response to actual/potential health problems (Problem + Etiology + Defining characteristics).
                  3. Planning: Setting measurable, time-bound patient goals/outcomes and prioritizing interventions (Maslow/ABC).
                  4. Implementation: Executing planned nursing actions, delegating, and documenting care.
                  5. Evaluation: Comparing patient response against expected goals and modifying the Nursing Care Plan if goals are unmet.
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 1,
            lessonNumber = 3,
            citation = "Unit 1, Lesson 3",
            title = "Expanded Role of Medical Surgical Nursing",
            keywords = listOf(
                "expanded role", "hospital", "community", "team", "interdisciplinary",
                "multidisciplinary", "rehabilitation", "counselor", "advocate", "নার্সের ভূমিকা",
                "হাসপাতাল", "কমিউনিটি", "টিম"
            ),
            summaryContent = """
                [BSN 3411 — Unit 1, Lesson 3: Expanded Role of Nursing]
                - Roles: Caregiver, Educator, Advocate, Counselor, Manager, Coordinator, Researcher, Rehabilitator across Hospital and Community settings.
                - Healthcare Team Models: Single-disciplinary, Multidisciplinary (parallel work), and Interdisciplinary (collaborative shared decision-making centered on patient outcome).
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 1,
            lessonNumber = 4,
            citation = "Unit 1, Lesson 4",
            title = "Emergency Nursing Management",
            keywords = listOf(
                "emergency", "airway", "heimlich", "ventilation", "cpr", "hemorrhage", "bleeding",
                "shock", "heat stroke", "anaphylactic", "anaphylaxis", "food poisoning", "botulism",
                "জরুরি", "ইমার্জেন্সি", "শক", "রক্তপাত", "হিট স্ট্রোক", "অ্যানাফাইল্যাক্সিস", "হাইমলিক"
            ),
            summaryContent = """
                [BSN 3411 — Unit 1, Lesson 4: Emergency Nursing Management]
                - Airway Obstruction: Head-tilt chin-lift / jaw thrust, clear foreign body, Heimlich maneuver (abdominal thrusts) for conscious choking. Mouth-to-mouth or mouth-to-nose rescue breathing if apneic.
                - Hemorrhage: Direct firm pressure over sterile dressing, elevation of limb above heart, pressure points, avoid prolonged tourniquet unless life-threatening amputation; monitor pulse/BP for hypovolemia.
                - Shock: Elevate legs (unless head/chest injury), keep warm (avoid overheating), maintain patent airway, IV crystalloid/blood replacement, treat underlying cause.
                - Heat Stroke: Rapid cooling (ice packs to neck/axilla/groin, tepid sponge + fan) until core temp reaches 39°C (102°F) to avoid hypothermia rebound; monitor airway & seizures.
                - Anaphylactic Shock: Immediate airway support, IM/SC Epinephrine (Adrenaline), IV fluids, antihistamines, corticosteroids, oxygen.
                - Food Poisoning / Botulism: Respiratory & fluid support, gastric lavage/activated charcoal if early, antitoxin for botulism, stool/vomitus culture.
                - Psychological Care: Calm reassurance to reduce panic and sympathetic stress.
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 2,
            lessonNumber = 1,
            citation = "Unit 2, Lesson 1",
            title = "Pre-Operative Nursing Management",
            keywords = listOf(
                "pre-op", "preoperative", "surgery", "consent", "npo", "fasting", "premedication",
                "atropine", "diazepam", "pethidine", "elective", "সার্জারি", "অপারেশন",
                "প্রি-অপারেটিভ", "কনসেন্ট", "উপবাস"
            ),
            summaryContent = """
                [BSN 3411 — Unit 2, Lesson 1: Pre-Operative Nursing Management]
                - Types of Surgery: Elective (planned), Essential (necessary within weeks), Emergency (immediate life-saving).
                - Psychological Preparation: Alleviate anxiety, explain procedures, deep-breathing/coughing/leg exercises teaching before surgery.
                - Physical Preparation: Nutritional optimization (protein & Vitamin C for healing), baseline labs/ECG/CXR, Informed Consent verified BEFORE premedication.
                - Night Before & Morning of Surgery: Skin/site prep (clipping hair, antiseptic bath), NPO (fasting 6-8 hrs to prevent aspiration), bowel prep/laxative if ordered, empty bladder before OT transfer, remove dentures/jewelry/nail polish.
                - Premedication: Sedatives (Diazepam), analgesics (Pethidine), anticholinergics (Atropine to dry secretions) given 45-60 mins pre-op; raise bed rails afterwards.
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 2,
            lessonNumber = 2,
            citation = "Unit 2, Lesson 2",
            title = "Immediate Post-Operative Surgical Nursing",
            keywords = listOf(
                "post-op", "postoperative", "recovery room", "pacu", "airway", "hypoventilation",
                "spinal anesthesia", "drainage", "পোস্ট-অপারেটিভ", "রিকভারি", "স্পাইনাল", "অ্যানেস্থেসিয়া",
                "রক্তক্ষরণ"
            ),
            summaryContent = """
                [BSN 3411 — Unit 2, Lesson 2: Post-Operative Surgical Nursing]
                - Recovery Room (PACU): Vital signs every 15 minutes for the first 2 hours until stable, then q30m-q1h.
                - Airway & Ventilation: Lateral/recovery position until gag reflex returns; suction secretions; detect hypoventilation (shallow breathing, cyanosis, restlessness) early.
                - Post-op Shock & Hemorrhage: Inspect dressings & drains frequently, watch tachycardia, falling BP, cold clammy skin, pallor; notify surgeon immediately.
                - Spinal Anesthesia Care: Keep patient flat (supine) for 6-12 hours (per protocol) until SBP > 90 mmHg and full motor/sensory function returns in legs; prevent spinal headache.
                - Tubes & Drains: Maintain patency of NG tube, Foley catheter, chest/wound drains; record color, amount, and consistency.
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 2,
            lessonNumber = 3,
            citation = "Unit 2, Lesson 3",
            title = "Continuing Post-Operative Nursing Care",
            keywords = listOf(
                "post-op pain", "flatulence", "distention", "adh", "blood transfusion",
                "urinary retention", "ambulation", "wound healing", "infection", "collagen",
                "ব্যথা", "পেট ফাঁপা", "রক্ত সঞ্চালন", "ক্ষত", "ড্রেসিং", "অ্যাম্বুলেশন"
            ),
            summaryContent = """
                [BSN 3411 — Unit 2, Lesson 3: Continuing Post-Operative Nursing Care]
                - Pain Management: Acute post-op pain peaks in first 24-48 hours; assess site/intensity, give prescribed analgesics before pain becomes severe, splint incision during coughing.
                - Flatulence & Abdominal Distention: Early ambulation, turning in bed, rectal tube or NG decompression if paralytic ileus suspected.
                - Fluid Balance & Blood Transfusion: Post-op ADH secretion retains water for 24-48 hours (avoid fluid overload); monitor blood transfusion reactions (fever, chills, back pain, urticaria — stop transfusion immediately, keep vein open with NS).
                - Urinary Retention: If no voiding within 12-24 hours post-op, offer privacy, warm water over perineum, running tap sound, sit up if allowed, catheterize as last resort under strict asepsis.
                - Early Ambulation: Prevents DVT, pneumonia, constipation, and urinary retention.
                - Wound Healing: Inflammatory -> Proliferative (collagen deposition peaks days 5-10) -> Maturation; aseptic dressing, watch redness/purulent discharge/dehiscence.
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 3,
            lessonNumber = 1,
            citation = "Unit 3, Lesson 1",
            title = "Disorders of Digestive Tracts (Peptic Ulcer & Appendicitis)",
            keywords = listOf(
                "digestive", "stomatitis", "thrush", "gingivitis", "gastritis", "peptic ulcer",
                "h. pylori", "gastrectomy", "vagotomy", "pyloroplasty", "appendicitis", "mcburney",
                "peritonitis", "পেপটিক আলসার", "গ্যাস্ট্রাইটিস", "অ্যাপেন্ডিসাইটিস", "পেরিটোনাইটিস", "পাকস্থলী"
            ),
            summaryContent = """
                [BSN 3411 — Unit 3, Lesson 1: Disorders of Digestive Tracts]
                - Oral & Gastric Conditions: Stomatitis (mouth care, saline rinses), Oral Thrush (Candida — Nystatin), Gingivitis, Acute/Chronic Gastritis (avoid NSAIDs, alcohol, spicy food).
                - Peptic Ulcer Disease (Gastric & Duodenal): Etiology includes H. pylori, NSAIDs/steroids, stress, smoking, Blood group O (duodenal) vs Blood group A (gastric). Symptoms: burning epigastric pain (relieved by food in duodenal, worsened by food in gastric), heartburn, melena/hematemesis. Meds: Antacids, H2 blockers (Ranitidine), PPIs. Surgery: Subtotal gastrectomy, Vagotomy & Pyloroplasty (watch dumping syndrome: small dry low-carb meals, lie down after eating).
                - Appendicitis: RLQ pain starting periumbilically and localizing at McBurney's point, rebound tenderness, low-grade fever, leukocytosis. NEVER give laxatives or apply heat to abdomen (risk of perforation -> Peritonitis!). Keep NPO, IV fluids, prepare for appendectomy.
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 3,
            lessonNumber = 2,
            citation = "Unit 3, Lesson 2",
            title = "Fluid-Electrolyte Imbalance & Burn Management",
            keywords = listOf(
                "fluid", "electrolyte", "dehydration", "hyponatremia", "hypernatremia",
                "hypokalemia", "hyperkalemia", "burn", "burns", "hypovolemic", "diuretic",
                "parkland", "পোড়া", "বার্ন", "ইলেকট্রোলাইট", "ডিহাইড্রেশন", "পটাশিয়াম", "সোডিয়াম"
            ),
            summaryContent = """
                [BSN 3411 — Unit 3, Lesson 2: Fluid-Electrolyte Problems & Burns]
                - Electrolytes:
                  * Normal Na+: 138–145 mEq/L. Hyponatremia (<135): weakness, confusion, seizures. Hypernatremia (>145): intense thirst, dry mucosa, lethargy.
                  * Normal K+: 3.5–5.0 mEq/L. Hypokalemia (<3.5): muscle weakness, cramps, ileus, flat T wave/U wave. Hyperkalemia (>5.0): peaked T wave, cardiac arrest risk (give IV Calcium Gluconate/Insulin+Dextrose per order).
                - Burns: Partial-thickness (epidermis + part of dermis, blisters, severe pain) vs Full-thickness (all skin layers, charred/white, painless locally due to nerve destruction).
                - Burn Phases: Hypovolemic/Shock Stage (0–72 hrs: capillary leak -> edema, hemoconcentration, shock) -> Diuretic Stage (>48–72 hrs: fluid shifts back into circulation -> diuresis, risk of pulmonary edema).
                - 11-Step Burn Management: (1) Stop burning & assess airway/inhalation injury, (2) Oxygen, (3) Large-bore IV access & fluid resuscitation (Ringer's Lactate), (4) Foley catheter & hourly urine output (30-50 mL/hr), (5) Pain control (IV morphine, avoid IM in shock), (6) Tetanus prophylaxis, (7) NG tube if >20% TBSA/ileus, (8) Wound cleansing & Silver Sulfadiazine/aseptic dressing, (9) Prevent hypothermia, (10) High-protein, high-calorie nutrition, (11) Infection control & ROM exercises to prevent contractures.
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 3,
            lessonNumber = 3,
            citation = "Unit 3, Lesson 3",
            title = "Common Medical Surgical Emergencies: Trauma, Stupor & Coma",
            keywords = listOf(
                "trauma", "stupor", "coma", "consciousness", "gcs", "metabolic coma",
                "diabetic coma", "কোমা", "অচেতন", "ট্রমা", "জ্ঞান", "সচেতনতা"
            ),
            summaryContent = """
                [BSN 3411 — Unit 3, Lesson 3: Trauma, Stupor & Coma]
                - 6 Levels of Consciousness: Alert -> Lethargy/Somnolence -> Obtundation -> Stupor (responds only to vigorous painful stimuli) -> Semi-coma -> Deep Coma (no response to stimuli).
                - Etiologies of Coma: Structural (head injury, hemorrhage, tumor, stroke), Metabolic (hypoglycemia, DKA, uremia, hepatic encephalopathy), Drug/Toxic (sedatives, alcohol, opioids), Hypertensive encephalopathy, Respiratory failure (hypercapnia/hypoxia).
                - Emergency Coma Management: Secure ABCs, C-spine immobilization in trauma, check capillary blood glucose immediately, lateral recovery position, suctioning, monitor GCS & pupils, IV access, treat reversible cause.
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 4,
            lessonNumber = 1,
            citation = "Unit 4, Lesson 1",
            title = "Nursing in Oncology (Cancer, Radiotherapy & Chemotherapy)",
            keywords = listOf(
                "oncology", "cancer", "tumor", "benign", "malignant", "leukemia", "lymphoma",
                "radiotherapy", "teletherapy", "chemotherapy", "pancytopenia", "leucopenia",
                "thrombocytopenia", "alopecia", "nephrotoxicity", "cystitis", "ক্যান্সার",
                "কেমোথেরাপি", "রেডিওথেরাপি", "টিউমার", "অনকোলজি"
            ),
            summaryContent = """
                [BSN 3411 — Unit 4, Lesson 1: Nursing in Oncology]
                - Benign vs Malignant: Benign (slow growth, encapsulated, non-metastatic) vs Malignant (rapid, infiltrative, metastasizes via lymph/blood).
                - Cancer Modalities: Surgery, Radiotherapy (Teletherapy/external beam, Plesiotherapy/brachytherapy, unsealed isotopes — follow time, distance, shielding radiation safety), Chemotherapy (6 major regimens/classes).
                - Chemotherapy Toxicities & Nursing Care:
                  * Stomatitis / Mouth ulceration: Gentle saline/baking soda mouthwashes, soft toothbrush, avoid spicy/hot foods.
                  * GI Nausea/Vomiting: Prophylactic antiemetics before chemo, small frequent bland meals.
                  * Pancytopenia: Leucopenia (neutropenic precautions: handwashing, reverse isolation, avoid crowds/raw fruits, monitor fever >38°C); Thrombocytopenia (bleeding precautions: soft brush, no IM injections/rectal meds/aspirin, check petechiae/hematuria); Anemia (rest, O2, transfusion if ordered).
                  * Alopecia: Psychological support, wigs/scarves before hair loss.
                  * Organ Toxicities: Cyclophosphamide hemorrhagic cystitis (vigorous hydration + frequent voiding + Mesna); Cisplatin nephrotoxicity (hydrate, monitor BUN/Cr & urine output); Vinca alkaloids (Vincristine) peripheral neuritis/constipation (neuro checks, stool softeners).
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 4,
            lessonNumber = 2,
            citation = "Unit 4, Lesson 2",
            title = "Nursing in Neurology (Meningitis, Increased ICP & Comatose Care)",
            keywords = listOf(
                "neurology", "meningitis", "kernig", "photophobia", "icp", "increased icp",
                "coning", "cheyne-stokes", "mannitol", "dexamethasone", "csf", "lumbar puncture",
                "comatose", "tracheostomy", "craniotomy", "নিউরোলজি", "মেনিনজাইটিস", "আইসিপি",
                "মস্তিষ্ক", "অচেতন রোগী", "ট্রাকিওস্টমি"
            ),
            summaryContent = """
                [BSN 3411 — Unit 4, Lesson 2: Nursing in Neurology]
                - Meningeal Irritation: Neck stiffness (nuchal rigidity), positive Kernig's sign, Brudzinski's sign, severe headache, photophobia (keep room dim & quiet).
                - Increased Intracranial Pressure (ICP): Early signs (restlessness, headache, vomiting, pupillary changes/CN III palsy from temporal lobe herniation/"coning"), Cushing's triad (rising SBP with widening pulse pressure, bradycardia, irregular/Cheyne-Stokes respiration <12/min).
                - Medications for ICP: IV Mannitol 20% (osmotic diuretic — watch urine output & electrolytes) and Dexamethasone (steroid, onset ~12h for vasogenic edema). Elevate HOB 30°, keep neck neutral, avoid straining/suctioning >10-15s.
                - Head Trauma / LP: Watch CSF rhinorrhea or otorrhea ("halo/ring sign", glucose positive — NEVER pack nose/ear!). After Lumbar Puncture: lie flat, hydrate, watch headache/leakage.
                - Comatose Patient Care: Semi-prone/lateral position, turn every 2 hours (skin/pressure ulcer care), NG tube feeding (verify placement via aspirate pH & auscultation, elevate head 30-45°), Eye care (normal saline wash q4h, lubricant/antibiotic ointment q6h, tape eyelids if blink absent), Tracheostomy suctioning (sterile, pre-oxygenate).
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 4,
            lessonNumber = 3,
            citation = "Unit 4, Lesson 3",
            title = "Nursing in Urinary & Renal Disorders",
            keywords = listOf(
                "kidney", "renal", "urinary", "anuria", "oliguria", "arf", "crf", "uti",
                "e. coli", "mid-stream", "prostatectomy", "foley", "irrigation", "renal stone",
                "glomerulonephritis", "nephrotic", "anasarca", "কিডনি", "রেনাল", "প্রস্রাব",
                "ইউটিআই", "পাথর", "নেফ্রোটিক", "ডায়ালাইসিস"
            ),
            summaryContent = """
                [BSN 3411 — Unit 4, Lesson 3: Nursing in Urinary Disorders]
                - UTI: Common in females (short urethra), E. coli most common. Collect mid-stream clean-catch urine (deliver to lab within 30 mins), complete full 14-day antibiotic course, high fluid intake, void regularly.
                - Benign Prostatic Hyperplasia / Post-Prostatectomy: 3-way Foley catheter with continuous bladder irrigation (CBI); calculate true urine output = Total drainage minus irrigation fluid infused; keep tubing patent against clots.
                - Renal Stones (Calculi): Calcium oxalate/phosphate, Uric acid, MgNH4PO4 (struvite). Strain all urine, high fluids (2.5-3 L/day unless obstructed), pain relief, dietary modification.
                - Acute Renal Failure (ARF): Oliguric phase (<400 mL/24h -> hyperkalemia, fluid overload, uremia), Diuretic phase (high output -> hypokalemia/dehydration), Recovery/Post-diuretic phase. Nutrition: ~2000 kcal/day, 20-60g protein (high biological value), restrict K+/Na+/fluid, avoid nephrotoxic drugs & sulphas.
                - Glomerulonephritis: Post-streptococcal (high ASO titer), puffy face/periorbital edema, smoky/cola urine, hypertension. Nephrotic Syndrome: Massive proteinuria (>3.5g/day), hypoalbuminemia, generalized edema (anasarca), exclude table salt.
                - Chronic Renal Failure (CRF): Irreversible nephron loss; measure BP supine, sitting, and standing; strict I&O, daily weight, skin care for uremic pruritus, dialysis/AV fistula care (no BP/IV/venipuncture on fistula arm).
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 4,
            lessonNumber = 4,
            citation = "Unit 4, Lesson 4",
            title = "Cardiac Nursing (Angina, MI, CCF & Stroke)",
            keywords = listOf(
                "cardiac", "heart", "angina", "mi", "myocardial infarction", "ccf", "heart failure",
                "oedema", "orthopnoea", "hepatomegaly", "ascites", "ecg", "heparin", "dvt",
                "thanophobia", "হৃদরোগ", "হার্ট", "অ্যাঞ্জাইনা", "হার্ট অ্যাটাক", "হার্ট ফেইলিউর", "ইসিজি"
            ),
            summaryContent = """
                [BSN 3411 — Unit 4, Lesson 4: Cardiac Nursing]
                - Psychological Aspect: Severe anxiety and "thanophobia" (fear of impending death) in acute MI/angina — calm reassurance reduces myocardial oxygen demand.
                - Signs & Symptoms: Angina (substernal crushing pain radiating to left arm/jaw, relieved by rest/NTG), MI (persistent pain >20-30m not relieved by NTG, diaphoresis, dyspnea), CCF (orthopnoea, paroxysmal nocturnal dyspnea, cardiac/sacral oedema, elevated JVP, hepatomegaly, ascites, hepatojugular reflux, oliguria, finger clubbing in congenital/chronic hypoxia).
                - Investigations: 12-lead ECG, Echocardiogram, Chest X-ray, Cardiac enzymes (Troponin/CK-MB), Coronary Angiography.
                - Nursing Interventions: Absolute bed rest in acute phase, propped-up (High Fowler's) position for dyspnea, Oxygen therapy, continuous ECG/vital monitoring, strict I&O & fluid/salt restriction in CCF, passive/active leg exercises + prescribed Heparin/anticoagulants to prevent DVT & thromboembolism.
            """.trimIndent()
        ),
        BookLesson(
            unitNumber = 4,
            lessonNumber = 5,
            citation = "Unit 4, Lesson 5",
            title = "Nursing in Lung & Respiratory Diseases",
            keywords = listOf(
                "lung", "respiratory", "pneumonia", "asthma", "copd", "tachypnoea", "stridor",
                "chest indrawing", "sputum", "mantoux", "ppd", "bronchoscopy", "pft",
                "salbutamol", "ipratropium", "nebulizer", "suction", "postural drainage",
                "ফুসফুস", "নিউমোনিয়া", "অ্যাজমা", "শ্বাসকষ্ট", "নেবুলাইজার", "কফ"
            ),
            summaryContent = """
                [BSN 3411 — Unit 4, Lesson 5: Nursing in Lung Diseases]
                - Assessment: Count respiratory rate for a FULL 1 minute (detect tachypnoea accurately), observe stridor (upper airway), bovine cough, wheezing, and lower chest wall indrawing (severe childhood pneumonia).
                - Investigations: Chest X-ray (CXR), 3 morning sputum samples (AFB/culture), Mantoux/PPD skin test (read at 48-72h), Bronchoscopy (NPO 6h pre & post until gag reflex returns), Pulmonary Function Test (PFT).
                - Acute Management: In severe pneumonia/hypoxia, give URGENT Oxygen BEFORE waiting for antibiotics; keep airway clear via gentle suctioning & postural drainage/chest physiotherapy; Nebulized bronchodilators (Salbutamol, Ipratropium Bromide, or Adrenaline for severe croup/stridor).
                - Prevention (especially childhood ARI/pneumonia): Exclusive breastfeeding, Vitamin A supplementation, complete EPI immunization, avoiding indoor smoke.
            """.trimIndent()
        )
    )

    fun retrieveRelevantContext(query: String, maxLessons: Int = 4): RetrievalResult {
        val normalizedQuery = query.lowercase()
        val tokens = normalizedQuery
            .split(Regex("[\\s,.;:?!()/\\-]+"))
            .filter { it.length >= 2 }

        val scored = lessons.map { lesson ->
            var score = 0
            for (kw in lesson.keywords) {
                val kwLower = kw.lowercase()
                if (normalizedQuery.contains(kwLower)) {
                    score += if (kwLower.contains(" ")) 6 else 4
                } else {
                    for (token in tokens) {
                        if (kwLower.contains(token) || token.contains(kwLower)) {
                            score += 1
                        }
                    }
                }
            }
            lesson to score
        }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(maxLessons)

        if (scored.isEmpty()) {
            return RetrievalResult(
                hasBookMatch = false,
                citationLabel = null,
                contextBlock = ""
            )
        }

        val citations = scored.joinToString(" • ") { it.first.citation }
        val contextText = scored.joinToString("\n\n") { it.first.summaryContent }
        return RetrievalResult(
            hasBookMatch = true,
            citationLabel = "BSN 3411 ($citations)",
            contextBlock = contextText
        )
    }
}
