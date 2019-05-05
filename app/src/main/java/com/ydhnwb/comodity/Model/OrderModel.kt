package com.ydhnwb.comodity.Model

data class OrderModel(val transaction_id : String, val status : Boolean) {
    constructor() : this("undefined", false)
}