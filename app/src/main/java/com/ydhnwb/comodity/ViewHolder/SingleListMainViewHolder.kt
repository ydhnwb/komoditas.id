package com.ydhnwb.comodity.ViewHolder

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.R
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by Prieyuda Akadita S on 19/05/2018.
 */
class SingleListMainViewHolder(itemView : View, context : Context ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
    var preview_image : ImageView
    var profilePicture : CircleImageView
    var displayName : TextView
    var nama_barang : TextView
    var onShortClickListener : MyClickListener? = null
    var onLongClickListener : MyClickListener? = null
    var harga : TextView


    init{
        preview_image = itemView.findViewById(R.id.preview_image)
        nama_barang = itemView.findViewById(R.id.nama_barang_on_list)
        profilePicture = itemView.findViewById(R.id.user_profile_on_list)
        displayName = itemView.findViewById(R.id.user_name_on_list)
        harga = itemView.findViewById(R.id.harga_on_list)
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

}