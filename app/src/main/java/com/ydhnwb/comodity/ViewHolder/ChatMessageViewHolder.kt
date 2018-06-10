package com.ydhnwb.comodity.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.R
import kotlinx.android.synthetic.main.single_list_chat.view.*

/**
 * Created by Prieyuda Akadita S on 29/05/2018.
 */
class ChatMessageViewHolder(itemView : View, context : Context): RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener  {
    var onShortClickListener : MyClickListener? = null
    var onLongClickListener : MyClickListener? = null
    var message : TextView
    var tanggal_kirim : TextView

    init{
        message = itemView.findViewById(R.id.textView_message)
        tanggal_kirim = itemView.findViewById(R.id.textView_tanggal)
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