package com.ydhnwb.comodity.Model

import com.stfalcon.chatkit.commons.models.IMessage

/**
 * Created by Prieyuda Akadita S on 29/05/2018.
 */
data class ChatModel (var message: String, var uid: String, var tanggal_post: MutableMap<String, String>) {

    constructor() : this("","", mutableMapOf<String, String>())

}