package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 19/05/2018.
 */
data class PostModel(val uid : String, val tanggal_post : Long, val favorite: Int, val caption: String,
                     val harga: String, val nama_barang: String, val nama_barang_idiomatic : String,
                     val tipe_barang: String, var tipe_nama : String) {

    constructor() : this("",0,0,"","0","",
            "", "", "")


}