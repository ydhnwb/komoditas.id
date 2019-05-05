package com.ydhnwb.comodity.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.single_list_choose_driver.view.*

class ChooseDriverVH (itemView : View, context : Context): RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener  {
    var name : TextView
    var photo : CircleImageView
    var status : TextView
    var phone : TextView
    var onShortClickListener : MyClickListener? = null
    var onLongClickListener : MyClickListener? = null

    init{
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
        name = itemView.findViewById(R.id.list_driver_name)
        photo = itemView.findViewById(R.id.list_driver_photo)
        phone = itemView.findViewById(R.id.list_driver_phone)
        status = itemView.findViewById(R.id.list_driver_status)
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