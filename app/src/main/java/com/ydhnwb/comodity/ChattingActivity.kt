package com.ydhnwb.comodity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.ydhnwb.comodity.Adapter.MessageListAdapter
import com.ydhnwb.comodity.Model.*
import com.ydhnwb.comodity.Utilities.Constant
import kotlinx.android.synthetic.main.activity_chatting.*
import kotlinx.android.synthetic.main.bottom_bar_chat.*
import kotlinx.android.synthetic.main.filler_chat.*

class ChattingActivity : AppCompatActivity(){

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private var mUserModel : FirebaseUser? = null
    private lateinit var targetUser : UserModel
    private lateinit var listOfChat : MutableList<ChatModelHelper>
    private lateinit var messageAdapter : MessageListAdapter
    private lateinit var mStorageTask : StorageTask<UploadTask.TaskSnapshot>
    private lateinit var mFilePath : Uri
    private lateinit var mFileName : String
    private lateinit var messageListener : ChildEventListener
    private val childDatabaseRefence = FirebaseDatabase.getInstance().getReference(Constant.CHAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)
        changeStatusbarBackground()
        setLightStatusbar()
        setSupportActionBar(toolbar)
        listOfChat = ArrayList()
        val animator = messageListChat.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        val mLayoutManager = LinearLayoutManager(this@ChattingActivity)
        messageListChat.layoutManager = mLayoutManager
        toolbar.setNavigationIcon(R.drawable.ic_action_back_colored)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        getUserTarget()
        initAuth()
        sendChat()
        addPhotos()
    }


    private fun getUserTarget(){
        val cDatabaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS)
        cDatabaseReference.child(getKeyPost()).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if((p0 != null)  && p0.exists()){
                    targetUser = p0.getValue(UserModel::class.java)!!
                    toolbar.title = " "
                    dengan_siapa.text = targetUser.display_name
                    Glide.with(applicationContext).load(targetUser.url_photo).into(dengan_profile)
                }
            }
        })
    }

    private fun initAuth(){
        mAuth = FirebaseAuth.getInstance()
        mUserModel = mAuth.currentUser
        if(mUserModel != null){
            messageAdapter = MessageListAdapter(this@ChattingActivity, listOfChat, mUserModel?.uid.toString(), getKeyPost())
            messageListChat.adapter = messageAdapter
            onAddedMessage()
        }
        /*mAuthStateListener = FirebaseAuth.AuthStateListener {
            val u = it.currentUser
            if(u == null){ finish() }else{
                mUserModel = UserModel(u.uid,u.displayName.toString(),u.displayName.toString().toLowerCase(),u.email.toString(),u.photoUrl.toString())
                messageAdapter = MessageListAdapter(this@ChattingActivity, listOfChat, mUserModel.uid, getKeyPost())
                messageListChat.adapter = messageAdapter
                onAddedMessage()
            }
        }
        mAuth.addAuthStateListener(mAuthStateListener)*/
    }

    private fun getKeyPost() : String{
        return intent.getStringExtra("KEYPOST") ?: throw IllegalArgumentException("Keypost is null")
    }


    private fun sendChat(){
        send_chat.setOnClickListener {
            val teks : String = text_chat.text.toString().trim()
            if(!teks.isEmpty()){
                val refChat = FirebaseDatabase.getInstance().getReference(Constant.CHAT).child(mUserModel?.uid).child(getKeyPost())
                val chatModel = ChatModel(teks,mUserModel?.uid.toString(),ServerValue.TIMESTAMP)
                val listOfChat = ListOfChatModelUploader(getKeyPost(), ServerValue.TIMESTAMP)
                refChat.addListenerForSingleValueEvent(object : ValueEventListener{
                    var isProgress = true
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            if(isProgress){
                                if(p0.hasChild("chat")){
                                    refChat.child("date").setValue(ServerValue.TIMESTAMP)
                                    refChat.child("chat").push().setValue(chatModel)
                                    isProgress = false
                                }else{
                                    refChat.setValue(listOfChat)
                                    refChat.child("chat").push().setValue(chatModel)
                                    isProgress = false
                                }
                            }
                        }else{
                            if(isProgress){
                                refChat.setValue(listOfChat)
                                refChat.child("chat").push().setValue(chatModel)
                                isProgress = false
                            }
                        }
                    }
                })

                val oRefChat = FirebaseDatabase.getInstance().getReference(Constant.CHAT).child(getKeyPost()).child(mUserModel?.uid)
                val kList = ListOfChatModelUploader(mUserModel?.uid.toString(), ServerValue.TIMESTAMP)
                oRefChat.addListenerForSingleValueEvent(object : ValueEventListener{
                    var isProgress = true
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            if(isProgress){
                                if(p0.hasChild("chat")){
                                    oRefChat.child("date").setValue(ServerValue.TIMESTAMP)
                                    oRefChat.child("chat").push().setValue(chatModel)
                                    isProgress = false
                                }else{
                                    oRefChat.setValue(kList)
                                    oRefChat.child("chat").push().setValue(chatModel)
                                    isProgress = false
                                }
                            }
                        }else{
                            if(isProgress){
                                oRefChat.setValue(kList)
                                oRefChat.child("chat").push().setValue(chatModel)
                                isProgress = false
                            }
                        }
                    }
                })
                text_chat.text.clear()
            }
        }
    }

    private fun onAddedMessage(){
        messageListener = object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}
            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                if(p0 != null){
                    val mBody = p0.getValue(ChatModelHelper::class.java)
                    if(mBody != null){
                        listOfChat.add(mBody)
                        messageAdapter.notifyDataSetChanged()
                        messageListChat.scrollToPosition(listOfChat.size - 1)
                    }
                }
            }
            override fun onChildRemoved(p0: DataSnapshot?) {}
        }
        childDatabaseRefence.child(mUserModel?.uid).child(getKeyPost()).child("chat").addChildEventListener(messageListener)
    }

    private fun addPhotos() {
        attach_chat.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select picture"), Constant.REQUEST_CODE_IMAGE)
        }
    }

    private fun getFileName(uri : Uri) : String? {
        var res : String? = null
        if(uri.scheme.equals("content")){
            val cursor = contentResolver.query(uri,null,null,null,null)
            try {
                if(cursor != null && cursor.moveToFirst()){
                    res = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }catch (e : Exception){
                println("getFileName : ${e.message}")
            }finally {
                cursor.close()
            }
        }
        if(res == null){
            res = uri.path
            val i : Int = res.lastIndexOf("/ch")
            if(i != -1){
                res = res.substring(i+1)
            }
        }
        return res
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Constant.REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK){
            if(data != null){
                try {
                    when {
                        data.data != null -> {
                            mFilePath = data.data
                            mFileName = getFileName(data.data).toString()
                        }
                        else -> println("Not selected image")
                    }
                }catch (e : Exception){
                    println("onActivity Result : ${e.message}")
                }
            }
        }
    }
    private fun changeStatusbarBackground(){
        if(Build.VERSION.SDK_INT >= 21){
            val w = window
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            w.statusBarColor = this.getDarkColor(Color.WHITE,1.0)
        }
    }

    private fun setLightStatusbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val decorView = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
    private fun getDarkColor(i : Int, value : Double) : Int{
        val r = Color.red(i)
        val g = Color.green(i)
        val b = Color.blue(i)
        return Color.rgb((r*value).toInt(), (g*value).toInt(), (b*value).toInt())
    }

    override fun onStart() {
        changeStatusbarBackground()
        setLightStatusbar()
        super.onStart()
    }

    override fun onDestroy() {
        childDatabaseRefence.removeEventListener(messageListener)
        super.onDestroy()
    }
}
