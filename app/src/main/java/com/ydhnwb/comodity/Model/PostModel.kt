package com.ydhnwb.comodity.Model

/**
 * Created by Prieyuda Akadita S on 19/05/2018.
 */
class PostModel {
    lateinit var uid : String
    var tanggal_post : Long? = null
    lateinit var caption :String
    lateinit var harga : String
    lateinit var nama_barang : String
    lateinit var tipe_barang : String

    constructor(){}

    constructor(uid: String, tanggal_post: Long, caption: String, harga : String, nama_barang : String, tipe_barang : String){
        this.uid = uid
        this.tanggal_post = tanggal_post
        this.caption = caption
        this.harga = harga
        this.nama_barang = nama_barang
        this.tipe_barang = tipe_barang
    }
}