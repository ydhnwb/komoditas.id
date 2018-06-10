package com.ydhnwb.comodity.Utilities

import android.support.v7.widget.RecyclerView

/**
 * Created by Prieyuda Akadita S on 18/05/2018.
 */
interface RecyclerItemListener {
    fun onSwiped(viewHolder : RecyclerView.ViewHolder, direction : Int, position : Int)

}