package com.smcall.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : Activity() {
    private var auth: FirebaseAuth? = null
    private var language = "bn"
    private var signupMode = false
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var statusText: TextView

    private data class Labels(
        val welcome: String, val intro: String, val name: String,
        val email: String, val password: String, val login: String,
        val signup: String, val create: String, val switchToSignup: String,
        val switchToLogin: String, val forgot: String, val firebaseMissing: String,
        val verifyTitle: String, val verifyBody: String, val resend: String,
        val reload: String, val logout: String, val home: String,
        val chats: String, val audio: String, val video: String,
        val groupAudio: String, val groupVideo: String, val notReady: String,
        val emailSent: String, val verified: String, val profileSaved: String
    )

    private fun labels(): Labels = when (language) {
        "en" -> Labels(
            "Welcome to SM CALL", "Sign in with your email and password.",
            "Your name", "Email address", "Password", "Sign in", "Create account",
            "Create account", "New here? Create an account", "Already registered? Sign in",
            "Forgot password?", "Firebase is not configured. Add android/app/google-services.json.",
            "Verify your email", "A verification link was sent to your email. Check Spam/Junk too. Verify before using SM CALL.",
            "Resend verification email", "I verified — Reload", "Sign out", "SM CALL Home",
            "Chats", "Audio call", "Video call", "Group audio (max 5)",
            "Group video (max 5)", "This feature is not implemented yet.",
            "Verification email sent.", "Email verified. Welcome!", "Profile saved."
        )
        "ar" -> Labels(
            "مرحباً بك في SM CALL", "سجّل الدخول بالبريد الإلكتروني وكلمة المرور.",
            "اسمك", "البريد الإلكتروني", "كلمة المرور", "تسجيل الدخول", "إنشاء حساب",
            "إنشاء حساب", "مستخدم جديد؟ أنشئ حسابًا", "لديك حساب؟ سجّل الدخول",
            "هل نسيت كلمة المرور؟", "Firebase غير مُعد. أضف ملف google-services.json إلى android/app.",
            "تحقق من بريدك الإلكتروني", "أرسلنا رابط التحقق إلى بريدك. تحقق من مجلد Spam/Junk أيضًا. يجب التحقق قبل استخدام التطبيق.",
            "إعادة إرسال رسالة التحقق", "لقد تحققت — إعادة التحميل", "تسجيل الخروج", "SM CALL الرئيسية",
            "المحادثات", "مكالمة صوتية", "مكالمة فيديو", "صوت جماعي (5 كحد أقصى)",
            "فيديو جماعي (5 كحد أقصى)", "هذه الميزة لم يتم تنفيذها بعد.",
            "تم إرسال رسالة التحقق.", "تم التحقق من البريد. أهلاً بك!", "تم حفظ الملف الشخصي."
        )
        "hi" -> Labels(
            "SM CALL में आपका स्वागत है", "ईमेल और पासवर्ड से लॉगिन करें।",
            "आपका नाम", "ईमेल पता", "पासवर्ड", "लॉगिन", "खाता बनाएँ",
            "खाता बनाएँ", "नए हैं? खाता बनाएँ", "पहले से खाता है? लॉगिन करें",
            "पासवर्ड भूल गए?", "Firebase सेट नहीं है। android/app में google-services.json जोड़ें।",
            "ईमेल सत्यापित करें", "सत्यापन लिंक आपके ईमेल पर भेजा गया है। Spam/Junk भी देखें। ऐप इस्तेमाल करने से पहले सत्यापन ज़रूरी है।",
            "सत्यापन ईमेल फिर भेजें", "मैंने सत्यापित किया — Reload", "लॉगआउट", "SM CALL होम",
            "चैट", "ऑडियो कॉल", "वीडियो कॉल", "ग्रुप ऑडियो (अधिकतम 5)",
            "ग्रुप वीडियो (अधिकतम 5)", "यह सुविधा अभी लागू नहीं है।",
            "सत्यापन ईमेल भेज दिया गया।", "ईमेल सत्यापित है। स्वागत है!", "प्रोफ़ाइल सेव हो गई।"
        )
        else -> Labels(
            "স্বাগতম SM CALL-এ", "ইমেইল ও পাসওয়ার্ড দিয়ে লগইন করুন।",
            "আপনার নাম", "ইমেইল ঠিকানা", "পাসওয়ার্ড", "লগইন", "অ্যাকাউন্ট তৈরি",
            "অ্যাকাউন্ট তৈরি", "নতুন? অ্যাকাউন্ট খুলুন", "আগে অ্যাকাউন্ট আছে? লগইন করুন",
            "পাসওয়ার্ড ভুলে গেছেন?", "Firebase সেটআপ হয়নি। android/app ফোল্ডারে google-services.json যোগ করুন।",
            "ইমেইল যাচাই করুন", "আপনার ইমেইলে যাচাই লিংক পাঠানো হয়েছে। Spam/Junk ফোল্ডারও দেখুন। যাচাই না করা পর্যন্ত অ্যাপ ব্যবহার করা যাবে না।",
            "আবার যাচাই ইমেইল পাঠান", "যাচাই করেছি — আবার দেখুন", "লগআউট", "SM CALL হোম",
            "চ্যাট", "অডিও কল", "ভিডিও কল", "গ্রুপ অডিও (সর্বোচ্চ ৫ জন)",
            "গ্রুপ ভিডিও (সর্বোচ্চ ৫ জন)", "এই ফিচারটি এখনো বাস্তবে তৈরি হয়নি।",
            "যাচাই ইমেইল পাঠানো হয়েছে।", "ইমেইল যাচাই হয়েছে। স্বাগতম!", "প্রোফাইল সংরক্ষণ হয়েছে।"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = savedInstanceState?.getString("language") ?: "bn"
        window.statusBarColor = Color.rgb(7, 27, 54)
        window.navigationBarColor = Color.rgb(7, 27, 54)
        auth = if (FirebaseApp.getApps(this).isNotEmpty()) FirebaseAuth.getInstance() else null
        val user = auth?.currentUser
        if (user == null) renderAuth() else refreshSignedInUser(user)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("language", language)
        super.onSaveInstanceState(outState)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun baseScreen(): LinearLayout {
        val scroll = ScrollView(this).apply { isFillViewport = true; isVerticalScrollBarEnabled = false }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(32))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(7, 27, 54), Color.rgb(18, 63, 112))
            )
            layoutDirection = if (language == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        }
        scroll.addView(page, ViewGroup.LayoutParams(-1, -2))
        setContentView(scroll)
        return page
    }

    private fun addBrand(page: LinearLayout) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brand = TextView(this).apply {
            text = "SM CALL"; textSize = 25f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD
        }
        row.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val languageButton = Button(this).apply {
            text = when (language) { "en" -> "English"; "ar" -> "العربية"; "hi" -> "हिन्दी"; else -> "বাংলা" }
            isAllCaps = false; setTextColor(Color.WHITE); background = rounded(Color.rgb(28, 78, 125))
            setOnClickListener { language = when (language) { "bn" -> "en"; "en" -> "ar"; "ar" -> "hi"; else -> "bn" }; renderCurrentScreen() }
        }
        row.addView(languageButton)
        page.addView(row)
        addSpace(page, 28)
    }

    private fun rounded(color: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(14).toFloat() }

    private fun addSpace(page: LinearLayout, height: Int) {
        page.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun addText(page: LinearLayout, value: String, size: Float, bold: Boolean = false): TextView {
        val view = TextView(this).apply {
            text = value; textSize = size; setTextColor(Color.WHITE)
            gravity = if (language == "ar") Gravity.RIGHT else Gravity.LEFT
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
        page.addView(view, LinearLayout.LayoutParams(-1, -2))
        return view
    }

    private fun addInput(page: LinearLayout, hint: String, type: Int): EditText {
        val field = EditText(this).apply {
            this.hint = hint; inputType = type; textSize = 16f; setSingleLine(true)
            setTextColor(Color.rgb(12, 35, 61)); setHintTextColor(Color.rgb(90, 110, 130))
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dp(14).toFloat() }
        }
        val params = LinearLayout.LayoutParams(-1, dp(54)); params.topMargin = dp(12)
        page.addView(field, params)
        return field
    }

    private fun addButton(page: LinearLayout, title: String, secondary: Boolean = false, action: () -> Unit): Button {
        val button = Button(this).apply {
            text = title; isAllCaps = false; textSize = 15f; setTextColor(Color.WHITE)
            background = rounded(if (secondary) Color.rgb(28, 78, 125) else Color.rgb(22, 199, 160))
            setOnClickListener { action() }
        }
        val params = LinearLayout.LayoutParams(-1, dp(54)); params.topMargin = dp(12)
        page.addView(button, params)
        return button
    }

    private fun renderCurrentScreen() {
        val user = auth?.currentUser
        when {
            user == null -> renderAuth()
            user.isEmailVerified -> renderHome(user)
            else -> renderVerification(user)
        }
    }

    private fun renderAuth() {
        val copy = labels()
        val page = baseScreen(); addBrand(page)
        addText(page, copy.welcome, 27f, true); addSpace(page, 8); addText(page, copy.intro, 16f)
        addSpace(page, 18)
        if (signupMode) nameInput = addInput(page, copy.name, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
        emailInput = addInput(page, copy.email, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        passwordInput = addInput(page, copy.password, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        statusText = addText(page, "", 14f); addSpace(page, 4)
        addButton(page, if (signupMode) copy.create else copy.login) {
            if (signupMode) createAccount() else login()
        }
        addButton(page, if (signupMode) copy.switchToLogin else copy.switchToSignup, true) {
            signupMode = !signupMode; renderAuth()
        }
        if (!signupMode) addButton(page, copy.forgot, true) { resetPassword() }
    }

    private fun createAccount() {
        val copy = labels()
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        if (name.isBlank() || email.isBlank() || password.isBlank()) { statusText.text = "সব তথ্য পূরণ করুন।"; return }
        if (password.length < 6) { statusText.text = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"; return }
        val currentAuth = auth
        if (currentAuth == null) { statusText.text = copy.firebaseMissing; return }
        statusText.text = "অ্যাকাউন্ট তৈরি হচ্ছে..."
        currentAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) { statusText.text = "অ্যাকাউন্ট তৈরি হয়নি।"; return@addOnSuccessListener }
                user.updateProfile(com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(name).build())
                    .addOnCompleteListener {
                        user.sendEmailVerification().addOnSuccessListener {
                            renderVerification(user)
                        }.addOnFailureListener { error -> renderVerification(user, "ইমেইল পাঠানো যায়নি: ${error.localizedMessage}") }
                    }
            }.addOnFailureListener { error -> statusText.text = authError(error) }
    }

    private fun login() {
        val copy = labels()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        if (email.isBlank() || password.isBlank()) { statusText.text = "ইমেইল ও পাসওয়ার্ড লিখুন।"; return }
        val currentAuth = auth
        if (currentAuth == null) { statusText.text = copy.firebaseMissing; return }
        statusText.text = "লগইন হচ্ছে..."
        currentAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result -> result.user?.let { refreshSignedInUser(it) } }
            .addOnFailureListener { error -> statusText.text = authError(error) }
    }

    private fun resetPassword() {
        val email = emailInput.text.toString().trim()
        if (email.isBlank()) { statusText.text = "আগে ইমেইল ঠিকানা লিখুন।"; return }
        auth?.sendPasswordResetEmail(email)
            ?.addOnSuccessListener { statusText.text = "পাসওয়ার্ড রিসেট লিংক পাঠানো হয়েছে।" }
            ?.addOnFailureListener { statusText.text = authError(it) }
    }

    private fun refreshSignedInUser(user: FirebaseUser) {
        user.reload().addOnSuccessListener {
            val fresh = auth?.currentUser
            if (fresh == null) renderAuth()
            else if (fresh.isEmailVerified) saveProfile(fresh)
            else renderVerification(fresh)
        }.addOnFailureListener { renderVerification(user, authError(it)) }
    }

    private fun renderVerification(user: FirebaseUser, message: String? = null) {
        val copy = labels()
        val page = baseScreen(); addBrand(page)
        addText(page, copy.verifyTitle, 26f, true); addSpace(page, 12)
        addText(page, user.email ?: "", 16f, true); addSpace(page, 12)
        addText(page, copy.verifyBody, 15f); addSpace(page, 10)
        statusText = addText(page, message ?: "", 14f)
        addButton(page, copy.resend) {
            user.sendEmailVerification().addOnSuccessListener {
                statusText.text = copy.emailSent
            }.addOnFailureListener { statusText.text = authError(it) }
        }
        addButton(page, copy.reload) { refreshSignedInUser(user) }
        addButton(page, copy.logout, true) { auth?.signOut(); signupMode = false; renderAuth() }
    }

    private fun saveProfile(user: FirebaseUser) {
        val name = user.displayName?.takeIf { it.isNotBlank() } ?: "SM CALL User"
        val profile = hashMapOf<String, Any>(
            "uid" to user.uid,
            "name" to name,
            "email" to (user.email ?: ""),
            "emailVerified" to true,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .set(profile)
            .addOnSuccessListener { renderHome(user); Toast.makeText(this, labels().profileSaved, Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { error ->
                renderHome(user)
                Toast.makeText(this, "লগইন হয়েছে, কিন্তু প্রোফাইল সংরক্ষণ হয়নি: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun renderHome(user: FirebaseUser) {
        val copy = labels()
        val page = baseScreen(); addBrand(page)
        addText(page, copy.home, 27f, true); addSpace(page, 8)
        addText(page, user.email ?: "", 15f); addSpace(page, 18)
        addButton(page, copy.chats) { showNotReady() }
        addButton(page, copy.audio, true) { showNotReady() }
        addButton(page, copy.video, true) { showNotReady() }
        addButton(page, copy.groupAudio, true) { showNotReady() }
        addButton(page, copy.groupVideo, true) { showNotReady() }
        addButton(page, copy.logout, true) { auth?.signOut(); signupMode = false; renderAuth() }
    }

    private fun showNotReady() {
        Toast.makeText(this, labels().notReady, Toast.LENGTH_LONG).show()
    }

    private fun authError(error: Throwable): String {
        val code = (error as? FirebaseAuthException)?.errorCode.orEmpty()
        return when (code) {
            "ERROR_INVALID_EMAIL" -> "ইমেইল ঠিকানা সঠিক নয়।"
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "ইমেইল বা পাসওয়ার্ড সঠিক নয়।"
            "ERROR_USER_NOT_FOUND" -> "এই ইমেইলে অ্যাকাউন্ট পাওয়া যায়নি।"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "এই ইমেইল দিয়ে আগে অ্যাকাউন্ট খোলা হয়েছে।"
            "ERROR_WEAK_PASSWORD" -> "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের দিন।"
            "ERROR_TOO_MANY_REQUESTS" -> "অনেকবার চেষ্টা হয়েছে। কিছুক্ষণ পরে আবার চেষ্টা করুন।"
            else -> error.localizedMessage ?: "কাজটি সম্পন্ন হয়নি।"
        }
    }
}
