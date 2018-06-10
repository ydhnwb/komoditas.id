package com.ydhnwb.comodity.Interfaces

import android.view.View

/**
 * Created by Prieyuda Akadita S on 19/05/2018.
 */
interface MyClickListener {
    abstract fun onClick(v: View, position: Int, isLongClick: Boolean)
}