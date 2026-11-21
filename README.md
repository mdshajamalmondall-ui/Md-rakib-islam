# নার্সিং এআই (Nursing AI)

বাংলাদেশি নার্স এবং নার্সিং শিক্ষার্থীদের জন্য তৈরি একটি আধুনিক অ্যান্ড্রয়েড অ্যাপ—যেখানে ক্লিনিক্যাল সার্চ, মেডিকেল-সার্জিক্যাল রেফারেন্স, মেডিসিন ও ডোজ ট্র্যাকিং এবং ব্যক্তিগত নোট ব্যবস্থাপনা একসাথে পাওয়া যায়।

## মূল ফিচারসমূহ

1. **চ্যাট (AI নার্সিং সার্চ ও চ্যাট)**
   - ChatGPT/Gemini-স্টাইল সার্চ ও রিয়েল-টাইম স্ট্রিমিং উত্তর।
   - ৩টি উত্তর গভীরতা মোড: **সংক্ষিপ্ত**, **মাঝারি**, এবং **বিস্তারিত**।
   - **BSN 3411: Medical Surgical Nursing** পাঠ্যবইয়ের ৪টি ইউনিট (১৫টি লেসন)-এর অভ্যন্তরীণ নলেজ বেস থেকে প্রাসঙ্গিক ইউনিট/লেসন রেফারেন্সসহ উত্তর।
   - যেকোনো উত্তর কপি ও সরাসরি **ফাইলে** সংরক্ষণের সুবিধা।

2. **টুলস (মেডিসিন ও ডোজ ব্যবস্থাপনা + ক্লিনিক্যাল ক্যালকুলেটর)**
   - **ওষুধ ও ডোজ ট্র্যাকার:** ওষুধের নাম, ডোজ, রুট (`IV`, `IM`, `PO`, `SC`, `Neb`, `Topical`), ফ্রিকোয়েন্সি (`OD`, `BD`, `TDS`, `QID`, `STAT`, `PRN`), ওয়ার্ড/বেড নোট সংরক্ষণ এবং ডোজ দেওয়ার রেকর্ড লগ।
   - **ক্লিনিক্যাল ক্যালকুলেটর:** IV ফ্লুইড ড্রিপ রেট (`drops/min` ও `mL/hr`), ওজন অনুযায়ী ওষুধের ডোজ (`mg/kg`), এবং বার্ন ফ্লুইড রিসাসিটেশন (**Parkland Formula**)।

3. **ফাইল (সেভ করা উত্তর ও ব্যক্তিগত নোট)**
   - চ্যাট থেকে সংরক্ষিত AI উত্তর ও নিজের লেখা ব্যক্তিগত নোট সার্চ, ফিল্টার (`সব ফাইল`, `AI উত্তর`, `ব্যক্তিগত নোট`), পিন, এডিট ও ডিলিট করার সুবিধা।

4. **প্রোফাইল**
   - Google Sign-In (`CredentialManager`) ভিত্তিক অ্যাকাউন্ট।
   - নাম, পদবি/কোর্স, হাসপাতাল/নার্সিং কলেজ, ওয়ার্ড, BNMC/স্টুডেন্ট আইডি এবং বায়ো সংরক্ষণ ও লাইভ অ্যাক্টিভিটি পরিসংখ্যান।

## প্রযুক্তি (Tech Stack)

- **UI:** Kotlin, Jetpack Compose, Material Design 3 (`NursingAiTheme`)
- **AI:** Firebase AI Logic (`gemini-3.8-flash` streaming + App Check)
- **Auth & Database:** Firebase Authentication (Google Sign-In via Jetpack Credential Manager), Cloud Firestore (Named Database + Zero-Trust Security Rules)
- **Architecture:** MVVM with `StateFlow` & Coroutines

## প্রজেক্ট স্ট্রাকচার

- `app/src/main/java/com/example/` — অ্যাপ সোর্স কোড (`auth`, `data`, `firebase`, `ui`)
- `firestore.rules` & `firebase-blueprint.json` — Firestore সিকিউরিটি রুলস ও ডেটা স্কিমা
- `firestore.test.js` & `firebase.json` — লোকাল Firestore এমুলেটর রুলস ইউনিট টেস্ট
- `.github/workflows/android.yml` — GitHub Actions CI বিল্ড (JDK 21 + Gradle 9.3.1), ইউনিট টেস্ট ও Debug APK আর্টিফ্যাক্ট ওয়ার্কফ্লো

## বিল্ড ও টেস্ট

```bash
# অ্যান্ড্রয়েড ডিবাগ APK বিল্ড
gradle :app:assembleDebug

# Firestore সিকিউরিটি রুলস টেস্ট (লোকাল এমুলেটরসহ)
FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
```

> **নোট:** GitHub-এ কোড পুশ করার জন্য AI Studio-এর Settings মেনু থেকে **Push to GitHub** ব্যবহার করতে পারেন অথবা ZIP হিসেবে এক্সপোর্ট করে রিপোজিটরিতে আপলোড করতে পারেন।
