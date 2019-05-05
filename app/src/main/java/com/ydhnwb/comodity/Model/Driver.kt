package com.ydhnwb.comodity.Model

data class Driver(val uid : String, val name : String, val email: String,val phone : String, val photo : String) {
    constructor() : this("undefined", "undefined", "undefined", "undefined", "undefined")
}