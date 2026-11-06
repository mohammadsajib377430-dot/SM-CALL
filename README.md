
<!DOCTYPE html>
<html lang="bn">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="theme-color" content="#102b55">
<title>SM CALL</title>
<style>
*{box-sizing:border-box}
body{margin:0;font-family:Arial,sans-serif;background:#07172f;color:white}
.app{max-width:480px;min-height:100vh;margin:auto;padding:18px;background:linear-gradient(180deg,#102b55,#07172f)}
header{display:flex;justify-content:space-between;align-items:center;padding:12px 0 24px}
.logo{font-size:27px;font-weight:bold}.logo span{color:#20d6c2}
button,input,textarea{font:inherit}
button{border:0;cursor:pointer}
.language{background:#1c416e;color:white;padding:10px;border-radius:10px}
.screen{display:none}.screen.active{display:block}
.card{background:#17365d;padding:20px;border-radius:18px;margin:14px 0}
h1,h2{margin-top:0}
p{color:#c5d6e9;line-height:1.5}
.input{width:100%;padding:14px;margin:8px 0;border:1px solid #49678a;border-radius:12px;background:#0b2443;color:white}
.primary,.secondary{width:100%;padding:14px;margin-top:10px;border-radius:12px}
.primary{background:#16c7a0;color:#062b32;font-weight:bold}
.secondary{background:#28517e;color:white}
.link{color:#20d6c2;text-align:center;margin-top:16px;cursor:pointer}
.status{font-size:13px;color:#20d6c2;overflow-wrap:anywhere}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:18px}
.feature{background:#17365d;border:1px solid #31557e;border-radius:16px;padding:20px 8px;text-align:center}
.feature .emoji{font-size:30px;margin-bottom:8px}
.feature h3{font-size:15px;margin:0}
.nav{display:flex;justify-content:space-around;background:#102b50;padding:14px;border-radius:15px;margin-top:22px}
.nav button{background:none;color:white}
.nav span{display:block;font-size:20px;margin-bottom:5px}
.message-list{min-height:160px;max-height:340px;overflow:auto;background:#0b2443;border-radius:12px;padding:10px;margin:12px 0}
.message{background:#24517a;padding:10px;border-radius:11px;margin:8px 0;overflow-wrap:anywhere}
.message small{display:block;color:#a9c5df;margin-bottom:5px}
.avatar{width:78px;height:78px;border-radius:50%;background:#20d6c2;color:#07334a;display:grid;place-items:center;font-size:32px;font-weight:bold;margin:0 auto 14px}
.profile{text-align:center;padding:24px}
.footer{text-align:center;color:#819bb8;font-size:12px;padding:24px 0}
video{width:100%;background:#000;border-radius:12px;margin:8px 0}
</style>
</head>
<body>
<div class="app">
<header>
<div class="logo">SM <span>CALL</span></div>
<button class="language" onclick="changeLanguage()">বাংলা / EN</button>
</header>

<section id="loginScreen" class="screen active">
<div class="card">
<h1 id="welcome">স্বাগতম SM CALL-এ</h1>
<p id="loginHelp">আপনার ফোন নম্বর দিয়ে লগইন করুন।</p>
<input id="loginPhone" class="input" type="tel" placeholder="+8801XXXXXXXXX">
<div id="loginCaptcha"></div>
<button class="primary" onclick="sendOTP('login')" id="loginBtn">SMS OTP পাঠান</button>
<p class="link" onclick="showScreen('signupScreen')" id="signupLink">নতুন অ্যাকাউন্ট তৈরি করুন</p>
<p id="loginStatus" class="status"></p>
</div>
</section>

<section id="signupScreen" class="screen">
<div class="card">
<h2>নতুন অ্যাকাউন্ট তৈরি করুন</h2>
<input id="signupName" class="input" placeholder="আপনার নাম">
<input id="signupPhone" class="input" type="tel" placeholder="+8801XXXXXXXXX">
<div id="signupCaptcha"></div>
<button class="primary" onclick="sendOTP('signup')">OTP পাঠান</button>
<p id="signupStatus" class="status"></p>
<p class="link" onclick="showScreen('loginScreen')">লগইন পেজে ফিরে যান</p>
</div>
</section>

<section id="otpScreen" class="screen">
<div class="card">
<h2>OTP যাচাই করুন</h2>
<p>আপনার ফোনে আসা ৬ সংখ্যার কোড লিখুন।</p>
<input id="otpCode" class="input" inputmode="numeric" maxlength="6" placeholder="৬ সংখ্যার OTP">
<button class="primary" onclick="verifyOTP()">যাচাই করুন</button>
<p id="otpStatus" class="status"></p>
</div>
</section>

<section id="homeScreen" class="screen">
<div class="card">
<h1>SM CALL</h1>
<p>আপনার যোগাযোগের নতুন ঠিকানা।</p>
<p id="greeting" class="status"></p>
</div>
<div class="grid">
<div class="feature" onclick="startCall('audio')"><div class="emoji">📞</div><h3>Audio Call</h3></div>
<div class="feature" onclick="startCall('video')"><div class="emoji">📹</div><h3>Video Call</h3></div>
<div class="feature" onclick="showScreen('chatsScreen')"><div class="emoji">💬</div><h3>Messages</h3></div>
<div class="feature" onclick="showScreen('groupScreen')"><div class="emoji">👥</div><h3>Group Chat</h3></div>
</div>
<div class="nav">
<button onclick="showScreen('homeScreen')"><span>🏠</span>Home</button>
<button onclick="showScreen('chatsScreen')"><span>💬</span>Chats</button>
<button onclick="showScreen('profileScreen')"><span>👤</span>Profile</button>
</div>
</section>

<section id="chatsScreen" class="screen">
<div class="card">
<h2>ব্যক্তিগত চ্যাট</h2>
<p>বন্ধুর Firebase User ID লিখুন।</p>
<input id="friendUID" class="input" placeholder="Friend User ID">
<button class="primary" onclick="startPersonalChat()">চ্যাট শুরু করুন</button>
<div id="chatList" class="message-list"></div>
</div>
<button class="secondary" onclick="showScreen('homeScreen')">হোমে ফিরে যান</button>
</section>

<section id="groupScreen" class="screen">
<div class="card">
<h2>নতুন গ্রুপ তৈরি করুন</h2>
<input id="groupName" class="input" placeholder="গ্রুপের নাম">
<textarea id="groupMembers" class="input" rows="4" placeholder="সদস্যদের User ID, প্রতিটি নতুন লাইনে"></textarea>
<p>সর্বোচ্চ ৫০ জন সদস্য, আপনিসহ।</p>
<button class="primary" onclick="createGroup()">গ্রুপ তৈরি করুন</button>
<p id="groupStatus" class="status"></p>
</div>
<button class="secondary" onclick="showScreen('homeScreen')">হোমে ফিরে যান</button>
</section>

<section id="chatRoomScreen" class="screen">
<div class="card">
<h2 id="roomTitle">চ্যাট</h2>
<div id="messages" class="message-list"></div>
<textarea id="messageInput" class="input" rows="2" placeholder="মেসেজ লিখুন..."></textarea>
<button class="primary" onclick="sendMessage()">মেসেজ পাঠান</button>
</div>
<button class="secondary" onclick="showScreen('chatsScreen')">চ্যাট তালিকায় ফিরে যান</button>
</section>

<section id="profileScreen" class="screen">
<div class="card profile">
<div class="avatar">SM</div>
<h2 id="profileName">ব্যবহারকারী</h2>
<p id="profilePhone"></p>
<p id="profileUID" class="status"></p>
</div>
<button class="secondary" onclick="logout()">Logout</button>
<button class="secondary" onclick="showScreen('homeScreen')">হোমে ফিরে যান</button>
</section>

<section id="callScreen" class="screen">
<div class="card">
<h2 id="callTitle">Call</h2>
<p id="callStatus" class="status"></p>
<video id="remoteVideo" autoplay playsinline></video>
<video id="localVideo" autoplay muted playsinline></video>
<button class="secondary" onclick="endCall()">কল শেষ করুন</button>
</div>
</section>

<div class="footer">SM CALL © 2026 | Stay Connected</div>
</div>

<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-app-compat.js"></script>
<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-auth-compat.js"></script>
<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-firestore-compat.js"></script>
<script>
const firebaseConfig={
 apiKey:"PASTE_FIREBASE_WEB_API_KEY_HERE",
 authDomain:"sm-call-bf23e.firebaseapp.com",
 projectId:"sm-call-bf23e",
 storageBucket:"sm-call-bf23e.firebasestorage.app",
 messagingSenderId:"844518596408",
 appId:"1:844518596408:web:1437d01d2ae914cd5d2bcd"
};
firebase.initializeApp(firebaseConfig);
const auth=firebase.auth();
const db=firebase.firestore();
let currentUser=null,confirmationResult=null,recaptchaVerifier=null;
let signupMode=false,currentChatId=null,unsubscribeMessages=null;
let pc=null,localStream=null,activeCallRef=null,callUnsubscribers=[];
let language="bn";

function showScreen(id){
 document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
 document.getElementById(id).classList.add("active");
 if(id==="chatsScreen")loadChatList();
}
function changeLanguage(){
 language=language==="bn"?"en":"bn";
 document.getElementById("welcome").textContent=language==="bn"?"স্বাগতম SM CALL-এ":"Welcome to SM CALL";
 document.getElementById("loginHelp").textContent=language==="bn"?"আপনার ফোন নম্বর দিয়ে লগইন করুন।":"Login with your phone number.";
 document.getElementById("loginBtn").textContent=language==="bn"?"SMS OTP পাঠান":"Send SMS OTP";
 document.getElementById("signupLink").textContent=language==="bn"?"নতুন অ্যাকাউন্ট তৈরি করুন":"Create new account";
}
function normalizePhone(v){
 let p=v.trim().replace(/[\s()-]/g,"");
 if(/^01\d{9}$/.test(p))return "+88"+p;
 if(/^8801\d{9}$/.test(p))return "+"+p;
 return p;
}
async function setupCaptcha(id){
 if(recaptchaVerifier){try{recaptchaVerifier.clear()}catch(e){}}
 recaptchaVerifier=new firebase.auth.RecaptchaVerifier(id,{size:"normal"});
 await recaptchaVerifier.render();
}
async function sendOTP(mode){
 const phone=normalizePhone(document.getElementById(mode==="signup"?"signupPhone":"loginPhone").value);
 const status=document.getElementById(mode==="signup"?"signupStatus":"loginStatus");
 if(!/^\+[1-9]\d{7,14}$/.test(phone)){status.textContent="সঠিক আন্তর্জাতিক ফোন নম্বর লিখুন।";return}
 if(mode==="signup"&&!document.getElementById("signupName").value.trim()){status.textContent="আপনার নাম লিখুন।";return}
 status.textContent="reCAPTCHA প্রস্তুত হচ্ছে...";
 try{
  await setupCaptcha(mode==="signup"?"signupCaptcha":"loginCaptcha");
  confirmationResult=await auth.signInWithPhoneNumber(phone,recaptchaVerifier);
  signupMode=mode==="signup";
  document.getElementById("otpStatus").textContent="আপনার ফোনে আসা কোড লিখুন।";
  showScreen("otpScreen");
 }catch(e){status.textContent="OTP error: "+e.message}
}
async function verifyOTP(){
 const code=document.getElementById("otpCode").value.trim();
 if(!confirmationResult){document.getElementById("otpStatus").textContent="আগে OTP পাঠান।";return}
 if(!/^\d{6}$/.test(code)){document.getElementById("otpStatus").textContent="৬ সংখ্যার OTP লিখুন।";return}
 try{
  const result=await confirmationResult.confirm(code);
  currentUser=result.user;
  await saveProfile();
  await showHome();
 }catch(e){document.getElementById("otpStatus").textContent="OTP যাচাই হয়নি: "+e.message}
}
async function saveProfile(){
 if(!currentUser)return;
 const ref=db.collection("users").doc(currentUser.uid);
 const old=await ref.get();
 let name=old.exists?(old.data().name||"SM CALL User"):"SM CALL User";
 if(signupMode)name=document.getElementById("signupName").value.trim();
 await ref.set({uid:currentUser.uid,name,phone:currentUser.phoneNumber||"",updatedAt:firebase.firestore.FieldValue.serverTimestamp()},{merge:true});
 signupMode=false;
}
async function showHome(){
 const snap=await db.collection("users").doc(currentUser.uid).get();
 const d=snap.exists?snap.data():{};
 document.getElementById("greeting").textContent="স্বাগতম, "+(d.name||"বন্ধু");
 document.getElementById("profileName").textContent=d.name||"SM CALL User";
 document.getElementById("profilePhone").textContent=d.phone||"";
 document.getElementById("profileUID").textContent="User ID: "+currentUser.uid;
 showScreen("homeScreen");
}
async function startPersonalChat(){
 const uid=document.getElementById("friendUID").value.trim();
 if(!uid||uid===currentUser.uid){alert("সঠিক User ID লিখুন।");return}
 try{
  const friend=await db.collection("users").doc(uid).get();
  if(!friend.exists){alert("User ID পাওয়া যায়নি।");return}
  const members=[currentUser.uid,uid].sort();
  const q=await db.collection("chats").where("members","==",members).get();
  let id;
  if(!q.empty)id=q.docs[0].id;
  else id=(await db.collection("chats").add({members,type:"personal",createdAt:firebase.firestore.FieldValue.serverTimestamp()})).id;
  openChat(id,friend.data().name||"বন্ধু");
 }catch(e){alert("Chat error: "+e.message)}
}
async function createGroup(){
 const name=document.getElementById("groupName").value.trim();
 let ids=[...new Set(document.getElementById("groupMembers").value.split(/[\n,]+/).map(x=>x.trim()).filter(Boolean))];
 ids=ids.filter(x=>x!==currentUser.uid);
 if(!name||!ids.length){alert("গ্রুপের নাম ও সদস্যদের UID দিন।");return}
 if(ids.length+1>50){alert("সর্বোচ্চ ৫০ জন সদস্য রাখা যাবে।");return}
 try{
  const docs=await Promise.all(ids.map(id=>db.collection("users").doc(id).get()));
  if(docs.some(d=>!d.exists)){alert("এক বা একাধিক User ID পাওয়া যায়নি।");return}
  const ref=await db.collection("chats").add({name,members:[...new Set([...ids,currentUser.uid])],type:"group",createdAt:firebase.firestore.FieldValue.serverTimestamp()});
  document.getElementById("groupStatus").textContent="গ্রুপ তৈরি হয়েছে।";
  openChat(ref.id,name);
 }catch(e){document.getElementById("groupStatus").textContent=e.message}
}
function openChat(id,title){
 currentChatId=id;document.getElementById("roomTitle").textContent=title;
 showScreen("chatRoomScreen");loadMessages();
}
function loadMessages(){
 if(unsubscribeMessages)unsubscribeMessages();
 const box=document.getElementById("messages");
 unsubscribeMessages=db.collection("chats").doc(currentChatId).collection("messages").orderBy("createdAt").onSnapshot(s=>{
  box.innerHTML="";
  s.forEach(doc=>{
   const d=doc.data(),item=document.createElement("div"),small=document.createElement("small"),body=document.createElement("div");
   item.className="message";small.textContent=d.sender===currentUser.uid?"আপনি":(d.senderName||"বন্ধু");body.textContent=d.text||"";
   item.append(small,body);box.appendChild(item);
  });
  box.scrollTop=box.scrollHeight;
 },e=>box.textContent="Message error: "+e.message);
}
async function sendMessage(){
 const input=document.getElementById("messageInput"),text=input.value.trim();
 if(!text||!currentChatId)return;
 try{
  const me=await db.collection("users").doc(currentUser.uid).get();
  await db.collection("chats").doc(currentChatId).collection("messages").add({
   text,sender:currentUser.uid,senderName:me.exists?me.data().name:"Member",
   createdAt:firebase.firestore.FieldValue.serverTimestamp()
  });
  input.value="";
 }catch(e){alert("মেসেজ পাঠানো যায়নি: "+e.message)}
}
async function loadChatList(){
 const list=document.getElementById("chatList");list.textContent="লোড হচ্ছে...";
 try{
  const s=await db.collection("chats").where("members","array-contains",currentUser.uid).get();
  list.innerHTML="";
  if(s.empty){list.textContent="এখনো কোনো চ্যাট নেই।";return}
  s.forEach(doc=>{
   const d=doc.data(),b=document.createElement("button");b.className="secondary";
   b.textContent=d.name||(d.type==="group"?"Group chat":"Personal chat");
   b.onclick=()=>openChat(doc.id,b.textContent);list.appendChild(b);
  });
 }catch(e){list.textContent="Chat list error: "+e.message}
}
</script>
</body>
</html>
