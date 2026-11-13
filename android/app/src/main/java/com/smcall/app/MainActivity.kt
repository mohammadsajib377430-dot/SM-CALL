package com.smcall.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
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
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {
    private var auth: FirebaseAuth? = null
    private var language = "bn"
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var resendTimer: CountDownTimer? = null
    private var resendAvailableAtMillis = 0L
    private var otpRequestInProgress = false

    private lateinit var phoneInput: EditText
    private lateinit var otpInput: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyButton: Button
    private lateinit var statusText: TextView

    private val languages = listOf("bn", "en", "ar", "hi")

    private data class Labels(
        val welcome: String,
        val intro: String,
        val phoneHint: String,
        val sendOtp: String,
        val otpHint: String,
        val verifyOtp: String,
        val otpSent: String,
        val verifying: String,
        val firebaseMissing: String,
        val invalidPhone: String,
        val invalidOtp: String,
        val otpFailed: String,
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
        verificationId = savedInstanceState?.getString(STATE_VERIFICATION_ID)
        resendAvailableAtMillis =
            savedInstanceState?.getLong(STATE_RESEND_AVAILABLE_AT) ?: 0L

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
            savedInstanceState?.let { state ->
                phoneInput.setText(state.getString(STATE_PHONE).orEmpty())
            }
            restorePendingOtpUi()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_LANGUAGE, language)
        outState.putString(STATE_VERIFICATION_ID, verificationId)
        outState.putLong(STATE_RESEND_AVAILABLE_AT, resendAvailableAtMillis)
        if (::phoneInput.isInitialized) {
            outState.putString(STATE_PHONE, phoneInput.text.toString())
        }
    }

    override fun onDestroy() {
        resendTimer?.cancel()
        resendTimer = null
        super.onDestroy()
    }

    private fun labels(): Labels = when (language) {
        "en" -> Labels(
            "Welcome to SM CALL",
            "Sign in with your phone number",
            "Phone number with country code, e.g. +8801...",
            "Send SMS code",
            "6-digit verification code",
            "Verify and sign in",
            "Code sent. Check your phone.",
            "Verifying your number...",
            "Firebase is not connected yet. Add google-services.json to android/app.",
            "Enter a valid international number, e.g. +8801XXXXXXXXX.",
            "Enter the 6-digit code.",
            "Could not verify: ",
            "SM CALL Home",
            "Signed in as",
            "Chats",
            "Audio call",
            "Video call",
            "Group video (up to 20)",
            "Sign out",
            "This feature is not active yet. It will be added and tested in a later step.",
            "Profile saved."
        )
        "ar" -> Labels(
            "مرحباً بك في SM CALL",
            "سجّل الدخول باستخدام رقم هاتفك",
            "رقم الهاتف مع رمز الدولة، مثل +8801...",
            "إرسال رمز SMS",
            "رمز التحقق المكوّن من 6 أرقام",
            "تحقق وسجّل الدخول",
            "تم إرسال الرمز. تحقق من هاتفك.",
            "جارٍ التحقق من رقمك...",
            "Firebase غير متصل بعد. أضف google-services.json إلى android/app.",
            "أدخل رقمًا دوليًا صحيحًا، مثل +8801XXXXXXXXX.",
            "أدخل الرمز المكوّن من 6 أرقام.",
            "تعذر التحقق: ",
            "الصفحة الرئيسية لـ SM CALL",
            "تم تسجيل الدخول باسم",
            "المحادثات",
            "مكالمة صوتية",
            "مكالمة فيديو",
            "فيديو جماعي (حتى 20)",
            "تسجيل الخروج",
            "هذه الميزة غير مفعّلة بعد. ستتم إضافتها واختبارها في خطوة لاحقة.",
            "تم حفظ الملف الشخصي."
        )
        "hi" -> Labels(
            "SM CALL में आपका स्वागत है",
            "अपने फ़ोन नंबर से लॉगिन करें",
            "देश कोड सहित फ़ोन नंबर, जैसे +8801...",
            "SMS कोड भेजें",
            "6 अंकों का सत्यापन कोड",
            "सत्यापित करें और लॉगिन करें",
            "कोड भेज दिया गया है। अपना फ़ोन देखें।",
            "आपका नंबर सत्यापित हो रहा है...",
            "Firebase अभी कनेक्ट नहीं है। android/app में google-services.json जोड़ें।",
            "सही अंतरराष्ट्रीय नंबर लिखें, जैसे +8801XXXXXXXXX।",
            "6 अंकों का कोड लिखें।",
            "सत्यापन नहीं हुआ: ",
            "SM CALL होम",
            "लॉगिन किया गया",
            "चैट",
            "ऑडियो कॉल",
            "वीडियो कॉल",
            "ग्रुप वीडियो (अधिकतम 20)",
            "लॉगआउट",
            "यह सुविधा अभी चालू नहीं है। इसे बाद के चरण में जोड़ा और जाँचा जाएगा।",
            "प्रोफ़ाइल सेव हो गई।"
        )
        else -> Labels(
            "স্বাগতম SM CALL-এ",
            "আপনার ফোন নম্বর দিয়ে লগইন করুন",
            "দেশের কোডসহ ফোন নম্বর লিখুন, যেমন +8801...",
            "SMS OTP পাঠান",
            "৬ সংখ্যার OTP লিখুন",
            "যাচাই করে লগইন করুন",
            "OTP পাঠানো হয়েছে। ফোন দেখুন।",
            "আপনার নম্বর যাচাই হচ্ছে...",
            "Firebase এখনো সংযুক্ত হয়নি। android/app ফোল্ডারে google-services.json যোগ করতে হবে।",
            "সঠিক আন্তর্জাতিক নম্বর লিখুন, যেমন +8801XXXXXXXXX।",
            "৬ সংখ্যার OTP লিখুন।",
            "যাচাই করা যায়নি: ",
            "SM CALL হোম",
            "লগইন করেছেন",
            "চ্যাট",
            "অডিও কল",
            "ভিডিও কল",
            "গ্রুপ ভিডিও (সর্বোচ্চ ২০ জন)",
            "লগআউট",
            "এই সুবিধাটি এখনো চালু হয়নি। পরের ধাপে তৈরি ও পরীক্ষা করা হবে।",
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
            layoutDirection = if (language == "ar") {
                View.LAYOUT_DIRECTION_RTL
            } else {
                View.LAYOUT_DIRECTION_LTR
            }
        }
        scroll.addView(
            page,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
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
        row.addView(
            brand,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
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
        page.addView(
            View(this),
            LinearLayout.LayoutParams(1, dp(heightDp))
        )
    }

    private fun addText(
        page: LinearLayout,
        text: String,
        size: Float,
        bold: Boolean = false
    ): TextView {
        val view = TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.WHITE)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            gravity = if (language == "ar") Gravity.RIGHT else Gravity.LEFT
        }
        page.addView(
            view,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
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
        page: LinearLayout,
        text: String,
        onClick: () -> Unit,
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
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(54)
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

        phoneInput = addInput(copy.phoneHint, InputType.TYPE_CLASS_PHONE)
        page.addView(
            phoneInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )

        otpInput = addInput(copy.otpHint, InputType.TYPE_CLASS_NUMBER)
        otpInput.textDirection = View.TEXT_DIRECTION_LTR
        otpInput.visibility = View.GONE
        val otpParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(54)
        )
        otpParams.topMargin = dp(14)
        page.addView(otpInput, otpParams)

        statusText = addText(page, "", 14f)
        addSpace(page, 6)

        sendOtpButton = addButton(page, copy.sendOtp, { sendOtp() })
        verifyButton = addButton(page, copy.verifyOtp, { verifyOtp() }, true)
        verifyButton.visibility = View.GONE

        addSpace(page, 22)
        addText(
            page,
            "Android app foundation • Firebase phone authentication",
            12f
        )
    }

    private fun changeLanguage() {
        val currentPhone = if (::phoneInput.isInitialized) phoneInput.text.toString() else ""
        val currentOtp = if (::otpInput.isInitialized) otpInput.text.toString() else ""
        val currentIndex = languages.indexOf(language).coerceAtLeast(0)
        language = languages[(currentIndex + 1) % languages.size]
        val signedInUser = auth?.currentUser
        if (signedInUser != null) {
            renderHome(signedInUser)
        } else {
            renderLogin()
            phoneInput.setText(currentPhone)
            otpInput.setText(currentOtp)
            restorePendingOtpUi()
        }
    }

    private fun resendReadyText(): String = when (language) {
        "en" -> "Resend SMS code"
        "ar" -> "إعادة إرسال رمز SMS"
        "hi" -> "SMS कोड फिर भेजें"
        else -> "আবার SMS OTP পাঠান"
    }

    private fun resendCountdownText(seconds: Long): String = when (language) {
        "en" -> "Resend in ${seconds}s"
        "ar" -> "${seconds} ث لإعادة الإرسال"
        "hi" -> "${seconds} सेकंड में फिर भेजें"
        else -> "${seconds} সেকেন্ড পরে আবার পাঠান"
    }

    private fun startResendCooldown(durationMillis: Long = 30_000L) {
        resendTimer?.cancel()
        if (durationMillis <= 0L) {
            resendAvailableAtMillis = 0L
            sendOtpButton.isEnabled = true
            sendOtpButton.text = resendReadyText()
            resendTimer = null
            return
        }

        resendAvailableAtMillis = System.currentTimeMillis() + durationMillis
        sendOtpButton.isEnabled = false
        resendTimer = object : CountDownTimer(durationMillis, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                sendOtpButton.text =
                    resendCountdownText((millisUntilFinished + 999L) / 1_000L)
            }

            override fun onFinish() {
                resendTimer = null
                resendAvailableAtMillis = 0L
                sendOtpButton.isEnabled = true
                sendOtpButton.text = resendReadyText()
            }
        }.start()
    }

    private fun restorePendingOtpUi() {
        if (verificationId == null) return
        otpInput.visibility = View.VISIBLE
        verifyButton.visibility = View.VISIBLE
        sendOtpButton.text = resendReadyText()
        val remaining = resendAvailableAtMillis - System.currentTimeMillis()
        if (remaining > 0L) {
            startResendCooldown(remaining)
        } else {
            resendAvailableAtMillis = 0L
            sendOtpButton.isEnabled = true
        }
    }

    private fun sendOtp() {
        val copy = labels()
        val phone = phoneInput.text.toString().trim()
        if (!Regex("^\\+[1-9][0-9]{7,14}$").matches(phone)) {
            statusText.text = copy.invalidPhone
            return
        }

        val currentAuth = auth
        if (currentAuth == null) {
            statusText.text = copy.firebaseMissing
            return
        }

        if (otpRequestInProgress) return
        if (resendAvailableAtMillis > System.currentTimeMillis()) return

        otpRequestInProgress = true
        sendOtpButton.isEnabled = false
        statusText.text = copy.verifying
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                runOnUiThread {
                    otpRequestInProgress = false
                    resendTimer?.cancel()
                    resendTimer = null
                    resendAvailableAtMillis = 0L
                    sendOtpButton.isEnabled = false
                    statusText.text = copy.verifying
                    signInWithCredential(credential)
                }
            }

            override fun onVerificationFailed(error: com.google.firebase.FirebaseException) {
                runOnUiThread {
                    otpRequestInProgress = false
                    sendOtpButton.isEnabled = true
                    statusText.text =
                        copy.otpFailed + (error.localizedMessage ?: error.javaClass.simpleName)
                }
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(id, token)
                verificationId = id
                resendToken = token
                runOnUiThread {
                    otpRequestInProgress = false
                    otpInput.visibility = View.VISIBLE
                    verifyButton.visibility = View.VISIBLE
                    sendOtpButton.text = resendReadyText()
                    startResendCooldown()
                    statusText.text = copy.otpSent
                }
            }

            override fun onCodeAutoRetrievalTimeOut(id: String) {
                verificationId = id
                runOnUiThread {
                    statusText.text = copy.otpSent
                }
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(currentAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
        resendToken?.let { optionsBuilder.setForceResendingToken(it) }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private fun verifyOtp() {
        val copy = labels()
        val code = otpInput.text.toString().trim()
        val id = verificationId
        if (id == null) {
            statusText.text = copy.otpSent
            return
        }
        if (!Regex("^[0-9]{6}$").matches(code)) {
            statusText.text = copy.invalidOtp
            return
        }
        statusText.text = copy.verifying
        signInWithCredential(PhoneAuthProvider.getCredential(id, code))
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        val currentAuth = auth ?: return
        currentAuth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    saveProfile(user)
                } else {
                    statusText.text = labels().otpFailed
                }
            }
            .addOnFailureListener { error ->
                statusText.text = labels().otpFailed + (error.localizedMessage ?: "")
            }
    }

    private fun saveProfile(user: FirebaseUser) {
        val database = FirebaseFirestore.getInstance()
        val publicProfile = hashMapOf<String, Any>(
            "uid" to user.uid,
            "name" to "SM CALL User",
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
                Toast.makeText(this, labels().profileSaved, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                renderHome(user)
                Toast.makeText(
                    this,
                    "Login হয়েছে, কিন্তু profile save হয়নি: " + (error.localizedMessage ?: ""),
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
        addText(page, copy.signedInAs + ": " + (user.phoneNumber ?: user.uid), 14f)
        addSpace(page, 20)

        addButton(page, copy.chats, {
            showNotReady(copy.notReady)
        })
        addButton(page, copy.audio, {
            showNotReady(copy.notReady)
        }, true)
        addButton(page, copy.video, {
            showNotReady(copy.notReady)
        }, true)
        addButton(page, copy.groupVideo, {
            showNotReady(copy.notReady)
        }, true)
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
        const val STATE_VERIFICATION_ID = "state_verification_id"
        const val STATE_RESEND_AVAILABLE_AT = "state_resend_available_at"
        const val STATE_PHONE = "state_phone"
    }
}
