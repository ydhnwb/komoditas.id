package com.ydhnwb.comodity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.Model.ListOfChatModel
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.ViewHolder.ListOfChatViewHolder
import kotlinx.android.synthetic.main.activity_list_chat.*
import kotlinx.android.synthetic.main.filler_list_chat.*

class ListChatActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var me: UserModel
    private lateinit var fireAdapter: FirebaseRecyclerAdapter<ListOfChatModel, ListOfChatViewHolder>
    private var mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.CHAT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_chat)
        setSupportActionBar(toolbar)
        initComp()
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener({
            finish()
        })
        initAuth()
    }


    private fun initComp() {
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.stackFromEnd = true
        mLayoutManager.reverseLayout = true
        list_of_chat_rv.layoutManager = mLayoutManager
        list_of_chat_rv.itemAnimator = DefaultItemAnimator()
    }


    private fun initAuth() {
        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val o = it.currentUser
            if (o == null) {
                finish()
            } else {
                me = UserModel(o.uid, o.displayName.toString(),o.displayName.toString().toLowerCase(), o.email.toString(), o.photoUrl.toString())
                fetchData()
            }
        }
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    private fun fetchData() {
        val fo = FirebaseRecyclerOptions.Builder<ListOfChatModel>()
                .setQuery(mDatabaseReference.child(me.uid), ListOfChatModel::class.java).build()

        fireAdapter = object : FirebaseRecyclerAdapter<ListOfChatModel, ListOfChatViewHolder>(fo) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListOfChatViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_item_listed_chat, parent, false)
                return ListOfChatViewHolder(view, this@ListChatActivity)

            }

            override fun onBindViewHolder(holder: ListOfChatViewHolder, position: Int, model: ListOfChatModel) {
                val uid : String = model.uid
                val oDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS).child(uid)
                oDatabaseReference.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if((p0 != null) && p0.exists()){
                            val u = p0.getValue(UserModel::class.java)
                            if(u != null){
                                holder.displayname.text = u.display_name
                                Glide.with(applicationContext).load(u.url_photo)
                                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                        .into(holder.profilePicture)
                            }
                        }
                    }
                })

                holder.setOnItemClickListener(object : MyClickListener{
                    override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                        val i = Intent(this@ListChatActivity, ChattingActivity::class.java)
                        i.putExtra("KEYPOST", getRef(position).key)
                        startActivity(i)
                    }

                })
            }
        }
        fireAdapter.notifyDataSetChanged()
        list_of_chat_rv.adapter = fireAdapter
        fireAdapter.startListening()

    }
}
