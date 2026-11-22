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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : Activity() {
    private var auth: FirebaseAuth? = null
    private var language = "bn"
    private var currentScreen = "login"
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var statusText: TextView

    private val languages = listOf("bn", "en", "ar", "hi")

    private data class Copy(
        val welcome: String,
        val intro: String,
        val name: String,
        val email: String,
        val password: String,
        val confirmPassword: String,
        val login: String,
        val createAccount: String,
        val newAccount: String,
        val forgotPassword: String,
        val verifyTitle: String,
        val verifyBody: String,
        val resend: String,
        val refresh: String,
        val logout: String,
        val home: String,
        val chats: String,
        val audio: String,
        val video: String,
        val groupAudio: String,
        val groupVideo: String,
        val firebaseMissing: String,
        val invalidFields: String,
        val passwordShort: String,
        val passwordMismatch: String,
        val signupSent: String,
        val verificationSent: String,
        val notVerified: String,
        val verified: String,
        val resetSent: String,
        val featureUnavailable: String,
        val profileError: String
    )

    private fun copy(): Copy = when (language) {
        "en" -> Copy(
            "Welcome to SM CALL", "Sign in with email and password",
            "Your name", "Email address", "Password", "Confirm password",
            "Sign in", "Create account", "Create a new account",
            "Forgot password?", "Verify your email",
            "Open the verification link sent to:", "Resend verification email",
            "I verified — check again", "Sign out", "SM CALL Home",
            "Chats", "Audio call", "Video call", "Group audio (max 5)",
            "Group video (max 5)", "Firebase is not configured. Add android/app/google-services.json.",
            "Fill in all required fields.", "Password must be at least 6 characters.",
            "Passwords do not match.", "Account created. Check your email.",
            "Verification email sent.", "Email is not verified yet. Check Inbox or Spam.",
            "Email verified.", "Password reset email sent.",
            "This feature is not implemented yet.", "Profile could not be saved: "
        )
        "ar" -> Copy(
            "مرحباً بك في SM CALL", "سجّل الدخول بالبريد الإلكتروني وكلمة المرور",
            "اسمك", "البريد الإلكتروني", "كلمة المرور", "تأكيد كلمة المرور",
            "تسجيل الدخول", "إنشاء حساب", "إنشاء حساب جديد",
            "هل نسيت كلمة المرور؟", "تحقق من بريدك الإلكتروني",
            "افتح رابط التحقق المرسل إلى:", "إعادة إرسال رسالة التحقق",
            "لقد تحققت — تحقق مرة أخرى", "تسجيل الخروج", "الصفحة الرئيسية",
            "المحادثات", "مكالمة صوتية", "مكالمة فيديو", "صوت جماعي (5 كحد أقصى)",
            "فيديو جماعي (5 كحد أقصى)", "Firebase غير مُعد. أضف google-services.json إلى android/app.",
            "أكمل جميع الحقول المطلوبة.", "يجب أن تتكون كلمة المرور من 6 أحرف على الأقل.",
            "كلمتا المرور غير متطابقتين.", "تم إنشاء الحساب. تحقق من بريدك.",
            "تم إرسال رسالة التحقق.", "لم يتم التحقق من البريد بعد. افحص الوارد أو الرسائل غير المرغوب فيها.",
            "تم التحقق من البريد.", "تم إرسال رابط إعادة تعيين كلمة المرور.",
            "هذه الميزة لم تُنفذ بعد.", "تعذر حفظ الملف الشخصي: "
        )
        "hi" -> Copy(
            "SM CALL में आपका स्वागत है", "ईमेल और पासवर्ड से लॉगिन करें",
            "आपका नाम", "ईमेल पता", "पासवर्ड", "पासवर्ड की पुष्टि करें",
            "लॉगिन", "खाता बनाएँ", "नया खाता बनाएँ",
            "पासवर्ड भूल गए?", "ईमेल सत्यापित करें",
            "इस पते पर भेजे गए सत्यापन लिंक को खोलें:", "सत्यापन ईमेल फिर भेजें",
            "मैंने सत्यापित किया — फिर जाँचें", "लॉगआउट", "SM CALL होम",
            "चैट", "ऑडियो कॉल", "वीडियो कॉल", "ग्रुप ऑडियो (अधिकतम 5)",
            "ग्रुप वीडियो (अधिकतम 5)", "Firebase कॉन्फ़िगर नहीं है। android/app में google-services.json जोड़ें।",
            "सभी ज़रूरी जानकारी भरें।", "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए।",
            "दोनों पासवर्ड मेल नहीं खाते।", "खाता बन गया। ईमेल देखें।",
            "सत्यापन ईमेल भेज दिया गया।", "ईमेल अभी सत्यापित नहीं है। Inbox या Spam देखें।",
            "ईमेल सत्यापित है।", "पासवर्ड रीसेट ईमेल भेज दिया गया।",
            "यह सुविधा अभी लागू नहीं है।", "प्रोफ़ाइल सेव नहीं हुई: "
        )
        else -> Copy(
            "স্বাগতম SM CALL-এ", "ইমেইল ও পাসওয়ার্ড দিয়ে লগইন করুন",
            "আপনার নাম", "ইমেইল ঠিকানা", "পাসওয়ার্ড", "পাসওয়ার্ড নিশ্চিত করুন",
            "লগইন", "অ্যাকাউন্ট তৈরি", "নতুন অ্যাকাউন্ট তৈরি করুন",
            "পাসওয়ার্ড ভুলে গেছেন?", "ইমেইল যাচাই করুন",
            "এই ঠিকানায় পাঠানো ভেরিফিকেশন লিংক খুলুন:", "ভেরিফিকেশন ইমেইল আবার পাঠান",
            "আমি যাচাই করেছি — আবার পরীক্ষা করুন", "লগআউট", "SM CALL হোম",
            "চ্যাট", "অডিও কল", "ভিডিও কল", "গ্রুপ অডিও (সর্বোচ্চ ৫ জন)",
            "গ্রুপ ভিডিও (সর্বোচ্চ ৫ জন)", "Firebase সেটআপ করা নেই। android/app ফোল্ডারে google-services.json যোগ করুন।",
            "সব প্রয়োজনীয় তথ্য পূরণ করুন।", "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।",
            "দুইটি পাসওয়ার্ড মিলছে না।", "অ্যাকাউন্ট তৈরি হয়েছে। ইমেইল দেখুন।",
            "ভেরিফিকেশন ইমেইল পাঠানো হয়েছে।", "ইমেইল এখনো যাচাই হয়নি। Inbox বা Spam ফোল্ডার দেখুন।",
            "ইমেইল যাচাই হয়েছে।", "পাসওয়ার্ড রিসেট ইমেইল পাঠানো হয়েছে।",
            "এই ফিচারটি এখনো তৈরি হয়নি।", "প্রোফাইল সংরক্ষণ হয়নি: "
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = savedInstanceState?.getString("language") ?: "bn"
        window.statusBarColor = Color.rgb(7, 27, 54)
        window.navigationBarColor = Color.rgb(7, 27, 54)
        auth = if (FirebaseApp.getApps(this).isNotEmpty()) FirebaseAuth.getInstance() else null

        val user = auth?.currentUser
        if (user == null) {
            renderLogin()
        } else {
            verifySignedInUser(user)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("language", language)
        super.onSaveInstanceState(outState)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun page(): LinearLayout {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = false
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(32))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(7, 27, 54), Color.rgb(18, 63, 112))
            )
            layoutDirection = if (language == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        }
        scroll.addView(content, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        setContentView(scroll)
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "SM CALL"
            textSize = 25f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        header.addView(brand, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val langButton = Button(this).apply {
            text = when (language) { "en" -> "English"; "ar" -> "العربية"; "hi" -> "हिन्दी"; else -> "বাংলা" }
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(Color.rgb(28, 78, 125))
            setOnClickListener { changeLanguage() }
        }
        header.addView(langButton)
        content.addView(header)
        space(content, 28)
        return content
    }

    private fun rounded(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(14).toFloat()
    }

    private fun space(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun text(parent: LinearLayout, value: String, size: Float, bold: Boolean = false): TextView {
        val view = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.WHITE)
            gravity = if (language == "ar") Gravity.RIGHT else Gravity.LEFT
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
        parent.addView(view, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        return view
    }

    private fun field(parent: LinearLayout, hint: String, type: Int): EditText {
        val input = EditText(this).apply {
            this.hint = hint
            inputType = type
            textSize = 16f
            setSingleLine(true)
            setTextColor(Color.rgb(12, 35, 61))
            setHintTextColor(Color.rgb(90, 110, 130))
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), Color.rgb(190, 208, 225))
            }
        }
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54))
        params.topMargin = dp(12)
        parent.addView(input, params)
        return input
    }

    private fun button(parent: LinearLayout, title: String, secondary: Boolean = false, action: () -> Unit) {
        val control = Button(this).apply {
            text = title
            isAllCaps = false
            textSize = 15f
            setTextColor(Color.WHITE)
            background = rounded(if (secondary) Color.rgb(28, 78, 125) else Color.rgb(22, 150, 130))
            setOnClickListener { action() }
        }
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54))
        params.topMargin = dp(12)
        parent.addView(control, params)
    }

    private fun status(parent: LinearLayout): TextView {
        space(parent, 12)
        return text(parent, "", 14f)
    }

    private fun firebaseReady(): Boolean {
        if (auth != null) return true
        statusText.text = copy().firebaseMissing
        return false
    }

    private fun renderLogin() {
        currentScreen = "login"
        val c = copy()
        val root = page()
        text(root, c.welcome, 27f, true)
        space(root, 8)
        text(root, c.intro, 16f)
        emailInput = field(root, c.email, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        passwordInput = field(root, c.password, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        statusText = status(root)
        button(root, c.login) { login() }
        button(root, c.forgotPassword, true) { resetPassword() }
        button(root, c.newAccount, true) { renderSignup() }
    }

    private fun renderSignup() {
        currentScreen = "signup"
        val c = copy()
        val root = page()
        text(root, c.createAccount, 27f, true)
        nameInput = field(root, c.name, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
        emailInput = field(root, c.email, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        passwordInput = field(root, c.password, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        confirmPasswordInput = field(root, c.confirmPassword, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        statusText = status(root)
        button(root, c.createAccount) { createAccount() }
        button(root, c.login, true) { renderLogin() }
    }

    private fun renderVerification(user: FirebaseUser) {
        currentScreen = "verification"
        val c = copy()
        val root = page()
        text(root, c.verifyTitle, 27f, true)
        space(root, 12)
        text(root, c.verifyBody, 16f)
        text(root, user.email ?: "", 16f, true)
        space(root, 12)
        text(root, "Spam / Junk ফোল্ডারও পরীক্ষা করুন।", 14f)
        statusText = status(root)
        button(root, c.resend) { resendVerification() }
        button(root, c.refresh) { refreshVerification() }
        button(root, c.logout, true) { signOut() }
    }

    private fun renderHome(user: FirebaseUser) {
        currentScreen = "home"
        val c = copy()
        val root = page()
        text(root, c.home, 27f, true)
        space(root, 8)
        text(root, user.email ?: "", 15f)
        button(root, c.chats) { showFeatureUnavailable() }
        button(root, c.audio, true) { showFeatureUnavailable() }
        button(root, c.video, true) { showFeatureUnavailable() }
        button(root, c.groupAudio, true) { showFeatureUnavailable() }
        button(root, c.groupVideo, true) { showFeatureUnavailable() }
        button(root, c.logout, true) { signOut() }
    }

    private fun changeLanguage() {
        val i = languages.indexOf(language).coerceAtLeast(0)
        language = languages[(i + 1) % languages.size]
        when (currentScreen) {
            "signup" -> renderSignup()
            "verification" -> auth?.currentUser?.let { renderVerification(it) } ?: renderLogin()
            "home" -> auth?.currentUser?.let { renderHome(it) } ?: renderLogin()
            else -> renderLogin()
        }
    }

    private fun createAccount() {
        val c = copy()
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmation = confirmPasswordInput.text.toString()
        if (!firebaseReady()) return
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmation.isBlank()) {
            statusText.text = c.invalidFields
            return
        }
        if (password.length < 6) {
            statusText.text = c.passwordShort
            return
        }
        if (password != confirmation) {
            statusText.text = c.passwordMismatch
            return
        }
        statusText.text = "..."
        auth!!.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    statusText.text = c.invalidFields
                    return@addOnSuccessListener
                }
                val profile = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                user.updateProfile(profile).addOnCompleteListener {
                    user.sendEmailVerification()
                        .addOnSuccessListener {
                            statusText.text = c.signupSent
                            renderVerification(user)
                        }
                        .addOnFailureListener { error -> statusText.text = error.localizedMessage ?: c.invalidFields }
                }
            }
            .addOnFailureListener { error -> statusText.text = error.localizedMessage ?: c.invalidFields }
    }

    private fun login() {
        val c = copy()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        if (!firebaseReady()) return
        if (email.isBlank() || password.isBlank()) {
            statusText.text = c.invalidFields
            return
        }
        statusText.text = "..."
        auth!!.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    statusText.text = c.invalidFields
                } else {
                    verifySignedInUser(user)
                }
            }
            .addOnFailureListener { error -> statusText.text = error.localizedMessage ?: c.invalidFields }
    }

    private fun verifySignedInUser(user: FirebaseUser) {
        user.reload().addOnCompleteListener {
            val refreshed = auth?.currentUser
            if (it.isSuccessful && refreshed != null && refreshed.isEmailVerified) {
                saveProfile(refreshed)
            } else if (refreshed != null) {
                renderVerification(refreshed)
                statusText.text = copy().notVerified
            } else {
                renderLogin()
            }
        }
    }

    private fun resendVerification() {
        val user = auth?.currentUser ?: return
        user.sendEmailVerification()
            .addOnSuccessListener { statusText.text = copy().verificationSent }
            .addOnFailureListener { error -> statusText.text = error.localizedMessage ?: copy().notVerified }
    }

    private fun refreshVerification() {
        val user = auth?.currentUser ?: return
        user.reload().addOnCompleteListener { task ->
            val refreshed = auth?.currentUser
            if (task.isSuccessful && refreshed != null && refreshed.isEmailVerified) {
                statusText.text = copy().verified
                saveProfile(refreshed)
            } else {
                statusText.text = copy().notVerified
            }
        }
    }

    private fun resetPassword() {
        val email = emailInput.text.toString().trim()
        if (!firebaseReady()) return
        if (email.isBlank()) {
            statusText.text = copy().invalidFields
            return
        }
        auth!!.sendPasswordResetEmail(email)
            .addOnSuccessListener { statusText.text = copy().resetSent }
            .addOnFailureListener { error -> statusText.text = error.localizedMessage ?: copy().invalidFields }
    }

    private fun saveProfile(user: FirebaseUser) {
        val email = user.email ?: run {
            renderVerification(user)
            return
        }
        val data = hashMapOf<String, Any>(
            "uid" to user.uid,
            "name" to (user.displayName ?: "SM CALL User"),
            "email" to email,
            "emailVerified" to true,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .set(data)
            .addOnSuccessListener { renderHome(user) }
            .addOnFailureListener { error ->
                renderHome(user)
                Toast.makeText(this, copy().profileError + (error.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
            }
    }

    private fun signOut() {
        auth?.signOut()
        renderLogin()
    }

    private fun showFeatureUnavailable() {
        Toast.makeText(this, copy().featureUnavailable, Toast.LENGTH_LONG).show()
    }
}
