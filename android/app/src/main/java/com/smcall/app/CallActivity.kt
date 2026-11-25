package com.smcall.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class CallActivity : Activity() {
    companion object {
        const val EXTRA_ROOM_ID = "roomId"
        const val EXTRA_CALLER = "isCaller"
        const val EXTRA_CALL_TYPE = "callType"
        private const val PERMISSION_REQUEST = 741
    }

    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var roomId: String
    private var isCaller = false
    private var callType = "audio"
    private var factory: PeerConnectionFactory? = null
    private var peer: PeerConnection? = null
    private var egl: EglBase? = null
    private var audioSource: AudioSource? = null
    private var audioTrack: AudioTrack? = null
    private var localVideo: VideoTrack? = null
    private var videoCapturer: org.webrtc.VideoCapturer? = null
    private var surfaceHelper: SurfaceTextureHelper? = null
    private var remoteRenderer: SurfaceViewRenderer? = null
    private var localRenderer: SurfaceViewRenderer? = null
    private var roomListener: ListenerRegistration? = null
    private var candidateListener: ListenerRegistration? = null
    private var answerApplied = false
    private var remoteDescriptionSet = false
    private val pendingRemoteCandidates = mutableListOf<IceCandidate>()
    private var muted = false
    private var cameraEnabled = true
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomId = intent.getStringExtra(EXTRA_ROOM_ID) ?: run { finish(); return }
        isCaller = intent.getBooleanExtra(EXTRA_CALLER, false)
        callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "audio"
        if (!hasPermissions()) {
            val permissions = if (callType == "video") arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                else arrayOf(Manifest.permission.RECORD_AUDIO)
            requestPermissions(permissions, PERMISSION_REQUEST)
        } else startCallEngine()
    }

    private fun hasPermissions(): Boolean =
        checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            (callType != "video" || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST && hasPermissions()) startCallEngine()
        else { Toast.makeText(this, "কলের জন্য মাইক্রোফোন/ক্যামেরা অনুমতি প্রয়োজন", Toast.LENGTH_LONG).show(); finish() }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 28, 18, 18)
            setBackgroundColor(android.graphics.Color.rgb(7, 27, 54))
        }
        status = TextView(this).apply {
            text = "কল সংযোগ হচ্ছে..."
            textSize = 18f
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(status, LinearLayout.LayoutParams(-1, 56))
        if (callType == "video") {
            remoteRenderer = SurfaceViewRenderer(this)
            localRenderer = SurfaceViewRenderer(this)
            root.addView(remoteRenderer, LinearLayout.LayoutParams(-1, 0, 1f))
            root.addView(localRenderer, LinearLayout.LayoutParams(-1, 0, 0.35f))
        }
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        fun control(label: String, action: () -> Unit) {
            controls.addView(Button(this).apply { text = label; isAllCaps = false; setOnClickListener { action() } },
                LinearLayout.LayoutParams(0, 58, 1f))
        }
        control("মাইক") { muted = !muted; audioTrack?.setEnabled(!muted) }
        if (callType == "video") control("ক্যামেরা") { cameraEnabled = !cameraEnabled; localVideo?.setEnabled(cameraEnabled) }
        control("কল শেষ") { endCall() }
        root.addView(controls)
        setContentView(root)
    }

    private fun startCallEngine() {
        buildUi()
        try {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions()
            )
            egl = EglBase.create()
            val encoder = DefaultVideoEncoderFactory(egl!!.eglBaseContext, true, true)
            val decoder = DefaultVideoDecoderFactory(egl!!.eglBaseContext)
            factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoder)
                .setVideoDecoderFactory(decoder)
                .createPeerConnectionFactory()
            if (callType == "video") {
                remoteRenderer?.init(egl!!.eglBaseContext, null)
                localRenderer?.init(egl!!.eglBaseContext, null)
                remoteRenderer?.setZOrderMediaOverlay(false)
                localRenderer?.setZOrderMediaOverlay(true)
            }
            createPeer()
            listenToRoom()
            listenToRemoteCandidates()
            if (isCaller) createOffer()
        } catch (error: Exception) {
            status.text = "WebRTC চালু হয়নি: ${error.localizedMessage}"
        }
    }

    private fun createPeer() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )
        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        peer = factory?.createPeerConnection(config, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                runOnUiThread {
                    status.text = when (state) {
                        PeerConnection.IceConnectionState.CONNECTED, PeerConnection.IceConnectionState.COMPLETED -> "কল সংযুক্ত হয়েছে"
                        PeerConnection.IceConnectionState.FAILED -> "নেটওয়ার্ক সংযোগ ব্যর্থ — TURN server প্রয়োজন হতে পারে"
                        PeerConnection.IceConnectionState.DISCONNECTED -> "সংযোগ বিচ্ছিন্ন হচ্ছে..."
                        else -> "কল সংযোগ হচ্ছে..."
                    }
                }
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate ?: return
                val collection = if (isCaller) "callerCandidates" else "calleeCandidates"
                db.collection("callRooms").document(roomId).collection(collection).add(
                    hashMapOf("sdpMid" to candidate.sdpMid, "sdpMLineIndex" to candidate.sdpMLineIndex, "candidate" to candidate.sdp)
                )
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: org.webrtc.MediaStream?) {}
            override fun onRemoveStream(stream: org.webrtc.MediaStream?) {}
            override fun onDataChannel(channel: org.webrtc.DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out org.webrtc.MediaStream>?) {
                val track = receiver?.track() as? VideoTrack
                if (track != null) runOnUiThread { remoteRenderer?.let { track.addSink(it) } }
            }
            override fun onTrack(transceiver: org.webrtc.RtpTransceiver?) {
                val track = transceiver?.receiver?.track() as? VideoTrack
                if (track != null) runOnUiThread { track.addSink(remoteRenderer) }
            }
        })
        val audioConstraints = MediaConstraints()
        audioSource = factory?.createAudioSource(audioConstraints)
        audioTrack = factory?.createAudioTrack("smcall-audio", audioSource)
        audioTrack?.let { peer?.addTrack(it, listOf("smcall-stream")) }
        if (callType == "video") startCamera()
    }

    private fun startCamera() {
        val enumerator = Camera2Enumerator(this)
        val camera = enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) }
            ?: enumerator.deviceNames.firstOrNull() ?: return
        videoCapturer = enumerator.createCapturer(camera, null) ?: return
        surfaceHelper = SurfaceTextureHelper.create("SMCallCapture", egl!!.eglBaseContext)
        val source = factory?.createVideoSource(false) ?: return
        videoCapturer?.initialize(surfaceHelper, this, source.capturerObserver)
        videoCapturer?.startCapture(640, 480, 24)
        localVideo = factory?.createVideoTrack("smcall-video", source)
        localRenderer?.let { localVideo?.addSink(it) }
        localVideo?.let { peer?.addTrack(it, listOf("smcall-stream")) }
    }

    private fun createOffer() {
        val constraints = MediaConstraints()
        peer?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                if (desc == null) return
                peer?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        db.collection("callRooms").document(roomId).update(
                            "offer", mapOf("type" to desc.type.canonicalForm(), "sdp" to desc.description),
                            "updatedAt", FieldValue.serverTimestamp()
                        )
                    }
                    override fun onSetFailure(error: String?) { showError(error) }
                    override fun onCreateSuccess(value: SessionDescription?) {}
                    override fun onCreateFailure(error: String?) { showError(error) }
                }, desc)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) { showError(error) }
            override fun onSetFailure(error: String?) { showError(error) }
        }, constraints)
    }

    private fun listenToRoom() {
        roomListener = db.collection("callRooms").document(roomId).addSnapshotListener(this) { snap, error ->
            if (error != null || snap == null || !snap.exists()) { if (error != null) showError(error.localizedMessage); return@addSnapshotListener }
            if (snap.getString("status") == "ended") { runOnUiThread { status.text = "কল শেষ হয়েছে"; finish() }; return@addSnapshotListener }
            if (isCaller) {
                val answer = snap.get("answer") as? Map<*, *>
                if (answer != null && !answerApplied) {
                    val sdp = answer["sdp"] as? String ?: return@addSnapshotListener
                    answerApplied = true
                    val type = SessionDescription.Type.fromCanonicalForm(answer["type"] as? String ?: "answer")
                    peer?.setRemoteDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            remoteDescriptionSet = true
                            flushRemoteCandidates()
                        }
                        override fun onSetFailure(error: String?) { showError(error) }
                        override fun onCreateSuccess(desc: SessionDescription?) {}
                        override fun onCreateFailure(error: String?) {}
                    }, SessionDescription(type, sdp))
                }
            } else {
                val offer = snap.get("offer") as? Map<*, *>
                if (offer != null && !remoteDescriptionSet) {
                    val sdp = offer["sdp"] as? String ?: return@addSnapshotListener
                    val type = SessionDescription.Type.fromCanonicalForm(offer["type"] as? String ?: "offer")
                    peer?.setRemoteDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            remoteDescriptionSet = true
                            flushRemoteCandidates()
                            createAnswer()
                        }
                        override fun onSetFailure(error: String?) { showError(error) }
                        override fun onCreateSuccess(desc: SessionDescription?) {}
                        override fun onCreateFailure(error: String?) {}
                    }, SessionDescription(type, sdp))
                }
            }
        }
    }

    private fun createAnswer() {
        peer?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                if (desc == null) return
                peer?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        db.collection("callRooms").document(roomId).update(
                            "answer", mapOf("type" to desc.type.canonicalForm(), "sdp" to desc.description),
                            "updatedAt", FieldValue.serverTimestamp()
                        )
                    }
                    override fun onSetFailure(error: String?) { showError(error) }
                    override fun onCreateSuccess(value: SessionDescription?) {}
                    override fun onCreateFailure(error: String?) { showError(error) }
                }, desc)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) { showError(error) }
            override fun onSetFailure(error: String?) { showError(error) }
        }, MediaConstraints())
    }

    private fun simpleObserver() = object : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String?) { showError(error) }
        override fun onSetFailure(error: String?) { showError(error) }
    }

    private fun listenToRemoteCandidates() {
        val collection = if (isCaller) "calleeCandidates" else "callerCandidates"
        candidateListener = db.collection("callRooms").document(roomId).collection(collection)
            .addSnapshotListener(this) { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                snapshot.documentChanges.forEach { change ->
                    val doc = change.document
                    val mid = doc.getString("sdpMid") ?: return@forEach
                    val index = (doc.getLong("sdpMLineIndex") ?: 0L).toInt()
                    val candidate = doc.getString("candidate") ?: return@forEach
                    val ice = IceCandidate(mid, index, candidate)
                    if (remoteDescriptionSet) peer?.addIceCandidate(ice) else pendingRemoteCandidates.add(ice)
                }
            }
    }

    private fun flushRemoteCandidates() {
        pendingRemoteCandidates.forEach { peer?.addIceCandidate(it) }
        pendingRemoteCandidates.clear()
    }

    private fun showError(message: String?) {
        runOnUiThread { status.text = "কল ত্রুটি: ${message ?: "অজানা সমস্যা"}" }
    }

    private fun endCall() {
        db.collection("callRooms").document(roomId).update(
            "status", "ended", "updatedAt", FieldValue.serverTimestamp()
        ).addOnCompleteListener { finish() }
    }

    override fun onDestroy() {
        roomListener?.remove()
        candidateListener?.remove()
        try { videoCapturer?.stopCapture() } catch (_: Exception) {}
        videoCapturer?.dispose()
        localVideo?.dispose()
        audioTrack?.dispose()
        audioSource?.dispose()
        peer?.close()
        peer?.dispose()
        factory?.dispose()
        surfaceHelper?.dispose()
        remoteRenderer?.release()
        localRenderer?.release()
        egl?.release()
        super.onDestroy()
    }
}
