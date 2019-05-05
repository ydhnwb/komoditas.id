package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 29/05/2018.
 */
data class IndividualPostModel(var key : String, var owner: String,var tipe_barang : String) {
    constructor() : this("", "", "") {}

}