package com.ydhnwb.comodity.Model

data class Tracker(val latitude : Double, val longitude : Double, val driver : String) {
    constructor() : this (0.0, 0.0, "undefined")
}