package com.smcall.app

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * First native Firestore chat milestone.
 * Chat messages are in-app messages; they are not carrier SMS.
 */
class ChatActivity : Activity() {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid
    private var language = "bn"
    private var activeChatId: String? = null
    private var activeChatTitle = ""
    private var messagesListener: ListenerRegistration? = null
    private lateinit var page: LinearLayout
    private lateinit var status: TextView

    private data class Copy(
        val title: String, val friend: String, val start: String,
        val group: String, val memberIds: String, val create: String,
        val chats: String, val back: String, val message: String,
        val send: String, val empty: String, val noFirebase: String,
        val invalidFriend: String, val invalidGroup: String,
        val maxMembers: String, val sent: String
    )

    private fun copy() = when (language) {
        "en" -> Copy("SM CALL Chats", "Friend's User ID", "Open personal chat",
            "Group name", "Member User IDs (one per line)", "Create group",
            "Your chats", "Back", "Write a message", "Send", "No chats yet.",
            "Firebase is not configured in this APK.", "Enter a friend's User ID.",
            "Enter a group name and at least one member.", "Maximum 20 people including you.",
            "Message sent.")
        "ar" -> Copy("محادثات SM CALL", "معرّف المستخدم للصديق", "فتح محادثة شخصية",
            "اسم المجموعة", "معرّفات الأعضاء (كل معرّف في سطر)", "إنشاء مجموعة",
            "محادثاتك", "رجوع", "اكتب رسالة", "إرسال", "لا توجد محادثات بعد.",
            "Firebase غير مهيأ في هذا التطبيق.", "أدخل معرّف المستخدم للصديق.",
            "أدخل اسم المجموعة ومعرّف عضو واحد على الأقل.", "الحد الأقصى 20 شخصًا معك.",
            "تم إرسال الرسالة.")
        "hi" -> Copy("SM CALL चैट", "मित्र की User ID", "निजी चैट खोलें",
            "ग्रुप का नाम", "सदस्यों की User ID (हर पंक्ति में एक)", "ग्रुप बनाएँ",
            "आपकी चैट", "वापस", "संदेश लिखें", "भेजें", "अभी कोई चैट नहीं है।",
            "इस APK में Firebase कॉन्फ़िगर नहीं है।", "मित्र की User ID लिखें।",
            "ग्रुप नाम और कम से कम एक सदस्य लिखें।", "आप सहित अधिकतम 20 लोग।",
            "संदेश भेज दिया गया।")
        else -> Copy("SM CALL চ্যাট", "বন্ধুর User ID", "ব্যক্তিগত চ্যাট খুলুন",
            "গ্রুপের নাম", "সদস্যদের User ID (প্রতি লাইনে একটি)", "গ্রুপ তৈরি করুন",
            "আপনার চ্যাট", "ফিরে যান", "মেসেজ লিখুন", "পাঠান", "এখনো কোনো চ্যাট নেই।",
            "এই APK-তে Firebase সংযুক্ত নেই।", "বন্ধুর User ID লিখুন।",
            "গ্রুপের নাম ও অন্তত একজন সদস্যের ID লিখুন।", "আপনিসহ সর্বোচ্চ ২০ জন।",
            "মেসেজ পাঠানো হয়েছে।")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = intent.getStringExtra("language") ?: "bn"
        if (uid == null) {
            Toast.makeText(this, copy().noFirebase, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        renderChatList()
    }

    override fun onDestroy() {
        messagesListener?.remove()
        messagesListener = null
        super.onDestroy()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun makePage(): LinearLayout {
        val scroll = ScrollView(this)
        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(28))
            setBackgroundColor(Color.rgb(7, 27, 54))
        }
        scroll.addView(page, ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        setContentView(scroll)
        return page
    }

    private fun addText(text: String, size: Float = 16f): TextView {
        val view = TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.WHITE)
            setPadding(0, dp(8), 0, dp(8))
        }
        page.addView(view)
        return view
    }

    private fun addInput(hint: String, multiline: Boolean = false): EditText {
        val field = EditText(this).apply {
            this.hint = hint
            textSize = 16f
            setTextColor(Color.BLACK)
            setHintTextColor(Color.DKGRAY)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }
            if (multiline) {
                minLines = 2
                maxLines = 4
            } else setSingleLine(true)
        }
        page.addView(field, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(8)
        })
        return field
    }

    private fun addButton(label: String, action: () -> Unit, secondary: Boolean = false) {
        val button = Button(this).apply {
            text = label
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(if (secondary) Color.rgb(40, 81, 126) else Color.rgb(22, 199, 160))
                cornerRadius = dp(12).toFloat()
            }
            setOnClickListener { action() }
        }
        page.addView(button, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply { topMargin = dp(8) })
    }

    private fun setStatus(text: String) {
        if (::status.isInitialized) status.text = text
    }

    private fun renderChatList() {
        messagesListener?.remove()
        messagesListener = null
        activeChatId = null
        val c = copy()
        makePage()
        addText(c.title, 26f)
        val friendId = addInput(c.friend)
        addButton(c.start) { openPersonalChat(friendId.text.toString().trim()) }
        val groupName = addInput(c.group)
        val members = addInput(c.memberIds, true)
        addButton(c.create) { createGroup(groupName.text.toString(), members.text.toString()) }
        addText(c.chats, 20f)
        status = addText("লোড হচ্ছে…", 13f)
        addButton(c.back, { finish() }, true)
        loadChats()
    }

    private fun loadChats() {
        val currentUid = uid ?: return
        db.collection("chats").whereArrayContains("members", currentUid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!::status.isInitialized || activeChatId != null) return@addOnSuccessListener
                if (snapshot.isEmpty) {
                    setStatus(copy().empty)
                    return@addOnSuccessListener
                }
                page.removeView(status)
                snapshot.documents.forEach { doc ->
                    val members = (doc.get("members") as? List<*>)?.filterIsInstance<String>().orEmpty()
                    val title = doc.getString("name")
                        ?: members.firstOrNull { it != currentUid }
                        ?: "Personal chat"
                    val button = Button(this).apply {
                        text = title
                        isAllCaps = false
                        setOnClickListener { openRoom(doc.id, title) }
                    }
                    page.addView(button)
                }
            }
            .addOnFailureListener { setStatus("চ্যাট লোড হয়নি: ${it.localizedMessage}") }
    }

    private fun openPersonalChat(friendId: String) {
        val currentUid = uid ?: return
        if (friendId.isBlank() || friendId == currentUid) {
            setStatus(copy().invalidFriend)
            return
        }
        db.collection("users").document(friendId).get()
            .addOnSuccessListener { friend ->
                if (!friend.exists()) {
                    setStatus("এই User ID পাওয়া যায়নি।")
                    return@addOnSuccessListener
                }
                db.collection("chats").whereArrayContains("members", currentUid).get()
                    .addOnSuccessListener { chats ->
                        val existing = chats.documents.firstOrNull { doc ->
                            doc.getString("type") == "personal" &&
                                (doc.get("members") as? List<*>)?.toSet() == setOf(currentUid, friendId)
                        }
                        if (existing != null) {
                            openRoom(existing.id, friend.getString("name") ?: friendId)
                        } else {
                            db.collection("chats").add(mapOf(
                                "members" to listOf(currentUid, friendId),
                                "type" to "personal",
                                "createdAt" to FieldValue.serverTimestamp()
                            )).addOnSuccessListener { ref ->
                                openRoom(ref.id, friend.getString("name") ?: friendId)
                            }.addOnFailureListener { setStatus("চ্যাট তৈরি হয়নি: ${it.localizedMessage}") }
                        }
                    }
                    .addOnFailureListener { setStatus("চ্যাট খোঁজা যায়নি: ${it.localizedMessage}") }
            }
            .addOnFailureListener { setStatus("বন্ধুর তথ্য পাওয়া যায়নি: ${it.localizedMessage}") }
    }

    private fun createGroup(nameValue: String, rawMembers: String) {
        val currentUid = uid ?: return
        val name = nameValue.trim()
        val otherIds = rawMembers.split(Regex("[\\n,]+"))
            .map { it.trim() }.filter { it.isNotBlank() && it != currentUid }.distinct()
        if (name.isBlank() || otherIds.isEmpty()) {
            setStatus(copy().invalidGroup)
            return
        }
        if (otherIds.size + 1 > 20) {
            setStatus(copy().maxMembers)
            return
        }
        val members = otherIds + currentUid
        db.collection("users").whereIn(FieldPath.documentId(), otherIds).get()
            .addOnSuccessListener { found ->
                if (found.size() != otherIds.size) {
                    setStatus("এক বা একাধিক User ID পাওয়া যায়নি।")
                    return@addOnSuccessListener
                }
                db.collection("chats").add(mapOf(
                    "name" to name,
                    "members" to members,
                    "type" to "group",
                    "createdAt" to FieldValue.serverTimestamp()
                )).addOnSuccessListener { ref ->
                    openRoom(ref.id, name)
                }.addOnFailureListener { setStatus("গ্রুপ তৈরি হয়নি: ${it.localizedMessage}") }
            }
            .addOnFailureListener { setStatus("সদস্য যাচাই হয়নি: ${it.localizedMessage}") }
    }

    private fun openRoom(chatId: String, title: String) {
        activeChatId = chatId
        activeChatTitle = title
        renderRoom()
    }

    private fun renderRoom() {
        val c = copy()
        makePage()
        addText(activeChatTitle, 23f)
        val messageList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        page.addView(messageList)
        val input = addInput(c.message, true)
        addButton(c.send) {
            val text = input.text.toString().trim()
            val chatId = activeChatId ?: return@addButton
            val sender = uid ?: return@addButton
            if (text.isNotEmpty()) {
                db.collection("chats").document(chatId).collection("messages").add(mapOf(
                    "sender" to sender,
                    "text" to text,
                    "createdAt" to FieldValue.serverTimestamp()
                )).addOnSuccessListener { input.text.clear() }
                    .addOnFailureListener { setStatus("মেসেজ পাঠানো যায়নি: ${it.localizedMessage}") }
            }
        }
        addButton(c.back, { renderChatList() }, true)
        messagesListener?.remove()
        messagesListener = db.collection("chats").document(activeChatId!!)
            .collection("messages").orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                messageList.removeAllViews()
                if (error != null) {
                    val err = TextView(this).apply {
                        text = "মেসেজ লোড হয়নি: ${error.localizedMessage}"
                        setTextColor(Color.WHITE)
                    }
                    messageList.addView(err)
                    return@addSnapshotListener
                }
                if (snapshot == null || snapshot.isEmpty) {
                    val empty = TextView(this).apply {
                        text = "এখনো কোনো মেসেজ নেই।"
                        setTextColor(Color.WHITE)
                    }
                    messageList.addView(empty)
                } else {
                    snapshot.documents.forEach { doc ->
                        val sender = doc.getString("sender") ?: "?"
                        val body = doc.getString("text") ?: ""
                        val line = TextView(this).apply {
                            text = (if (sender == uid) "আপনি: " else "$sender: ") + body
                            textSize = 15f
                            setTextColor(Color.WHITE)
                            setPadding(dp(10), dp(8), dp(10), dp(8))
                            background = GradientDrawable().apply {
                                setColor(Color.rgb(35, 78, 119))
                                cornerRadius = dp(10).toFloat()
                            }
                        }
                        messageList.addView(line, LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply { bottomMargin = dp(6) })
                    }
                }
            }
    }

    private fun setStatusOrToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
