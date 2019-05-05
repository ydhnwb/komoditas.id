package com.ydhnwb.comodity.Model

import com.google.firebase.database.ServerValue

/**
 * Created by Prieyuda Akadita S on 29/05/2018.
 */
data class ListOfChatModel(var uid: String, val date : Long) {
    //maybe i'll add something in this model class in the future
    constructor() : this("", 0) {}
 }