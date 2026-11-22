package com.smcall.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
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
    private lateinit var statusText: TextView
    private lateinit var loginButton: Button
    private lateinit var createAccountButton: Button
    private lateinit var resetPasswordButton: Button

    private val languages = listOf("bn", "en", "ar", "hi")

    private data class Labels(
        val welcome: String,
        val intro: String,
        val emailHint: String,
        val passwordHint: String,
        val login: String,
        val createAccount: String,
        val resetPassword: String,
        val enterEmail: String,
        val invalidEmail: String,
        val enterPassword: String,
        val passwordTooShort: String,
        val firebaseMissing: String,
        val working: String,
        val loginFailed: String,
        val accountCreated: String,
        val resetSent: String,
        val homeTitle: String,
        val signedInAs: String,
        val chats: String,
        val audio: String,
        val video: String,
        val groupVideo: String,
        val logout: String,
        val notReady: String,
        val profileSaved: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = savedInstanceState?.getString(STATE_LANGUAGE) ?: language

        window.statusBarColor = Color.rgb(7, 27, 54)
        window.navigationBarColor = Color.rgb(7, 27, 54)

        auth = if (FirebaseApp.getApps(this).isNotEmpty()) {
            FirebaseAuth.getInstance()
        } else {
            null
        }

        val signedInUser = auth?.currentUser
        if (signedInUser != null) {
            renderHome(signedInUser)
        } else {
            renderLogin()
            emailInput.setText(savedInstanceState?.getString(STATE_EMAIL).orEmpty())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_LANGUAGE, language)
        if (::emailInput.isInitialized) {
            outState.putString(STATE_EMAIL, emailInput.text.toString())
        }
    }

    private fun labels(): Labels = when (language) {
        "en" -> Labels(
            "Welcome to SM CALL", "Sign in or create an account with email",
            "Email address", "Password (at least 6 characters)",
            "Log in", "Create account", "Forgot password?",
            "Enter your email address.", "Enter a valid email address.",
            "Enter your password.", "Password must be at least 6 characters.",
            "Firebase is not connected. Check android/app/google-services.json.",
            "Please wait...", "Authentication failed: ", "Account created successfully.",
            "Password reset email sent. Check your inbox.",
            "SM CALL Home", "Signed in as", "Chats", "Audio call", "Video call",
            "Group video (up to 20)", "Sign out",
            "This feature is not active yet. It will be added and tested later.",
            "Profile saved."
        )
        "ar" -> Labels(
            "مرحباً بك في SM CALL", "سجّل الدخول أو أنشئ حساباً بالبريد الإلكتروني",
            "البريد الإلكتروني", "كلمة المرور (6 أحرف على الأقل)",
            "تسجيل الدخول", "إنشاء حساب", "هل نسيت كلمة المرور؟",
            "أدخل بريدك الإلكتروني.", "أدخل عنوان بريد إلكتروني صحيحاً.",
            "أدخل كلمة المرور.", "يجب أن تتكون كلمة المرور من 6 أحرف على الأقل.",
            "Firebase غير متصل. تحقق من ملف google-services.json.",
            "يرجى الانتظار...", "فشلت المصادقة: ", "تم إنشاء الحساب بنجاح.",
            "تم إرسال رسالة إعادة تعيين كلمة المرور. تحقق من بريدك.",
            "الصفحة الرئيسية لـ SM CALL", "تم تسجيل الدخول باسم", "المحادثات",
            "مكالمة صوتية", "مكالمة فيديو", "فيديو جماعي (حتى 20)", "تسجيل الخروج",
            "هذه الميزة غير مفعّلة بعد. ستتم إضافتها واختبارها لاحقاً.",
            "تم حفظ الملف الشخصي."
        )
        "hi" -> Labels(
            "SM CALL में आपका स्वागत है", "ईमेल से लॉगिन करें या नया खाता बनाएँ",
            "ईमेल पता", "पासवर्ड (कम से कम 6 अक्षर)",
            "लॉगिन", "खाता बनाएँ", "पासवर्ड भूल गए?",
            "अपना ईमेल पता लिखें।", "सही ईमेल पता लिखें।",
            "पासवर्ड लिखें।", "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए।",
            "Firebase कनेक्ट नहीं है। google-services.json जाँचें।",
            "कृपया प्रतीक्षा करें...", "लॉगिन विफल: ", "खाता सफलतापूर्वक बन गया।",
            "पासवर्ड रीसेट ईमेल भेज दिया गया है। इनबॉक्स देखें।",
            "SM CALL होम", "लॉगिन किया गया", "चैट", "ऑडियो कॉल", "वीडियो कॉल",
            "ग्रुप वीडियो (अधिकतम 20)", "लॉगआउट",
            "यह सुविधा अभी चालू नहीं है। इसे बाद में जोड़ा और जाँचा जाएगा।",
            "प्रोफ़ाइल सेव हो गई।"
        )
        else -> Labels(
            "স্বাগতম SM CALL-এ", "ইমেইল দিয়ে লগইন করুন অথবা নতুন অ্যাকাউন্ট খুলুন",
            "আপনার ইমেইল ঠিকানা", "পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)",
            "লগইন করুন", "নতুন অ্যাকাউন্ট তৈরি করুন", "পাসওয়ার্ড ভুলে গেছেন?",
            "আপনার ইমেইল লিখুন।", "সঠিক ইমেইল ঠিকানা লিখুন।",
            "আপনার পাসওয়ার্ড লিখুন।", "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।",
            "Firebase সংযুক্ত নেই। android/app/google-services.json পরীক্ষা করুন।",
            "অপেক্ষা করুন...", "লগইন করা যায়নি: ", "অ্যাকাউন্ট তৈরি হয়েছে।",
            "পাসওয়ার্ড রিসেটের ইমেইল পাঠানো হয়েছে। ইনবক্স দেখুন।",
            "SM CALL হোম", "লগইন করেছেন", "চ্যাট", "অডিও কল", "ভিডিও কল",
            "গ্রুপ ভিডিও (সর্বোচ্চ ২০ জন)", "লগআউট",
            "এই সুবিধাটি এখনো চালু হয়নি। পরে তৈরি ও পরীক্ষা করা হবে।",
            "প্রোফাইল সংরক্ষণ হয়েছে।"
        )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun baseScreen(): LinearLayout {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = false
        }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(32))
            minimumHeight = resources.displayMetrics.heightPixels
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(7, 27, 54), Color.rgb(18, 63, 112))
            )
            layoutDirection = if (language == "ar") View.LAYOUT_DIRECTION_RTL
                else View.LAYOUT_DIRECTION_LTR
        }
        scroll.addView(page, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        setContentView(scroll)
        return page
    }

    private fun addBrand(page: LinearLayout) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "SM CALL"
            textSize = 25f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        row.addView(brand, LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ))
        val languageButton = Button(this).apply {
            text = when (language) {
                "en" -> "English"
                "ar" -> "العربية"
                "hi" -> "हिन्दी"
                else -> "বাংলা"
            }
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(Color.rgb(28, 78, 125), 14)
            setOnClickListener { changeLanguage() }
        }
        row.addView(languageButton)
        page.addView(row)
        addSpace(page, 28)
    }

    private fun rounded(color: Int, radiusDp: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
        }

    private fun addSpace(page: LinearLayout, heightDp: Int) {
        page.addView(View(this), LinearLayout.LayoutParams(1, dp(heightDp)))
    }

    private fun addText(
        page: LinearLayout, text: String, size: Float, bold: Boolean = false
    ): TextView {
        val view = TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.WHITE)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            gravity = if (language == "ar") Gravity.RIGHT else Gravity.LEFT
        }
        page.addView(view, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        return view
    }

    private fun addInput(hint: String, inputType: Int): EditText =
        EditText(this).apply {
            this.hint = hint
            this.inputType = inputType
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

    private fun addButton(
        page: LinearLayout, text: String, onClick: () -> Unit,
        secondary: Boolean = false
    ): Button {
        val button = Button(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 15f
            setTextColor(Color.WHITE)
            background = rounded(
                if (secondary) Color.rgb(28, 78, 125) else Color.rgb(22, 199, 160),
                14
            )
            setOnClickListener { onClick() }
        }
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(54)
        )
        params.topMargin = dp(12)
        page.addView(button, params)
        return button
    }

    private fun renderLogin() {
        val copy = labels()
        val page = baseScreen()
        addBrand(page)
        addText(page, copy.welcome, 27f, true)
        addSpace(page, 8)
        addText(page, copy.intro, 16f)
        addSpace(page, 24)

        emailInput = addInput(
            copy.emailHint,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        page.addView(emailInput, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(54)
        ))

        passwordInput = addInput(
            copy.passwordHint,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        val passwordParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(54)
        ).apply { topMargin = dp(14) }
        page.addView(passwordInput, passwordParams)

        statusText = addText(page, "", 14f)
        addSpace(page, 6)
        loginButton = addButton(page, copy.login, { authenticate(createAccount = false) })
        createAccountButton = addButton(
            page, copy.createAccount, { authenticate(createAccount = true) }, true
        )
        resetPasswordButton = addButton(
            page, copy.resetPassword, { sendPasswordReset() }, true
        )
        addSpace(page, 22)
        addText(page, "Email and password • Firebase Authentication", 12f)
    }

    private fun changeLanguage() {
        val currentEmail = if (::emailInput.isInitialized) emailInput.text.toString() else ""
        val currentPassword = if (::passwordInput.isInitialized) passwordInput.text.toString() else ""
        val currentIndex = languages.indexOf(language).coerceAtLeast(0)
        language = languages[(currentIndex + 1) % languages.size]
        val signedInUser = auth?.currentUser
        if (signedInUser != null) {
            renderHome(signedInUser)
        } else {
            renderLogin()
            emailInput.setText(currentEmail)
            passwordInput.setText(currentPassword)
        }
    }

    private fun setAuthButtonsEnabled(enabled: Boolean) {
        loginButton.isEnabled = enabled
        createAccountButton.isEnabled = enabled
        resetPasswordButton.isEnabled = enabled
    }

    private fun authenticate(createAccount: Boolean) {
        val copy = labels()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (email.isBlank()) {
            statusText.text = copy.enterEmail
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            statusText.text = copy.invalidEmail
            return
        }
        if (password.isBlank()) {
            statusText.text = copy.enterPassword
            return
        }
        if (createAccount && password.length < 6) {
            statusText.text = copy.passwordTooShort
            return
        }

        val currentAuth = auth
        if (currentAuth == null) {
            statusText.text = copy.firebaseMissing
            return
        }

        setAuthButtonsEnabled(false)
        statusText.text = copy.working
        val task = if (createAccount) {
            currentAuth.createUserWithEmailAndPassword(email, password)
        } else {
            currentAuth.signInWithEmailAndPassword(email, password)
        }
        task.addOnSuccessListener { result ->
            val user = result.user
            if (user != null) {
                saveProfile(user, copy.accountCreated)
            } else {
                setAuthButtonsEnabled(true)
                statusText.text = copy.loginFailed
            }
        }.addOnFailureListener { error ->
            setAuthButtonsEnabled(true)
            statusText.text = localizedAuthError(error)
        }
    }

    private fun sendPasswordReset() {
        val copy = labels()
        val email = emailInput.text.toString().trim()
        if (email.isBlank()) {
            statusText.text = copy.enterEmail
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            statusText.text = copy.invalidEmail
            return
        }
        val currentAuth = auth
        if (currentAuth == null) {
            statusText.text = copy.firebaseMissing
            return
        }
        resetPasswordButton.isEnabled = false
        statusText.text = copy.working
        currentAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                resetPasswordButton.isEnabled = true
                statusText.text = copy.resetSent
            }
            .addOnFailureListener { error ->
                resetPasswordButton.isEnabled = true
                statusText.text = localizedAuthError(error)
            }
    }

    private fun localizedAuthError(error: Throwable): String {
        val code = (error as? FirebaseAuthException)?.errorCode.orEmpty()
        val message = when (code) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> when (language) {
                "en" -> "This email is already registered. Please log in."
                "ar" -> "هذا البريد مسجل بالفعل. يرجى تسجيل الدخول."
                "hi" -> "यह ईमेल पहले से पंजीकृत है। लॉगिन करें।"
                else -> "এই ইমেইল দিয়ে আগে অ্যাকাউন্ট খোলা হয়েছে। লগইন করুন।"
            }
            "ERROR_INVALID_EMAIL" -> labels().invalidEmail
            "ERROR_WEAK_PASSWORD" -> labels().passwordTooShort
            "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
                when (language) {
                    "en" -> "Email or password is incorrect."
                    "ar" -> "البريد الإلكتروني أو كلمة المرور غير صحيحة."
                    "hi" -> "ईमेल या पासवर्ड गलत है।"
                    else -> "ইমেইল অথবা পাসওয়ার্ড ভুল।"
                }
            "ERROR_TOO_MANY_REQUESTS" -> when (language) {
                "en" -> "Too many attempts. Please try again later."
                "ar" -> "محاولات كثيرة. حاول مرة أخرى لاحقاً."
                "hi" -> "बहुत अधिक प्रयास। बाद में कोशिश करें।"
                else -> "অনেকবার চেষ্টা হয়েছে। কিছুক্ষণ পরে আবার চেষ্টা করুন।"
            }
            "ERROR_NETWORK_REQUEST_FAILED" -> when (language) {
                "en" -> "Network error. Check your internet connection."
                "ar" -> "خطأ في الشبكة. تحقق من اتصال الإنترنت."
                "hi" -> "नेटवर्क त्रुटि। इंटरनेट जाँचें।"
                else -> "ইন্টারনেট সংযোগ পরীক্ষা করুন।"
            }
            else -> labels().loginFailed + (error.localizedMessage ?: error.javaClass.simpleName)
        }
        return message
    }

    private fun saveProfile(user: FirebaseUser, successMessage: String) {
        val database = FirebaseFirestore.getInstance()
        val publicProfile = hashMapOf<String, Any>(
            "uid" to user.uid,
            "name" to (user.email?.substringBefore("@") ?: "SM CALL User"),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val privateProfile = hashMapOf<String, Any>(
            "uid" to user.uid,
            "phone" to (user.phoneNumber ?: ""),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        database.collection("users").document(user.uid)
            .set(publicProfile, SetOptions.merge())
            .onSuccessTask {
                database.collection("privateUsers").document(user.uid)
                    .set(privateProfile, SetOptions.merge())
            }
            .addOnSuccessListener {
                renderHome(user)
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                renderHome(user)
                Toast.makeText(
                    this,
                    "Login হয়েছে, কিন্তু profile save হয়নি: " +
                        (error.localizedMessage ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun renderHome(user: FirebaseUser) {
        val copy = labels()
        val page = baseScreen()
        addBrand(page)
        addText(page, copy.homeTitle, 27f, true)
        addSpace(page, 8)
        addText(page, copy.signedInAs + ": " + (user.email ?: user.uid), 14f)
        addSpace(page, 20)

        addButton(page, copy.chats, {
            startActivity(Intent(this, ChatActivity::class.java).putExtra("language", language))
        })
        addButton(page, copy.audio, { showNotReady(copy.notReady) }, true)
        addButton(page, copy.video, { showNotReady(copy.notReady) }, true)
        addButton(page, copy.groupVideo, { showNotReady(copy.notReady) }, true)
        addSpace(page, 16)
        addButton(page, copy.logout, {
            auth?.signOut()
            renderLogin()
        }, true)
    }

    private fun showNotReady(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private companion object {
        const val STATE_LANGUAGE = "state_language"
        const val STATE_EMAIL = "state_email"
    }
}
