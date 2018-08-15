package com.ydhnwb.comodity.Fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ydhnwb.comodity.DetailActivity
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.ProfileActivity
import com.ydhnwb.comodity.R
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.ViewHolder.SingleListSearchUserViewHolder
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class FragmentUser : Fragment() {
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFirebaseDatabase : FirebaseDatabase
    private lateinit var mUser : UserModel
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mAuthStateListener : FirebaseAuth.AuthStateListener
    private lateinit var querySearch : String
    private lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<UserModel, SingleListSearchUserViewHolder>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        val mLayoutManager = LinearLayoutManager(activity)
        view.recyclerViewUser.layoutManager  = mLayoutManager
        initFire()
        initAuth()
        if(arguments != null && !arguments!!.getString("QUERY_SEARCH").toString().isEmpty()){
            querySearch = arguments!!.getString("QUERY_SEARCH").toString()
            val fo = FirebaseRecyclerOptions.Builder<UserModel>().setQuery(mDatabaseReference.orderByChild("display_name_idiomatic").startAt(querySearch.toLowerCase()).endAt(querySearch.toLowerCase() + "\uf8ff"), UserModel::class.java).build()
            firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<UserModel, SingleListSearchUserViewHolder>(fo){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleListSearchUserViewHolder {
                    val v = LayoutInflater.from(parent.context).inflate(R.layout.single_list_search_user ,parent,false)
                    return SingleListSearchUserViewHolder(v, activity!!)
                }
                override fun onBindViewHolder(holder: SingleListSearchUserViewHolder, position: Int, model: UserModel) {
                    holder.displayName.text = model.display_name
                    Glide.with(context!!).load(model.url_photo)
                                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                                .into(holder.profilePic)

                    holder.setOnItemClickListener(object : MyClickListener {
                        override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                            if(mUser.uid.equals(model.uid)){
                                startActivity(Intent(activity, ProfileActivity::class.java))
                            }else{
                                Toast.makeText(activity, "Go to lapak ${model.display_name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })

                }

            }
            view.recyclerViewUser.adapter = firebaseRecyclerAdapter
            firebaseRecyclerAdapter.startListening()

        }
        return view
    }

    private fun initAuth(){
        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val fUser = it.currentUser
            if(fUser != null){
                mUser = UserModel(fUser.uid,fUser.displayName.toString(),fUser.displayName.toString().toLowerCase(),
                        fUser.email.toString(), fUser.photoUrl.toString())
            }
        }
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    private fun initFire(){
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mFirebaseDatabase.getReference(Constant.USERS)
    }
}