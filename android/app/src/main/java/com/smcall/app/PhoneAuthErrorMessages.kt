package com.smcall.app

/**
 * Converts common Firebase Phone Authentication failures into safe, localized guidance.
 * Unknown errors return null so callers can retain a diagnostic fallback.
 */
object PhoneAuthErrorMessages {
    fun message(
        errorCode: String?,
        exceptionType: String?,
        language: String
    ): String? {
        val normalizedCode = errorCode
            ?.substringAfterLast('/')
            ?.lowercase()
            ?.removePrefix("error_")
            ?.replace('_', '-')

        val typeName = exceptionType?.substringAfterLast('.')
        val code = when {
            typeName == "FirebaseTooManyRequestsException" -> "too-many-requests"
            normalizedCode == "quota-exceeded" -> "quota-exceeded"
            else -> normalizedCode
        }

        val locale = when (language) {
            "en" -> "en"
            "ar" -> "ar"
            "hi" -> "hi"
            else -> "bn"
        }

        return when (code) {
            "billing-not-enabled" -> when (locale) {
                "en" -> "SMS verification is not enabled for this Firebase project yet. Check Phone sign-in and billing settings."
                "ar" -> "لم يتم تفعيل التحقق عبر SMS في مشروع Firebase بعد. تحقق من إعدادات تسجيل الهاتف والفوترة."
                "hi" -> "इस Firebase प्रोजेक्ट में SMS सत्यापन चालू नहीं है। Phone sign-in और billing सेटिंग जाँचें।"
                else -> "এই Firebase প্রজেক্টে SMS যাচাই চালু নেই। Phone sign-in ও Billing সেটিংস পরীক্ষা করুন।"
            }
            "too-many-requests" -> when (locale) {
                "en" -> "Too many attempts. Wait a while before requesting another code."
                "ar" -> "محاولات كثيرة جدًا. انتظر قليلًا قبل طلب رمز جديد."
                "hi" -> "बहुत अधिक प्रयास हुए हैं। नया कोड माँगने से पहले कुछ देर प्रतीक्षा करें।"
                else -> "অনেকবার চেষ্টা হয়েছে। নতুন OTP চাওয়ার আগে কিছুক্ষণ অপেক্ষা করুন।"
            }
            "quota-exceeded" -> when (locale) {
                "en" -> "The SMS quota has been reached. Try later or use a Firebase test phone number."
                "ar" -> "تم بلوغ حد رسائل SMS. حاول لاحقًا أو استخدم رقم اختبار Firebase."
                "hi" -> "SMS सीमा पूरी हो गई है। बाद में प्रयास करें या Firebase test phone number इस्तेमाल करें।"
                else -> "SMS পাঠানোর সীমা শেষ হয়েছে। পরে চেষ্টা করুন অথবা Firebase Test Phone Number ব্যবহার করুন।"
            }
            "invalid-phone-number" -> when (locale) {
                "en" -> "Firebase rejected this phone number. Include the country code, for example +8801..."
                "ar" -> "رفض Firebase رقم الهاتف. أضف رمز الدولة، مثل +8801..."
                "hi" -> "Firebase ने यह फ़ोन नंबर स्वीकार नहीं किया। देश कोड सहित नंबर लिखें, जैसे +8801..."
                else -> "Firebase এই ফোন নম্বর গ্রহণ করেনি। দেশের কোডসহ নম্বর লিখুন, যেমন +8801..."
            }
            "operation-not-allowed" -> when (locale) {
                "en" -> "Phone sign-in is disabled in Firebase Authentication. Enable the Phone provider."
                "ar" -> "تسجيل الدخول بالهاتف معطّل في Firebase Authentication. فعّل مزوّد الهاتف."
                "hi" -> "Firebase Authentication में Phone sign-in बंद है। Phone provider चालू करें।"
                else -> "Firebase Authentication-এ Phone sign-in বন্ধ আছে। Phone provider চালু করুন।"
            }
            "network-request-failed" -> when (locale) {
                "en" -> "Network error. Check your internet connection and try again."
                "ar" -> "حدث خطأ في الشبكة. تحقق من اتصال الإنترنت وحاول مرة أخرى."
                "hi" -> "नेटवर्क समस्या है। इंटरनेट कनेक्शन जाँचकर फिर प्रयास करें।"
                else -> "ইন্টারনেট সংযোগে সমস্যা হয়েছে। সংযোগ পরীক্ষা করে আবার চেষ্টা করুন।"
            }
            "captcha-check-failed", "app-not-authorized", "invalid-app-credential",
            "missing-client-identifier" -> when (locale) {
                "en" -> "App verification failed. Check the Android app registration and SHA fingerprints in Firebase."
                "ar" -> "فشل التحقق من التطبيق. تحقق من تسجيل تطبيق Android وبصمات SHA في Firebase."
                "hi" -> "ऐप सत्यापन विफल हुआ। Firebase में Android app registration और SHA fingerprint जाँचें।"
                else -> "অ্যাপ যাচাই ব্যর্থ হয়েছে। Firebase-এ Android App Registration ও SHA fingerprint পরীক্ষা করুন।"
            }
            "session-expired", "code-expired" -> when (locale) {
                "en" -> "This verification code has expired. Request a new code."
                "ar" -> "انتهت صلاحية رمز التحقق. اطلب رمزًا جديدًا."
                "hi" -> "इस सत्यापन कोड की अवधि समाप्त हो गई है। नया कोड माँगें।"
                else -> "এই OTP-এর মেয়াদ শেষ হয়েছে। নতুন OTP পাঠান।"
            }
            "invalid-verification-code" -> when (locale) {
                "en" -> "The verification code is incorrect. Check the 6 digits and try again."
                "ar" -> "رمز التحقق غير صحيح. تحقق من الأرقام الستة وحاول مرة أخرى."
                "hi" -> "सत्यापन कोड गलत है। 6 अंक जाँचकर फिर प्रयास करें।"
                else -> "OTP সঠিক নয়। ৬টি সংখ্যা পরীক্ষা করে আবার চেষ্টা করুন।"
            }
            "missing-verification-code" -> when (locale) {
                "en" -> "Enter the 6-digit verification code."
                "ar" -> "أدخل رمز التحقق المكوّن من 6 أرقام."
                "hi" -> "6 अंकों का सत्यापन कोड लिखें।"
                else -> "৬ সংখ্যার OTP লিখুন।"
            }
            else -> null
        }
    }
}
