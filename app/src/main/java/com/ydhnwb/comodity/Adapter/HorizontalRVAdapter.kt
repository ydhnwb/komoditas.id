package com.ydhnwb.comodity.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.Model.ImageModel
import com.ydhnwb.comodity.R

/**
 * Created by Prieyuda Akadita S on 22/05/2018.
 */
class HorizontalRVAdapter (val context : Context, val model : MutableList<ImageModel>) : RecyclerView.Adapter<HorizontalRVAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.images_on_list, parent, false)
        return HorizontalRVAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return model.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}


    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(v: View?) {
            if (v != null) {
                shortClickListener.onClick(v,adapterPosition,false)
            }
        }


        fun setOnItemClickListener(shortClickListener: MyClickListener){
            this.shortClickListener = shortClickListener
        }


        var imageView : ImageView
        lateinit var shortClickListener : MyClickListener

        init {
            imageView = itemView.findViewById(R.id.images_on_crd)
            itemView.setOnClickListener(this)
        }

    }
}