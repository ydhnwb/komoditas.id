package com.ydhnwb.comodity.Model

data class TransactionModelUploader(val id_transaction : String, val owner : String,
                                    val buyer : String, val date : MutableMap<String, String>,
                                    val status : String, val price : Int)