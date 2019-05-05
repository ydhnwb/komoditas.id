package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 29/05/2018.
 */
data class ChatModelHelper(var message: String, var uid: String, var tanggal_post: Long, val key : String) {

    constructor() : this("", "", 0, "") {}

}