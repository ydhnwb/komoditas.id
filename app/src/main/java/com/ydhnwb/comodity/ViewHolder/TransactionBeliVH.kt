package com.ydhnwb.comodity.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.R
import de.hdodenhof.circleimageview.CircleImageView

class TransactionBeliVH (itemView : View, context : Context) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    var onShortClickListener : MyClickListener? = null
    var onLongClickListener : MyClickListener? = null
    var image : CircleImageView
    var name : TextView
    var status : TextView
    var owner : TextView
    init {

        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
        image = itemView.findViewById(R.id.list_transaction_photo)
        name = itemView.findViewById(R.id.list_transaction_name)
        status = itemView.findViewById(R.id.list_transaction_status)
        owner = itemView.findViewById(R.id.list_transaction_owner)
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