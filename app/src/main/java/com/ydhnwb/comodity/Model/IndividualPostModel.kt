package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 29/05/2018.
 */
class IndividualPostModel {
    lateinit var key : String
    lateinit var owner : String
    constructor(){}
    constructor(key : String, owner : String){
        this.key = key
        this.owner = owner
    }
}