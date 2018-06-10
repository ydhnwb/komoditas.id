package com.ydhnwb.comodity.ViewHolder

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.R
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by Prieyuda Akadita S on 19/05/2018.
 */
class SingleListMainViewHolder(itemView : View, context : Context ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    var profilePicture : CircleImageView
    var displayName : TextView
    var caption : TextView
    var dateUploaded : TextView
    var nama_barang : TextView
    var onShortClickListener : MyClickListener? = null
    var onLongClickListener : MyClickListener? = null
    var horizontal_rv : RecyclerView
    var harga : TextView


    init{
        nama_barang = itemView.findViewById(R.id.nama_barang_on_list)
        profilePicture = itemView.findViewById(R.id.user_profile_on_list)
        displayName = itemView.findViewById(R.id.user_name_on_list)
        caption = itemView.findViewById(R.id.caption_on_list)
        dateUploaded = itemView.findViewById(R.id.date_on_list)
        harga = itemView.findViewById(R.id.harga_on_list)
        horizontal_rv = itemView.findViewById(R.id.horizontal_rv)
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        horizontal_rv.layoutManager = linearLayoutManager
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