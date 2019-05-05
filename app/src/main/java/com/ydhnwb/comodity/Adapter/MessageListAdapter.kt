package com.ydhnwb.comodity.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.Model.ChatModelHelper
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.R
import com.ydhnwb.comodity.Utilities.Constant
import kotlinx.android.synthetic.main.item_list_message_received.view.*
import kotlinx.android.synthetic.main.item_list_message_sent.view.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.widget.Toast
import com.ydhnwb.comodity.Model.ImageModel


class MessageListAdapter(var context: Context, var model: MutableList<ChatModelHelper>,val uid : String,val userTarget : String) : RecyclerView.Adapter<MessageListAdapter.MViewHolder>() {

    val isMe = 1
    val isNotMe = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        if(viewType == isMe){
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_list_message_sent, parent, false)
            return MessageListAdapter.MViewHolder(view)
        }else{
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_list_message_received, parent, false)
            return MessageListAdapter.MViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return model.size
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        var chatModel = model[position]
        if (chatModel.uid.equals(uid)){
            holder.bindSent(chatModel, context, uid, userTarget)
        }else{
            holder.bindReceive(chatModel, context, uid, userTarget)
        }

    }


    override fun getItemViewType(position: Int): Int {
        var chatModel = model[position]
        return if (chatModel.uid.equals(uid)){
            isMe
        }else{
            isNotMe
        }
    }

    class MViewHolder (itemView : View?) : RecyclerView.ViewHolder(itemView){
        val userRef = FirebaseDatabase.getInstance().getReference(Constant.USERS)

        fun bindSent(message : ChatModelHelper , ctx : Context, uid : String, userTarget: String){
            itemView.item_message_sent_message_body.text = message.message
            itemView.item_message_sent_tanggal_post.text = AnotherMethods.getTimeDate(message.tanggal_post)
            itemView.setOnLongClickListener {
                val clipboardManager = ctx.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val myClipData = ClipData.newPlainText("captions", message.message)
                clipboardManager.primaryClip = myClipData
                Toast.makeText(ctx, "Teks berhasil disalin", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener false
            }

            val fotoChecker = FirebaseDatabase.getInstance().getReference(Constant.CHAT).child(uid).child(userTarget).child("chat").child(message.key).child("foto")
            fotoChecker.orderByKey().limitToLast(1).addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0 != null && p0.exists()){
                        var fotokey = ""
                        for (child in p0.getChildren()) {
                            fotokey = child.key
                        }
                        val im = p0.child(fotokey).getValue(ImageModel::class.java)
                        if (im != null) {
                            Toast.makeText(ctx, p0.toString(), Toast.LENGTH_LONG).show()
                            Glide.with(ctx).load(im.photosUrl).into(itemView.image_in_chat_bubble_me)
                        }

                    }
                }

            })

        }

        fun bindReceive(message: ChatModelHelper, ctx : Context, uid : String, userTarget: String){
            itemView.item_message_received_message_body.text = message.message
            itemView.item_message_received_tanggal_post.text = AnotherMethods.getTimeDate(message.tanggal_post)
            userRef.child(message.uid).addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0 != null && p0.exists()){
                        val u = p0.getValue(UserModel::class.java)
                        if(u != null){
                            itemView.item_message_received_display_name.text = u.display_name
                            Glide.with(ctx).load(u.url_photo).into(itemView.item_message_received_photo_profile)
                        }
                    }
                }
            })
            itemView.setOnLongClickListener {
                val clipboardManager = ctx.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val myClipData = ClipData.newPlainText("captions", message.message)
                clipboardManager.primaryClip = myClipData
                Toast.makeText(ctx, "Teks berhasil disalin", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener false
            }
            val fotoChecker = FirebaseDatabase.getInstance().getReference(Constant.CHAT).child(uid).child(userTarget).child("chat").child(message.key).child("foto")
            fotoChecker.orderByKey().limitToLast(1).addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0 != null && p0.exists()){
                        var fotokey = ""
                        for (child in p0.getChildren()) {
                            fotokey = child.key
                        }
                        val im = p0.child(fotokey).getValue(ImageModel::class.java)
                        if (im != null) {
                            Glide.with(ctx).load(im.photosUrl).into(itemView.image_in_chat_bubble_receive)
                        }
                    }
                }

            })

        }
    }

}