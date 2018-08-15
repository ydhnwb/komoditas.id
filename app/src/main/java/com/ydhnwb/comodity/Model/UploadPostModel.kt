package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 18/05/2018.
 */
data class UploadPostModel (var uid: String, var tanggal_post: MutableMap<String, String>,var caption: String,
                            var harga: String,var nama_barang: String, var nama_barang_idiomatic : String,
                            var tipe_barang: String,var favorite: Int, var tipe_nama : String) {
    constructor() : this("", mutableMapOf<String, String>(),"","0","No name",
            "","",0, "")


}