package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 11/05/2018.
 */
data class UserModel(var uid : String, var display_name: String, var display_name_idiomatic: String, var email: String,
                     var url_photo: String) {
    constructor() : this("","","","","")

}