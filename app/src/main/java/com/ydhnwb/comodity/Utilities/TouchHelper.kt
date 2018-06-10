package com.ydhnwb.comodity.Utilities

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

/**
 * Created by Prieyuda Akadita S on 18/05/2018.
 */
class TouchHelper (dragDirs: Int, swipeDirs: Int, rListener: RecyclerItemListener): ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs)  {
    var listener : RecyclerItemListener
    init {
        this.listener = rListener
    }

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        if(listener != null){
            if (viewHolder != null) {
                listener.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
        //super.clearView(recyclerView, viewHolder)
        val viewForeground = (viewHolder as PopulateImagesViewHolder.MViewHolder).view_foreground
        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewForeground)
    }

    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        //super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val viewForeground = (viewHolder as PopulateImagesViewHolder.MViewHolder).view_foreground
        ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(c,recyclerView, viewForeground, dX,dY, actionState,isCurrentlyActive)
    }

    override fun onChildDrawOver(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        //super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val viewForeground = (viewHolder as PopulateImagesViewHolder.MViewHolder).view_foreground
        ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(c,recyclerView, viewForeground, dX,dY, actionState,isCurrentlyActive)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        //super.onSelectedChanged(viewHolder, actionState)
        if(viewHolder != null){
            val viewForeground = (viewHolder as PopulateImagesViewHolder.MViewHolder).view_foreground
            ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(viewForeground)
        }
    }
}