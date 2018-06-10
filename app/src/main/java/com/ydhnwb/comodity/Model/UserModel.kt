package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 11/05/2018.
 */
class UserModel {
    lateinit var uid : String
    lateinit var display_name : String
    lateinit var display_name_idiomatic : String
    lateinit var email :String
    lateinit var url_photo : String
    constructor(){}
    constructor(uid : String, display_name : String, display_name_idiomatic: String,email : String, url_photo : String){
        this.uid = uid
        this.display_name = display_name
        this.email = email
        this.url_photo = url_photo
        this.display_name_idiomatic = display_name_idiomatic
    }
}