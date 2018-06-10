package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 18/05/2018.
 */
class UploadPostModel {
    lateinit var uid : String
    lateinit var tanggal_post : MutableMap<String, String>
    lateinit var caption :String
    lateinit var harga : String
    lateinit var nama_barang : String
    lateinit var tipe_barang : String

    constructor(){}

    constructor(uid: String, tanggal_post: MutableMap<String, String>, caption: String, harga : String, nama_barang : String, tipe_barang : String){
        this.uid = uid
        this.tanggal_post = tanggal_post
        this.caption = caption
        this.harga = harga
        this.nama_barang = nama_barang
        this.tipe_barang = tipe_barang
    }


}