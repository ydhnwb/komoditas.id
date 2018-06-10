package com.ydhnwb.comodity.FirebaseMethods

import java.text.DateFormat
import java.util.*


class AnotherMethods {
    companion object {
        fun getTimeDate(timeStamp: Long): String {
            try {
                val dateFormat = DateFormat.getDateTimeInstance()
                val netDate = Date(timeStamp)
                return dateFormat.format(netDate)
            } catch (e: Exception) {
                println("Cannot getTimeDate caused by ${e.message}")
                return "date"
            }
        }


    }
}