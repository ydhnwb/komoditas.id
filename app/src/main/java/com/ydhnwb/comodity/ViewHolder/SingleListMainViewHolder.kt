package com.ydhnwb.comodity.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.like.LikeButton
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.R
import com.ydhnwb.comodity.Utilities.Constant
import com.google.firebase.auth.FirebaseAuth

class SingleListMainViewHolder(itemView : View, context : Context ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
    var preview_image : ImageView
    //var profilePicture : CircleImageView
    var displayName : TextView
    var nama_barang : TextView
    var onShortClickListener : MyClickListener? = null
    var onLongClickListener : MyClickListener? = null
    var harga : TextView
    var likeButton : LikeButton
    private var likesDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.LIKES)
    private lateinit var cAuth : FirebaseAuth

    init{
        preview_image = itemView.findViewById(R.id.preview_image)
        nama_barang = itemView.findViewById(R.id.nama_barang_on_list)
        //profilePicture = itemView.findViewById(R.id.user_profile_on_list)
        displayName = itemView.findViewById(R.id.user_name_on_list)
        harga = itemView.findViewById(R.id.harga_on_list)
        likeButton = itemView.findViewById(R.id.likeButton)
        likesDatabaseReference.keepSynced(true)
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
    }

    override fun onClick(v: View?) {
        onShortClickListener?.onClick(v!!, adapterPosition, false)
    }

    override fun onLongClick(v: View?): Boolean {
        onLongClickListener?.onClick(v!!,adapterPosition,true)
        return false
    }

    fun setOnLongItemClickListener(longClick : MyClickListener){
        this.onLongClickListener = longClick
    }

    fun setOnItemClickListener(shortClick : MyClickListener){
        this.onShortClickListener = shortClick
    }

    fun decideLikes(uidPost : String){
        cAuth = FirebaseAuth.getInstance()
        val u = cAuth.currentUser
        if(u != null){
            likesDatabaseReference.child(uidPost).addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0 != null && p0.exists()){
                        if(p0.hasChild(u.uid)){
                            likeButton.isLiked = true
                        }
                    }
                }
            })

        }
    }

}