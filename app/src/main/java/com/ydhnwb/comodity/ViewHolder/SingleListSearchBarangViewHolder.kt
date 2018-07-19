package com.ydhnwb.comodity.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.like.LikeButton
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.R
import com.ydhnwb.comodity.Utilities.Constant
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.*

class SingleListSearchBarangViewHolder(itemView : View, context : Context) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
    var image_barang : ImageView
    var foto_profil : CircleImageView
    var nama_barang : TextView
    var harga : TextView
    var display_name : TextView
    var like_button : LikeButton
    var onShortClickListener : MyClickListener? = null
    var onLongClickListener : MyClickListener? = null
    private var likesDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.LIKES)
    private lateinit var cAuth : FirebaseAuth

    init {
        image_barang = itemView.findViewById(R.id.preview_image_on_barang_search)
        foto_profil = itemView.findViewById(R.id.foto_profil_on_search)
        nama_barang = itemView.findViewById(R.id.nama_barang_on_search)
        harga = itemView.findViewById(R.id.harga_on_search)
        display_name = itemView.findViewById(R.id.display_name_on_search)
        like_button = itemView.findViewById(R.id.like_on_search)
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
            likesDatabaseReference.child(uidPost).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0 != null && p0.exists()){
                        if(p0.hasChild(u.uid)){
                            like_button.isLiked = true
                        }
                    }
                }

            })

        }
    }
}