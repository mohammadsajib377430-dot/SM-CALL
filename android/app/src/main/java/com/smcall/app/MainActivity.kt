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
import com.google.firebase.firestore.SetOptions

class MainActivity : Activity() {
    private var auth: FirebaseAuth? = null
    private var language = "bn"
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var statusText: TextView
    private lateinit var primaryButton: Button

    private val languages = listOf("bn", "en", "ar", "hi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = savedInstanceState?.getString("language") ?: "bn"
        window.statusBarColor = Color.rgb(7, 27, 54)
        window.navigationBarColor = Color.rgb(7, 27, 54)
        auth = if (FirebaseApp.getApps(this).isNotEmpty()) FirebaseAuth.getInstance() else null

        val user = auth?.currentUser
        if (user == null) renderLogin() else refreshAndRoute(user)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("language", language)
        super.onSaveInstanceState(outState)
    }

    private fun tr(bn: String, en: String, ar: String, hi: String): String =
        when (language) { "en" -> en; "ar" -> ar; "hi" -> hi; else -> bn }

    private fun baseScreen(): LinearLayout {
        val scroll = ScrollView(this).apply { isFillViewport = true; isVerticalScrollBarEnabled = false }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(32))
            minimumHeight = resources.displayMetrics.heightPixels
            layoutDirection = if (language == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(7, 27, 54), Color.rgb(18, 63, 112)))
        }
        scroll.addView(page, ViewGroup.LayoutParams(-1, -2))
        setContentView(scroll)
        return page
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun addBrand(page: LinearLayout) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brand = TextView(this).apply {
            text = "SM CALL"; textSize = 25f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD
        }
        row.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val languageButton = Button(this).apply {
            text = tr("বাংলা", "English", "العربية", "हिन्दी"); isAllCaps = false
            setTextColor(Color.WHITE); background = rounded(Color.rgb(28, 78, 125), 14)
            setOnClickListener { changeLanguage() }
        }
        row.addView(languageButton)
        page.addView(row)
        addSpace(page, 28)
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }

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
            background = GradientDrawable().apply {
                setColor(Color.WHITE); cornerRadius = dp(14).toFloat()
                setStroke(dp(1), Color.rgb(190, 208, 225))
            }
        }
        val params = LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(12) }
        page.addView(field, params)
        return field
    }

    private fun addButton(page: LinearLayout, label: String, secondary: Boolean = false, action: () -> Unit): Button {
        val button = Button(this).apply {
            text = label; isAllCaps = false; textSize = 15f; setTextColor(Color.WHITE)
            background = rounded(if (secondary) Color.rgb(28, 78, 125) else Color.rgb(22, 150, 130), 14)
            setOnClickListener { action() }
        }
        page.addView(button, LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(12) })
        return button
    }

    private fun addStatus(page: LinearLayout) {
        statusText = addText(page, "", 14f)
        addSpace(page, 6)
    }

    private fun firebaseReady(): FirebaseAuth? {
        val current = auth
        if (current == null) {
            statusText.text = tr(
                "Firebase সংযুক্ত হয়নি। আগে Firebase Android সেটআপ করতে হবে।",
                "Firebase is not configured. Complete Firebase Android setup first.",
                "لم يتم إعداد Firebase بعد.",
                "Firebase अभी सेट नहीं है।"
            )
        }
        return current
    }

    private fun renderLogin() {
        val page = baseScreen(); addBrand(page)
        addText(page, tr("স্বাগতম SM CALL-এ", "Welcome to SM CALL", "مرحباً بك في SM CALL", "SM CALL में आपका स्वागत है"), 27f, true)
        addText(page, tr("ইমেইল ও পাসওয়ার্ড দিয়ে লগইন করুন।", "Sign in with your email and password.", "سجّل الدخول بالبريد الإلكتروني وكلمة المرور.", "ईमेल और पासवर्ड से लॉगिन करें।"), 16f)
        addSpace(page, 18)
        emailInput = addInput(page, tr("ইমেইল ঠিকানা", "Email address", "البريد الإلكتروني", "ईमेल पता"), InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_CLASS_TEXT)
        passwordInput = addInput(page, tr("পাসওয়ার্ড", "Password", "كلمة المرور", "पासवर्ड"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        addStatus(page)
        primaryButton = addButton(page, tr("লগইন", "Sign in", "تسجيل الدخول", "लॉगिन")) { login() }
        addButton(page, tr("নতুন অ্যাকাউন্ট তৈরি করুন", "Create account", "إنشاء حساب جديد", "नया खाता बनाएँ"), true) { renderSignup(emailInput.text.toString()) }
        addButton(page, tr("পাসওয়ার্ড ভুলে গেছেন?", "Forgot password?", "هل نسيت كلمة المرور؟", "पासवर्ड भूल गए?"), true) { resetPassword() }
        addSpace(page, 18)
        addText(page, tr("শুধু ইমেইল লগইন। ফোন OTP ব্যবহার করা হয় না।", "Email and password only. No phone OTP.", "البريد الإلكتروني فقط، دون رمز هاتف.", "केवल ईमेल लॉगिन। फोन OTP नहीं।"), 12f)
    }

    private fun renderSignup(prefill: String = "") {
        val page = baseScreen(); addBrand(page)
        addText(page, tr("নতুন অ্যাকাউন্ট", "Create account", "إنشاء حساب", "नया खाता"), 27f, true)
        nameInput = addInput(page, tr("আপনার নাম", "Your name", "اسمك", "आपका नाम"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS)
        emailInput = addInput(page, tr("ইমেইল ঠিকানা", "Email address", "البريد الإلكتروني", "ईमेल पता"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        emailInput.setText(prefill)
        passwordInput = addInput(page, tr("পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)", "Password (at least 6 characters)", "كلمة المرور (6 أحرف على الأقل)", "पासवर्ड (कम से कम 6 अक्षर)"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        confirmPasswordInput = addInput(page, tr("পাসওয়ার্ড আবার লিখুন", "Confirm password", "تأكيد كلمة المرور", "पासवर्ड दोबारा लिखें"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        addStatus(page)
        primaryButton = addButton(page, tr("অ্যাকাউন্ট তৈরি করুন", "Create account", "إنشاء حساب", "खाता बनाएँ")) { createAccount() }
        addButton(page, tr("লগইন পাতায় ফিরে যান", "Back to sign in", "العودة لتسجيل الدخول", "लॉगिन पर लौटें"), true) { renderLogin() }
    }

    private fun renderVerification(user: FirebaseUser) {
        val page = baseScreen(); addBrand(page)
        addText(page, tr("ইমেইল যাচাই করুন", "Verify your email", "تحقق من بريدك الإلكتروني", "ईमेल सत्यापित करें"), 27f, true)
        addText(page, tr("আমরা এই ঠিকানায় যাচাই লিংক পাঠিয়েছি:", "We sent a verification link to:", "أرسلنا رابط التحقق إلى:", "हमने इस पते पर सत्यापन लिंक भेजा है:"), 16f)
        addText(page, user.email.orEmpty(), 16f, true)
        addSpace(page, 12)
        addText(page, tr("Inbox না পেলে Spam/Junk ফোল্ডার দেখুন। লিংকে চাপার পর নিচের Reload চাপুন।", "Check Spam/Junk if it is not in your inbox. After clicking the link, tap Reload.", "تحقق من مجلد Spam/Junk ثم اضغط إعادة التحميل بعد فتح الرابط.", "Inbox में न मिले तो Spam/Junk देखें। लिंक खोलने के बाद Reload दबाएँ।"), 14f)
        addStatus(page)
        addButton(page, tr("আবার যাচাই ইমেইল পাঠান", "Resend verification email", "إعادة إرسال رسالة التحقق", "सत्यापन ईमेल फिर भेजें")) { resendVerification() }
        addButton(page, tr("আমি যাচাই করেছি / Reload", "I verified / Reload", "لقد تحققت / إعادة تحميل", "मैंने सत्यापित किया / Reload"), true) { refreshVerification() }
        addButton(page, tr("লগআউট", "Sign out", "تسجيل الخروج", "लॉगआउट"), true) { auth?.signOut(); renderLogin() }
    }

    private fun renderHome(user: FirebaseUser) {
        val page = baseScreen(); addBrand(page)
        addText(page, tr("SM CALL হোম", "SM CALL Home", "الصفحة الرئيسية", "SM CALL होम"), 27f, true)
        addText(page, user.email.orEmpty(), 15f)
        addSpace(page, 18)
        addText(page, tr("ইমেইল যাচাই সম্পন্ন।", "Email verified.", "تم التحقق من البريد.", "ईमेल सत्यापित है।"), 15f)
        addButton(page, tr("চ্যাট", "Chats", "المحادثات", "चैट")) { showNotReady() }
        addButton(page, tr("অডিও কল", "Audio call", "مكالمة صوتية", "ऑडियो कॉल"), true) { showNotReady() }
        addButton(page, tr("ভিডিও কল", "Video call", "مكالمة فيديو", "वीडियो कॉल"), true) { showNotReady() }
        addButton(page, tr("গ্রুপ অডিও / ভিডিও কল (সর্বোচ্চ ৫ জন)", "Group audio / video (max 5)", "مكالمة جماعية (5 كحد أقصى)", "ग्रुप कॉल (अधिकतम 5)"), true) { showNotReady() }
        addButton(page, tr("লগআউট", "Sign out", "تسجيل الخروج", "लॉगआउट"), true) { auth?.signOut()?.addOnCompleteListener { renderLogin() } }
    }

    private fun showNotReady() {
        Toast.makeText(this, tr("এই ফিচার এখনো তৈরি হয়নি।", "This feature is not implemented yet.", "هذه الميزة لم تُنفذ بعد.", "यह सुविधा अभी लागू नहीं है।"), Toast.LENGTH_LONG).show()
    }

    private fun login() {
        val current = firebaseReady() ?: return
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        if (email.isBlank() || password.isBlank()) { statusText.text = tr("ইমেইল ও পাসওয়ার্ড লিখুন।", "Enter email and password.", "أدخل البريد وكلمة المرور.", "ईमेल और पासवर्ड लिखें।"); return }
        primaryButton.isEnabled = false
        statusText.text = tr("লগইন হচ্ছে...", "Signing in...", "جارٍ تسجيل الدخول...", "लॉगिन हो रहा है...")
        current.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result -> primaryButton.isEnabled = true; result.user?.let { refreshAndRoute(it) } }
            .addOnFailureListener { error -> primaryButton.isEnabled = true; statusText.text = authError(error) }
    }

    private fun createAccount() {
        val current = firebaseReady() ?: return
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirm = confirmPasswordInput.text.toString()
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) { statusText.text = tr("সব তথ্য পূরণ করুন।", "Complete all fields.", "أكمل جميع الحقول.", "सभी जानकारी भरें।"); return }
        if (password.length < 6) { statusText.text = tr("পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।", "Password must be at least 6 characters.", "يجب أن تتكون كلمة المرور من 6 أحرف على الأقل.", "पासवर्ड कम से कम 6 अक्षर का होना चाहिए।"); return }
        if (password != confirm) { statusText.text = tr("দুইটি পাসওয়ার্ড মিলছে না।", "Passwords do not match.", "كلمتا المرور غير متطابقتين.", "दोनों पासवर्ड मेल नहीं खाते।"); return }
        primaryButton.isEnabled = false
        current.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) { primaryButton.isEnabled = true; statusText.text = "Account creation failed."; return@addOnSuccessListener }
                user.updateProfile(com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(name).build())
                    .addOnCompleteListener {
                        user.sendEmailVerification()
                            .addOnSuccessListener { primaryButton.isEnabled = true; renderVerification(user) }
                            .addOnFailureListener { error -> primaryButton.isEnabled = true; statusText.text = tr("অ্যাকাউন্ট হয়েছে, কিন্তু যাচাই ইমেইল পাঠানো যায়নি: ", "Account created, but verification email failed: ", "تم إنشاء الحساب لكن فشل إرسال رسالة التحقق: ", "खाता बन गया, लेकिन सत्यापन ईमेल नहीं भेजा गया: ") + (error.localizedMessage ?: "") }
                    }
            }
            .addOnFailureListener { error -> primaryButton.isEnabled = true; statusText.text = authError(error) }
    }

    private fun resendVerification() {
        val user = auth?.currentUser ?: return
        statusText.text = tr("ইমেইল পাঠানো হচ্ছে...", "Sending email...", "جارٍ الإرسال...", "ईमेल भेजा जा रहा है...")
        user.sendEmailVerification()
            .addOnSuccessListener { statusText.text = tr("যাচাই ইমেইল পাঠানো হয়েছে।", "Verification email sent.", "تم إرسال رسالة التحقق.", "सत्यापन ईमेल भेजा गया।") }
            .addOnFailureListener { statusText.text = authError(it) }
    }

    private fun refreshVerification() {
        val user = auth?.currentUser ?: return
        statusText.text = tr("যাচাই অবস্থা দেখা হচ্ছে...", "Checking verification...", "جارٍ التحقق...", "सत्यापन जाँचा जा रहा है...")
        user.reload().addOnSuccessListener {
            val refreshed = auth?.currentUser
            if (refreshed?.isEmailVerified == true) saveProfileAndOpen(refreshed)
            else { statusText.text = tr("এখনো যাচাই হয়নি। ইমেইলের লিংক খুলে আবার Reload চাপুন।", "Not verified yet. Open the email link and reload again.", "لم يتم التحقق بعد. افتح الرابط ثم أعد التحميل.", "अभी सत्यापित नहीं हुआ। ईमेल लिंक खोलकर फिर Reload करें।"); refreshed?.let { renderVerification(it) } }
        }.addOnFailureListener { statusText.text = authError(it) }
    }

    private fun refreshAndRoute(user: FirebaseUser) {
        user.reload().addOnSuccessListener {
            val refreshed = auth?.currentUser ?: user
            if (refreshed.isEmailVerified) saveProfileAndOpen(refreshed) else renderVerification(refreshed)
        }.addOnFailureListener { renderLogin(); Toast.makeText(this, authError(it), Toast.LENGTH_LONG).show() }
    }

    private fun resetPassword() {
        val current = firebaseReady() ?: return
        val email = emailInput.text.toString().trim()
        if (email.isBlank()) { statusText.text = tr("আগে ইমেইল লিখুন।", "Enter your email first.", "أدخل بريدك أولاً.", "पहले ईमेल लिखें।"); return }
        current.sendPasswordResetEmail(email)
            .addOnSuccessListener { statusText.text = tr("পাসওয়ার্ড রিসেট লিংক পাঠানো হয়েছে।", "Password reset link sent.", "تم إرسال رابط إعادة تعيين كلمة المرور.", "पासवर्ड रीसेट लिंक भेजा गया।") }
            .addOnFailureListener { statusText.text = authError(it) }
    }

    private fun saveProfileAndOpen(user: FirebaseUser) {
        val profile = hashMapOf<String, Any>(
            "uid" to user.uid,
            "email" to (user.email ?: ""),
            "emailVerified" to true,
            "name" to (user.displayName ?: ""),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .set(profile, SetOptions.merge())
            .addOnSuccessListener { renderHome(user) }
            .addOnFailureListener {
                renderHome(user)
                Toast.makeText(this, tr("লগইন হয়েছে, কিন্তু প্রোফাইল সেভ হয়নি: ", "Signed in, but profile save failed: ", "تم تسجيل الدخول لكن فشل حفظ الملف: ", "लॉगिन हुआ, लेकिन प्रोफ़ाइल सेव नहीं हुई: ") + (it.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
            }
    }

    private fun authError(error: Throwable): String {
        val code = (error as? FirebaseAuthException)?.errorCode.orEmpty()
        return when (code) {
            "ERROR_INVALID_EMAIL" -> tr("ইমেইল ঠিকানা সঠিক নয়।", "Invalid email address.", "البريد الإلكتروني غير صحيح.", "ईमेल पता अमान्य है।")
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> tr("ইমেইল বা পাসওয়ার্ড ভুল।", "Email or password is incorrect.", "البريد أو كلمة المرور غير صحيحة.", "ईमेल या पासवर्ड गलत है।")
            "ERROR_USER_NOT_FOUND" -> tr("এই ইমেইলে অ্যাকাউন্ট নেই।", "No account found for this email.", "لا يوجد حساب لهذا البريد.", "इस ईमेल पर खाता नहीं मिला।")
            "ERROR_EMAIL_ALREADY_IN_USE" -> tr("এই ইমেইলে আগে অ্যাকাউন্ট আছে।", "An account already uses this email.", "هذا البريد مستخدم بالفعل.", "इस ईमेल से पहले से खाता है।")
            "ERROR_WEAK_PASSWORD" -> tr("আরও শক্ত পাসওয়ার্ড দিন।", "Choose a stronger password.", "اختر كلمة مرور أقوى.", "मज़बूत पासवर्ड चुनें।")
            else -> error.localizedMessage ?: tr("কাজটি সম্পন্ন হয়নি।", "The operation failed.", "فشلت العملية.", "काम पूरा नहीं हुआ।")
        }
    }

    private fun changeLanguage() {
        val index = languages.indexOf(language).coerceAtLeast(0)
        language = languages[(index + 1) % languages.size]
        val user = auth?.currentUser
        when {
            user != null && user.isEmailVerified -> renderHome(user)
            user != null -> renderVerification(user)
            else -> renderLogin()
        }
    }
}
