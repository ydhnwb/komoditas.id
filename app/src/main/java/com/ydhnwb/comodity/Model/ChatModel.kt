package com.ydhnwb.comodity.Model


/**
 * Created by Prieyuda Akadita S on 29/05/2018.
 */
data class ChatModel (var message: String, var uid: String, var tanggal_post: MutableMap<String, String>) {

    constructor() : this("","", mutableMapOf<String, String>())

}