package com.ydhnwb.comodity.FirebaseMethods

import java.text.DateFormat
import java.util.*

class AnotherMethods {
    companion object {
        fun getTimeDate(timeStamp: Long): String {
            return try {
                val dateFormat = DateFormat.getDateTimeInstance()
                val netDate = Date(timeStamp)
                dateFormat.format(netDate)
            }catch(e: Exception) {
                println("Cannot getTimeDate caused by ${e.message}")
                "date"
            }
        }
    }
}