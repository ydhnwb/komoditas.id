package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 29/05/2018.
 */
class ChatModelHelper {
    lateinit var message : String
    lateinit var uid : String
    var tanggal_post : Long? = null

    constructor(){}
    constructor(message : String, uid : String, tanggal_post : Long){
        this.message = message
        this.uid = uid
        this.tanggal_post = tanggal_post
    }
}