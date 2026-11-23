package com.smcall.app

import android.app.Activity
import android.graphics.Color
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
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneOtpActivity : Activity() {
    private var auth: FirebaseAuth? = null
    private var language = "bn"
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var signingIn = false

    private lateinit var phoneInput: EditText
    private lateinit var codeInput: EditText
    private lateinit var statusText: TextView
    private lateinit var verifyButton: Button
    private lateinit var resendButton: Button

    private data class Copy(
        val title: String,
        val intro: String,
        val phoneHint: String,
        val send: String,
        val codeHint: String,
        val verify: String,
        val resend: String,
        val back: String,
        val invalidPhone: String,
        val invalidCode: String,
        val sending: String,
        val sent: String,
        val signingIn: String,
        val missingFirebase: String,
        val failed: String,
        val billing: String,
        val quota: String,
        val tooMany: String,
        val invalidOtp: String,
        val expired: String,
        val providerOff: String
    )

    private fun copy(): Copy = when (language) {
        "en" -> Copy(
            "Sign in with phone", "Enter your phone number in international format, including +country code.",
            "+8801712345678", "Send verification code", "6-digit code", "Verify and sign in",
            "Resend code", "Back", "Enter a valid E.164 number, for example +8801712345678.",
            "Enter the 6-digit code.", "Sending SMS code…", "Code sent. Check your SMS.",
            "Verifying…", "Firebase is not configured for this app.", "Phone verification failed.",
            "Firebase Phone Auth billing is not enabled. Use a configured test phone number or enable billing.",
            "SMS quota exceeded. Use a Firebase test phone number or wait for quota reset.",
            "Too many attempts. Wait before trying again.", "The verification code is incorrect.",
            "The verification session expired. Request a new code.", "Phone sign-in is disabled in Firebase Authentication."
        )
        "ar" -> Copy(
            "تسجيل الدخول بالهاتف", "أدخل رقم الهاتف بالصيغة الدولية مع رمز الدولة وعلامة +.",
            "+8801712345678", "إرسال رمز التحقق", "رمز من 6 أرقام", "تحقق وسجّل الدخول",
            "إعادة إرسال الرمز", "رجوع", "أدخل رقمًا دوليًا صحيحًا مثل +8801712345678.",
            "أدخل الرمز المكوّن من 6 أرقام.", "جارٍ إرسال الرمز…", "تم إرسال الرمز. تحقق من الرسائل.",
            "جارٍ التحقق…", "Firebase غير مُعد لهذا التطبيق.", "فشل التحقق من رقم الهاتف.",
            "فوترة Firebase Phone Auth غير مفعّلة. استخدم رقم اختبار أو فعّل الفوترة.",
            "تم تجاوز حصة الرسائل. استخدم رقم اختبار أو انتظر إعادة تعيين الحصة.",
            "محاولات كثيرة. انتظر قبل المحاولة مجددًا.", "رمز التحقق غير صحيح.",
            "انتهت جلسة التحقق. اطلب رمزًا جديدًا.", "تسجيل الدخول بالهاتف معطّل في Firebase."
        )
        "hi" -> Copy(
            "फोन से लॉगिन", "देश के कोड और + के साथ अंतरराष्ट्रीय प्रारूप में फोन नंबर लिखें।",
            "+8801712345678", "OTP भेजें", "6 अंकों का OTP", "OTP जाँचें और लॉगिन करें",
            "OTP फिर भेजें", "वापस", "+8801712345678 जैसा सही अंतरराष्ट्रीय नंबर लिखें।",
            "6 अंकों का OTP लिखें।", "OTP भेजा जा रहा है…", "OTP भेज दिया गया। SMS देखें।",
            "जाँच हो रही है…", "इस ऐप के लिए Firebase सेट नहीं है।", "फोन सत्यापन विफल हुआ।",
            "Firebase Phone Auth billing चालू नहीं है। Test phone number उपयोग करें या billing चालू करें।",
            "SMS quota समाप्त है। Test phone number उपयोग करें या quota reset होने दें।",
            "बहुत अधिक प्रयास हुए। थोड़ी देर बाद कोशिश करें।", "OTP गलत है।",
            "सत्यापन सत्र समाप्त हो गया। नया OTP माँगें।", "Firebase में Phone sign-in बंद है।"
        )
        else -> Copy(
            "ফোন নম্বর দিয়ে লগইন", "দেশের কোডসহ আন্তর্জাতিক ফরম্যাটে ফোন নম্বর লিখুন।",
            "+8801712345678", "OTP পাঠান", "৬ সংখ্যার OTP", "OTP যাচাই করে লগইন",
            "আবার OTP পাঠান", "ফিরে যান", "+8801712345678-এর মতো সঠিক আন্তর্জাতিক নম্বর লিখুন।",
            "৬ সংখ্যার OTP লিখুন।", "OTP পাঠানো হচ্ছে…", "OTP পাঠানো হয়েছে। SMS দেখুন।",
            "যাচাই হচ্ছে…", "এই অ্যাপে Firebase সেটআপ নেই।", "ফোন নম্বর যাচাই করা যায়নি।",
            "Firebase Phone Auth-এর Billing চালু নেই। Test Phone Number ব্যবহার করুন অথবা Billing চালু করুন।",
            "SMS কোটা শেষ। Test Phone Number ব্যবহার করুন অথবা কোটা রিসেট হওয়া পর্যন্ত অপেক্ষা করুন।",
            "অনেকবার চেষ্টা হয়েছে। কিছুক্ষণ পরে আবার চেষ্টা করুন।", "OTP সঠিক নয়।",
            "যাচাইয়ের সময় শেষ। নতুন OTP পাঠান।", "Firebase Authentication-এ Phone Sign-in বন্ধ আছে।"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = intent.getStringExtra(EXTRA_LANGUAGE)?.takeIf {
            it in setOf("bn", "en", "ar", "hi")
        } ?: "bn"
        auth = if (FirebaseApp.getApps(this).isNotEmpty()) FirebaseAuth.getInstance() else null
        render()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(14).toFloat()
    }

    private fun addInput(page: LinearLayout, hint: String, inputType: Int): EditText {
        val field = EditText(this).apply {
            this.hint = hint
            this.inputType = inputType
            textSize = 16f
            setSingleLine(true)
            setTextColor(Color.rgb(12, 35, 61))
            setHintTextColor(Color.rgb(90, 110, 130))
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = rounded(Color.WHITE)
        }
        val params = LinearLayout.LayoutParams(-1, dp(54))
        params.topMargin = dp(12)
        page.addView(field, params)
        return field
    }

    private fun addButton(page: LinearLayout, label: String, secondary: Boolean = false, action: () -> Unit): Button {
        val button = Button(this).apply {
            text = label
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(if (secondary) Color.rgb(28, 78, 125) else Color.rgb(22, 199, 160))
            setOnClickListener { action() }
        }
        val params = LinearLayout.LayoutParams(-1, dp(54))
        params.topMargin = dp(12)
        page.addView(button, params)
        return button
    }

    private fun render() {
        val text = copy()
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            layoutDirection = if (language == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            setPadding(dp(24), dp(28), dp(24), dp(32))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(7, 27, 54), Color.rgb(18, 63, 112))
            )
        }
        scroll.addView(page, ViewGroup.LayoutParams(-1, -2))
        setContentView(scroll)

        val brand = TextView(this).apply {
            text = "SM CALL"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = if (language == "ar") Gravity.RIGHT else Gravity.LEFT
        }
        page.addView(brand, LinearLayout.LayoutParams(-1, -2))

        val title = TextView(this).apply {
            text = text.title
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = if (language == "ar") Gravity.RIGHT else Gravity.LEFT
        }
        val titleParams = LinearLayout.LayoutParams(-1, -2)
        titleParams.topMargin = dp(28)
        page.addView(title, titleParams)

        val intro = TextView(this).apply {
            text = text.intro
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = if (language == "ar") Gravity.RIGHT else Gravity.LEFT
        }
        page.addView(intro, LinearLayout.LayoutParams(-1, -2))

        phoneInput = addInput(page, text.phoneHint, InputType.TYPE_CLASS_PHONE)
        statusText = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = if (language == "ar") Gravity.RIGHT else Gravity.LEFT
        }
        val statusParams = LinearLayout.LayoutParams(-1, -2)
        statusParams.topMargin = dp(12)
        page.addView(statusText, statusParams)

        addButton(page, text.send) { requestCode(forceResend = false) }

        codeInput = addInput(page, text.codeHint, InputType.TYPE_CLASS_NUMBER)
        codeInput.visibility = View.GONE

        verifyButton = addButton(page, text.verify) { verifyCode() }
        verifyButton.visibility = View.GONE

        resendButton = addButton(page, text.resend, secondary = true) {
            requestCode(forceResend = true)
        }
        resendButton.visibility = View.GONE

        addButton(page, text.back, secondary = true) {
            setResult(RESULT_CANCELED)
            finish()
        }

        if (auth == null) statusText.text = text.missingFirebase
    }

    private fun requestCode(forceResend: Boolean) {
        val text = copy()
        val currentAuth = auth
        if (currentAuth == null) {
            statusText.text = text.missingFirebase
            return
        }
        val phone = PhoneNumberNormalizer.normalizePhone(phoneInput.text.toString())
        if (!PhoneNumberNormalizer.isValidE164(phone)) {
            statusText.text = text.invalidPhone
            return
        }

        statusText.text = text.sending
        val options = PhoneAuthOptions.newBuilder(currentAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
        if (forceResend) resendToken?.let { options.setForceResendingToken(it) }
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signIn(credential)
        }

        override fun onVerificationFailed(error: FirebaseException) {
            statusText.text = authError(error)
        }

        override fun onCodeSent(
            id: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            verificationId = id
            resendToken = token
            codeInput.visibility = View.VISIBLE
            verifyButton.visibility = View.VISIBLE
            resendButton.visibility = View.VISIBLE
            statusText.text = copy().sent
        }

        override fun onCodeAutoRetrievalTimeOut(id: String) {
            verificationId = id
            statusText.text = copy().expired
        }
    }

    private fun verifyCode() {
        val text = copy()
        val id = verificationId
        val code = PhoneNumberNormalizer.normalizeDigits(codeInput.text.toString()).trim()
        if (id.isNullOrBlank()) {
            statusText.text = text.expired
            return
        }
        if (!code.matches(Regex("^[0-9]{6}$"))) {
            statusText.text = text.invalidCode
            return
        }
        signIn(PhoneAuthProvider.getCredential(id, code))
    }

    private fun signIn(credential: PhoneAuthCredential) {
        val currentAuth = auth ?: return
        if (signingIn) return
        signingIn = true
        statusText.text = copy().signingIn
        currentAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { error ->
                signingIn = false
                statusText.text = authError(error)
            }
    }

    private fun authError(error: Throwable): String {
        val code = (error as? FirebaseAuthException)?.errorCode.orEmpty()
        val text = copy()
        return when (code) {
            "ERROR_BILLING_NOT_ENABLED" -> text.billing
            "ERROR_QUOTA_EXCEEDED" -> text.quota
            "ERROR_TOO_MANY_REQUESTS" -> text.tooMany
            "ERROR_INVALID_VERIFICATION_CODE" -> text.invalidOtp
            "ERROR_SESSION_EXPIRED" -> text.expired
            "ERROR_OPERATION_NOT_ALLOWED" -> text.providerOff
            "ERROR_INVALID_PHONE_NUMBER" -> text.invalidPhone
            else -> error.localizedMessage ?: text.failed
        }
    }

    companion object {
        const val EXTRA_LANGUAGE = "smcall.language"
    }
}
