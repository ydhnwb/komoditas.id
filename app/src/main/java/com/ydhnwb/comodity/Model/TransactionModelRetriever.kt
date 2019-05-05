package com.ydhnwb.comodity.Model

data class TransactionModelRetriever(val id_transaction : String, val owner : String,
                                     val buyer : String, val date : Long,
                                     val status : String, val price : Int){

    constructor() : this("undefined", "undefined", "undefined", 0, "undefined",0)
}