package com.ydhnwb.comodity.Utilities

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ydhnwb.comodity.Model.PopulateImagesModel
import com.ydhnwb.comodity.R

/**
 * Created by Prieyuda Akadita S on 18/05/2018.
 */
class PopulateImagesViewHolder(var context: Context, var model: MutableList<PopulateImagesModel>) : RecyclerView.Adapter<PopulateImagesViewHolder.MViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.single_item_populate_image, parent, false)
        return PopulateImagesViewHolder.MViewHolder(view)
    }

    override fun getItemCount(): Int {
        return model.size
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        var myModel : PopulateImagesModel = model.get(position)
        holder.fileName.text = myModel.fileName
        Glide.with(context).load(myModel.filePath).into(holder.image)
        if(myModel.status.equals(Constant.STATUS)){
            holder.status.setImageResource(R.drawable.ic_action_waiting)
        }else{
            holder.status.setImageResource(R.drawable.ic_action_done_green)
        }
    }


    fun removeItem(position: Int){
        model.removeAt(position)
        notifyItemRemoved(position)
    }

    fun undoDeleteItem(sModel : PopulateImagesModel, position : Int){
        model.add(position, sModel)
        notifyItemInserted(position)
    }

    class MViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView){
        var image : ImageView
        var fileName : TextView
        var status : ImageView
        var view_background : RelativeLayout
        var view_foreground : RelativeLayout
        init {
            image = itemView!!.findViewById(R.id.image_list_circle)
            fileName = itemView.findViewById(R.id.fileName)
            status = itemView.findViewById(R.id.status_image_view)
            view_background = itemView.findViewById(R.id.view_background)
            view_foreground = itemView.findViewById(R.id.view_foreground)
        }

    }



}