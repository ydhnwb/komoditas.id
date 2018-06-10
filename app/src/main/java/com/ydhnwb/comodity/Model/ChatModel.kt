package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 29/05/2018.
 */
class ChatModel {
    lateinit var message : String
    lateinit var uid : String
    lateinit var tanggal_post : MutableMap<String, String>

    constructor(){}
    constructor(message : String, uid : String, tanggal_post : MutableMap<String, String>){
        this.message = message
        this.uid = uid
        this.tanggal_post = tanggal_post
    }
}