package com.ydhnwb.comodity.FirebaseMethods

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Transaction
import java.text.DateFormat
import java.util.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.databinding.adapters.NumberPickerBindingAdapter.setValue
import android.support.design.widget.Snackbar
import com.google.firebase.database.MutableData



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
        fun counter(counterRef : DatabaseReference, isIncrement : Boolean) {
            counterRef.runTransaction(object : Transaction.Handler {
                override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {}
                override fun doTransaction(p0: MutableData?): Transaction.Result {
                    if (p0 != null) {
                        var i = p0.getValue(Int::class.java)!!
                        if (isIncrement) {
                            i++
                        } else if (!isIncrement) {
                            i--
                        } else {
                            println("else condition in counter")
                        }
                        p0.value = i
                    }
                    return Transaction.success(p0)
                }
            })
        }
    }
}