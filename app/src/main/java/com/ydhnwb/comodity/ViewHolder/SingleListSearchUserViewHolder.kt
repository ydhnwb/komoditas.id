package com.ydhnwb.comodity.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.single_list_search_user.view.*

class SingleListSearchUserViewHolder (itemView : View, context : Context) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
    var onShortClickListener : MyClickListener? = null
    var onLongClickListener : MyClickListener? = null
    var profilePic : CircleImageView
    var displayName : TextView

    init{
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
        profilePic = itemView.findViewById(R.id.list_search_user_profile_pic)
        displayName = itemView.findViewById(R.id.list_search_user_display_name)
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