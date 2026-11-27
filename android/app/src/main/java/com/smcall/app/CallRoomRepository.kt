package com.smcall.app

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Firestore room membership for one-to-one and small mesh calls.
 * This manages room state only; it does not itself transmit audio/video.
 */
class CallRoomRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        const val MAX_PARTICIPANTS = 5
        const val TYPE_AUDIO = "audio"
        const val TYPE_VIDEO = "video"
        private const val COLLECTION = "callRooms"
    }

    fun createRoom(type: String, targetUid: String): Task<DocumentReference> {
        require(type == TYPE_AUDIO || type == TYPE_VIDEO) { "Call type must be audio or video." }
        val user = requireVerifiedUser()
        require(targetUid.isNotBlank() && targetUid != user.uid) {
            "A different invitee UID is required."
        }
        val room = hashMapOf<String, Any>(
            "hostUid" to user.uid,
            "targetUid" to targetUid,
            "members" to listOf(user.uid),
            "callType" to type,
            "status" to "ringing",
            "maxParticipants" to MAX_PARTICIPANTS,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return db.collection(COLLECTION).add(room)
    }

    fun joinRoom(roomId: String): Task<Void> {
        val user = requireVerifiedUser()
        val ref = db.collection(COLLECTION).document(roomId)
        return db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            check(snapshot.exists()) { "Call room does not exist." }
            val status = snapshot.getString("status")
            check(status == "ringing" || status == "active") { "This call has already ended." }
            val members = (snapshot.get("members") as? List<*>)?.filterIsInstance<String>()
                ?: error("Invalid call room membership.")
            if (user.uid !in members) {
                check(members.size < MAX_PARTICIPANTS) { "This group call is full (maximum 5 people)." }
                transaction.update(ref, "members", FieldValue.arrayUnion(user.uid))
                transaction.update(ref, "status", "active")
                transaction.update(ref, "updatedAt", FieldValue.serverTimestamp())
            }
            null
        }
    }

    fun leaveRoom(roomId: String): Task<Void> {
        val user = requireVerifiedUser()
        val ref = db.collection(COLLECTION).document(roomId)
        return db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            if (!snapshot.exists()) return@runTransaction null
            val members = (snapshot.get("members") as? List<*>)?.filterIsInstance<String>()
                ?: emptyList()
            if (user.uid in members) {
                val remaining = members.filterNot { it == user.uid }
                if (remaining.isEmpty()) {
                    transaction.update(ref, mapOf(
                        "members" to emptyList<String>(),
                        "status" to "ended",
                        "endedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ))
                } else {
                    transaction.update(ref, mapOf(
                        "members" to remaining,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ))
                }
            }
            null
        }
    }

    fun listen(roomId: String, onChange: (DocumentSnapshot?, Exception?) -> Unit): ListenerRegistration {
        requireVerifiedUser()
        return db.collection(COLLECTION).document(roomId).addSnapshotListener { snapshot, error ->
            onChange(snapshot, error)
        }
    }

    private fun requireVerifiedUser() = auth.currentUser?.also {
        check(VerifiedAccountPolicy.isVerified(it.isEmailVerified, it.phoneNumber)) {
            "Verify your email or phone number before using calls."
        }
    } ?: error("Sign in before starting a call.")
}
