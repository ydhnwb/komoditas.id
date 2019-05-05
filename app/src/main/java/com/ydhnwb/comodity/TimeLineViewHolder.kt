package com.ydhnwb.comodity

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.vipulasri.timelineview.TimelineView
import com.ydhnwb.comodity.Model.Tracker
import kotlinx.android.synthetic.main.content_tracking.view.*
import kotlinx.android.synthetic.main.single_list_timeline.view.*

class TimeLineViewHolder(var context: Context, var model: MutableList<Tracker>) : RecyclerView.Adapter<TimeLineViewHolder.MViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        val view = View.inflate(parent.context, R.layout.single_list_timeline, null)
        return TimeLineViewHolder.MViewHolder(view, viewType)
    }

    override fun getItemCount() = model.size

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount);
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        holder.bindData(model.get(position), context)
    }

    class MViewHolder(itemView : View, viewType : Int) : RecyclerView.ViewHolder(itemView){

        fun bindData(model : Tracker, context: Context){
            itemView.time_marker.initLine(itemViewType)
            itemView.list_timeline_name.text = "Latitude : ${model.latitude} Long : ${model.longitude}"
            itemView.setOnClickListener {
                Toast.makeText(context, "Click works!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}