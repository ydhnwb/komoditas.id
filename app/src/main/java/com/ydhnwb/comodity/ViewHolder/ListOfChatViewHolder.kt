package com.ydhnwb.comodity.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.single_item_listed_chat.view.*
import org.w3c.dom.Text

/**
 * Created by Prieyuda Akadita S on 29/05/2018.
 */
class ListOfChatViewHolder(itemView : View, context : Context): RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    var profilePicture : CircleImageView
    var displayname : TextView
    var onShortClickListener : MyClickListener? = null
    var onLongClickListener : MyClickListener? = null

    init {
        profilePicture = itemView.findViewById(R.id.list_of_chat_picture_profile)
        displayname = itemView.findViewById(R.id.list_of_chat_display_name)
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