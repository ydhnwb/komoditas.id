package com.ydhnwb.comodity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.Model.*
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.ViewHolder.ChatMessageViewHolder
import kotlinx.android.synthetic.main.activity_chatting.*
import kotlinx.android.synthetic.main.bottom_bar_chat.*
import kotlinx.android.synthetic.main.filler_chat.*

class ChattingActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var mUserModel : UserModel
    private lateinit var targetUser : UserModel
    private lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<ChatModelHelper, ChatMessageViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)
        setSupportActionBar(toolbar)
        initComp()
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener({
            finish()
        })
        getUserTarget()
        initAuth()
        sendChat()

    }

    private fun getUserTarget(){
        val cDatabaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS)
        cDatabaseReference.child(getKeyPost()).addValueEventListener(object : ValueEventListener{
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
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val u = it.currentUser
            if(u == null){ finish() }else{
                mUserModel = UserModel(u.uid,u.displayName.toString(),u.displayName.toString().toLowerCase(),u.email.toString(),u.photoUrl.toString())
                getData()
            }
        }
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    private fun getKeyPost() : String{
        return intent.getStringExtra("KEYPOST") ?: throw IllegalArgumentException("Keypost is null")
    }

    private fun initComp(){
        val mLayoutManager = LinearLayoutManager(applicationContext)
        chat_recycle.layoutManager = mLayoutManager
        chat_recycle.itemAnimator = DefaultItemAnimator()
    }

    private fun sendChat(){
        send_chat.setOnClickListener({
            val teks : String = text_chat.text.toString()
            if(!teks.isEmpty()){
                val refChat = FirebaseDatabase.getInstance().getReference(Constant.CHAT).child(mUserModel.uid).child(getKeyPost())
                val chatModel = ChatModel(teks,mUserModel.uid,ServerValue.TIMESTAMP)
                val listOfChat = ListOfChatModel(getKeyPost())
                refChat.addValueEventListener(object : ValueEventListener{
                    var isProgress = true
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            if(isProgress){
                                if(p0.hasChild("chat")){
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

                val oRefChat = FirebaseDatabase.getInstance().getReference(Constant.CHAT).child(getKeyPost()).child(mUserModel.uid)
                val kList = ListOfChatModel(mUserModel.uid)
                oRefChat.addValueEventListener(object : ValueEventListener{
                    var isProgress = true
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            if(isProgress){
                                if(p0.hasChild("chat")){
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
        })
    }

    private fun getData(){
        val chatDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.CHAT)
                .child(mUserModel.uid).child(getKeyPost()).child("chat")
        val fo = FirebaseRecyclerOptions.Builder<ChatModelHelper>().setQuery(chatDatabaseReference, ChatModelHelper::class.java).build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ChatModelHelper, ChatMessageViewHolder>(fo){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_list_chat,parent,false)
                return ChatMessageViewHolder(view, this@ChattingActivity)
            }

            override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int, model: ChatModelHelper) {
                holder.tanggal_kirim.text = AnotherMethods.getTimeDate(model.tanggal_post!!)
                holder.message.text =  model.message
            }
        }
        firebaseRecyclerAdapter.notifyDataSetChanged()
        chat_recycle.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }
}
