package com.smcall.app

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MainActivity : Activity() {
    private var auth: FirebaseAuth? = null
    private var language = "bn"
    private var signupMode = false
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var statusText: TextView
    private var messageListener: ListenerRegistration? = null
    private var incomingCallListener: ListenerRegistration? = null
    private val handledIncomingCalls = mutableSetOf<String>()
    private val phoneAuthRequestCode = 4201

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

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == phoneAuthRequestCode && resultCode == RESULT_OK) {
            auth?.currentUser?.let { refreshSignedInUser(it) }
        }
    }

    override fun onDestroy() {
        incomingCallListener?.remove()
        messageListener?.remove()
        super.onDestroy()
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

    private fun isVerifiedAccount(user: FirebaseUser): Boolean =
        VerifiedAccountPolicy.isVerified(user.isEmailVerified, user.phoneNumber)

    private fun renderCurrentScreen() {
        val user = auth?.currentUser
        when {
            user == null -> renderAuth()
            isVerifiedAccount(user) -> renderHome(user)
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
        if (!signupMode) {
            addButton(page, copy.forgot, true) { resetPassword() }
            val phoneLabel = when (language) {
                "en" -> "Sign in with phone OTP"
                "ar" -> "تسجيل الدخول برمز الهاتف"
                "hi" -> "फोन OTP से लॉगिन"
                else -> "ফোন নম্বর দিয়ে OTP লগইন"
            }
            addButton(page, phoneLabel, true) {
                val intent = Intent(this, PhoneOtpActivity::class.java)
                    .putExtra(PhoneOtpActivity.EXTRA_LANGUAGE, language)
                startActivityForResult(intent, phoneAuthRequestCode)
            }
        }
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
            else if (isVerifiedAccount(fresh)) saveProfile(fresh)
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
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!user.phoneNumber.isNullOrBlank()) {
            profile["phoneVerified"] = true
        } else {
            profile["emailVerified"] = user.isEmailVerified
        }
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
        addText(page, user.email ?: user.phoneNumber.orEmpty(), 15f); addSpace(page, 18)
        listenForIncomingCalls(user)
        addButton(page, copy.chats) {
            Toast.makeText(this, "চ্যাট খুলছে...", Toast.LENGTH_SHORT).show()
            openChats(user)
        }
        addText(page, "বন্ধুর চ্যাট খুলে অডিও বা ভিডিও কল শুরু করুন।", 14f)
        addButton(page, copy.logout, true) { auth?.signOut(); signupMode = false; renderAuth() }
    }


    private fun openChats(user: FirebaseUser) {
        messageListener?.remove()
        val page = baseScreen(); addBrand(page)
        addText(page, "চ্যাট", 27f, true); addSpace(page, 8)
        addText(page, "আপনার User ID বন্ধুকে পাঠান। বন্ধুর User ID দিয়ে নতুন ব্যক্তিগত চ্যাট শুরু করুন।", 14f)
        addSpace(page, 8)
        addText(page, user.uid, 12f)
        addButton(page, "আমার User ID কপি করুন", true) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("SM CALL User ID", user.uid))
            Toast.makeText(this, "User ID কপি হয়েছে।", Toast.LENGTH_SHORT).show()
        }
        addSpace(page, 14)
        val friendId = addInput(page, "বন্ধুর User ID লিখুন", InputType.TYPE_CLASS_TEXT)
        addButton(page, "নতুন চ্যাট শুরু করুন") {
            val id = friendId.text.toString().trim()
            if (id.isBlank() || id == user.uid) {
                Toast.makeText(this, "অন্য ব্যবহারকারীর সঠিক User ID দিন।", Toast.LENGTH_LONG).show()
            } else openPersonalChat(user, id)
        }
        addSpace(page, 18)
        addText(page, "আগের চ্যাট", 19f, true)
        FirebaseFirestore.getInstance().collection("chats")
            .whereArrayContains("members", user.uid)
            .addSnapshotListener(this) { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "চ্যাট লোড হয়নি: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                snapshot.documents.forEach { chat ->
                    val members = chat.get("members") as? List<*> ?: return@forEach
                    val otherUid = members.filterIsInstance<String>().firstOrNull { it != user.uid } ?: return@forEach
                    FirebaseFirestore.getInstance().collection("users").document(otherUid).get()
                        .addOnSuccessListener { profile ->
                            val title = profile.getString("name")?.takeIf { it.isNotBlank() } ?: otherUid.take(10)
                            addButton(page, title) { openChatMessages(user, chat.id, title) }
                        }
                }
            }
        addSpace(page, 12)
        addButton(page, "হোমে ফিরে যান", true) { renderHome(user) }
    }

    private fun openPersonalChat(user: FirebaseUser, friendUid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(friendUid).get()
            .addOnSuccessListener { profile ->
                if (!profile.exists()) {
                    Toast.makeText(this, "এই User ID পাওয়া যায়নি।", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }
                val ids = listOf(user.uid, friendUid).sorted()
                val chatId = ids.joinToString("_")
                val chatRef = db.collection("chats").document(chatId)
                chatRef.get().addOnSuccessListener { existing ->
                    val title = profile.getString("name")?.takeIf { it.isNotBlank() } ?: friendUid.take(10)
                    if (existing.exists()) openChatMessages(user, chatId, title)
                    else {
                        val data = hashMapOf<String, Any>(
                            "members" to ids,
                            "type" to "personal",
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        chatRef.set(data).addOnSuccessListener {
                            openChatMessages(user, chatId, title)
                        }.addOnFailureListener {
                            Toast.makeText(this, "চ্যাট তৈরি হয়নি: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "চ্যাট খোলা যায়নি: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "ব্যবহারকারী খুঁজে পাওয়া যায়নি: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun openChatMessages(user: FirebaseUser, chatId: String, title: String) {
        messageListener?.remove()
        renderChatMessages(user, chatId, title)
    }

    private fun renderChatMessages(user: FirebaseUser, chatId: String, title: String) {
        val page = baseScreen(); addBrand(page)
        addText(page, title, 23f, true); addSpace(page, 6)
        addText(page, "ব্যক্তিগত চ্যাট", 13f)
        val peerUid = chatId.split("_").firstOrNull { it != user.uid }
        if (peerUid != null) {
            addButton(page, "অডিও কল শুরু করুন") { startOutgoingCall(user, peerUid, "audio") }
            addButton(page, "ভিডিও কল শুরু করুন", true) { startOutgoingCall(user, peerUid, "video") }
        }
        val messagesBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val messageScroll = ScrollView(this).apply {
            isFillViewport = false
            addView(messagesBox, ViewGroup.LayoutParams(-1, -2))
        }
        val scrollParams = LinearLayout.LayoutParams(-1, 0, 1f)
        scrollParams.topMargin = dp(12); scrollParams.bottomMargin = dp(12)
        page.addView(messageScroll, scrollParams)
        val input = addInput(page, "মেসেজ লিখুন...", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        addButton(page, "মেসেজ পাঠান") {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                val data = hashMapOf<String, Any>(
                    "sender" to user.uid,
                    "text" to text,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                FirebaseFirestore.getInstance().collection("chats").document(chatId)
                    .collection("messages").add(data)
                    .addOnSuccessListener { input.text.clear() }
                    .addOnFailureListener {
                        Toast.makeText(this, "মেসেজ পাঠানো যায়নি: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }
        addButton(page, "চ্যাট তালিকায় ফিরুন", true) { messageListener?.remove(); openChats(user) }
        messageListener = FirebaseFirestore.getInstance().collection("chats").document(chatId)
            .collection("messages").orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener(this) { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "মেসেজ লোড হয়নি: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                messagesBox.removeAllViews()
                snapshot?.documents?.forEach { doc ->
                    val sender = doc.getString("sender").orEmpty()
                    val body = doc.getString("text").orEmpty()
                    val line = TextView(this).apply {
                        text = (if (sender == user.uid) "আপনি: " else "$title: ") + body
                        textSize = 15f
                        setTextColor(Color.WHITE)
                        setPadding(dp(12), dp(10), dp(12), dp(10))
                        background = rounded(if (sender == user.uid) Color.rgb(22, 120, 110) else Color.rgb(28, 78, 125))
                    }
                    val params = LinearLayout.LayoutParams(-1, -2)
                    params.bottomMargin = dp(8)
                    messagesBox.addView(line, params)
                }
                messageScroll.post { messageScroll.fullScroll(View.FOCUS_DOWN) }
            }
    }

    private fun startOutgoingCall(user: FirebaseUser, targetUid: String, type: String) {
        if (!isVerifiedAccount(user)) { Toast.makeText(this, "আগে ইমেইল বা ফোন নম্বর যাচাই করুন।", Toast.LENGTH_LONG).show(); return }
        val room = hashMapOf<String, Any>(
            "hostUid" to user.uid, "targetUid" to targetUid, "members" to listOf(user.uid),
            "callType" to type, "status" to "ringing", "maxParticipants" to 5,
            "createdAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp()
        )
        FirebaseFirestore.getInstance().collection("callRooms").add(room).addOnSuccessListener { ref ->
            startActivity(Intent(this, CallActivity::class.java).putExtra(CallActivity.EXTRA_ROOM_ID, ref.id)
                .putExtra(CallActivity.EXTRA_CALLER, true).putExtra(CallActivity.EXTRA_CALL_TYPE, type))
        }.addOnFailureListener { Toast.makeText(this, "কল শুরু হয়নি: ${it.localizedMessage}", Toast.LENGTH_LONG).show() }
    }

    private fun listenForIncomingCalls(user: FirebaseUser) {
        incomingCallListener?.remove()
        incomingCallListener = FirebaseFirestore.getInstance().collection("callRooms")
            .whereEqualTo("targetUid", user.uid).whereEqualTo("status", "ringing")
            .addSnapshotListener(this) { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                snapshot.documents.forEach { room ->
                    if (!handledIncomingCalls.add(room.id)) return@forEach
                    val callerUid = room.getString("hostUid") ?: return@forEach
                    val type = room.getString("callType") ?: "audio"
                    FirebaseFirestore.getInstance().collection("users").document(callerUid).get().addOnSuccessListener { profile ->
                        val callerName = profile.getString("name") ?: "SM CALL User"
                        AlertDialog.Builder(this).setTitle(if (type == "video") "ভিডিও কল" else "অডিও কল")
                            .setMessage("$callerName আপনাকে কল করছেন।").setCancelable(false)
                            .setPositiveButton("গ্রহণ করুন") { _, _ ->
                                FirebaseFirestore.getInstance().collection("callRooms").document(room.id).update(
                                    "members", FieldValue.arrayUnion(user.uid), "status", "active", "updatedAt", FieldValue.serverTimestamp()
                                ).addOnSuccessListener {
                                    startActivity(Intent(this, CallActivity::class.java).putExtra(CallActivity.EXTRA_ROOM_ID, room.id)
                                        .putExtra(CallActivity.EXTRA_CALLER, false).putExtra(CallActivity.EXTRA_CALL_TYPE, type))
                                }.addOnFailureListener { Toast.makeText(this, "কল গ্রহণ হয়নি: ${it.localizedMessage}", Toast.LENGTH_LONG).show() }
                            }.setNegativeButton("এখন নয়") { _, _ ->
                                FirebaseFirestore.getInstance().collection("callRooms").document(room.id)
                                    .update("status", "ended", "updatedAt", FieldValue.serverTimestamp())
                            }.show()
                    }
                }
            }
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
