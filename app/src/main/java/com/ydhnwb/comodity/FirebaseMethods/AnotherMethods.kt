package com.ydhnwb.comodity.FirebaseMethods

import android.app.Activity
import android.content.Context
import java.text.DateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.ydhnwb.comodity.Model.ImageModel
import com.ydhnwb.comodity.Model.PopulateImageModel
import com.ydhnwb.comodity.Model.TransactionModelUploader
import com.ydhnwb.comodity.Utilities.Constant
import java.io.ByteArrayOutputStream




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

        fun getFileName(uri : Uri, context : Context) : String? {
            var res : String? = null
            if(uri.scheme.equals("content")){
                val cursor = context.contentResolver.query(uri,null,null,null,null)
                try {
                    if(cursor != null && cursor.moveToFirst()){
                        res = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }catch (e : Exception){
                    println("getFileName : ${e.message}")
                }finally {
                    cursor.close()
                }
            }
            if(res == null){
                res = uri.path
                val i : Int = res.lastIndexOf("/ch")
                if(i != -1){
                    res = res.substring(i+1)
                }
            }
            return res
        }

        fun uploadPhotos(context : Context, generatedKey : String, email : String, fileName : String,
                         filePath : Uri){
            try{
                lateinit var storageReference: StorageReference
                val mFirebaseDatabase= FirebaseDatabase.getInstance()
                val sDatabaseReference = mFirebaseDatabase.getReference(Constant.POST)
                val mFirebaseStorage = FirebaseStorage.getInstance().getReference(Constant.IMAGE_POST)
                val millis = System.currentTimeMillis().toString()
                storageReference = mFirebaseStorage.child(email).child(generatedKey).child(millis+"_${fileName}")
                val bitmap : Bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, filePath)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream)
                val data = byteArrayOutputStream.toByteArray()
                var mStorageTask : StorageTask<UploadTask.TaskSnapshot> = storageReference.putBytes(data).addOnSuccessListener { taskSnapshot ->
                    val imageUrl = ImageModel(taskSnapshot.downloadUrl.toString())
                    sDatabaseReference.child(generatedKey).child("foto").push().setValue(imageUrl)
                }.addOnFailureListener {
                    println("Proses cannot upload caused by ${it.message}")
                }.addOnProgressListener {
                    val onProgressPercentage : Long = 100 * it.bytesTransferred / it.totalByteCount
                    println("Proses $onProgressPercentage %")
                }
            }catch (e:Exception){
                println("Proses " +e.message + " in uploadPhotos")
            }
        }

        fun userTransaction(userId : String, id_transaction : String, owner : String, price : Int){
            val mRef = FirebaseDatabase.getInstance().getReference(Constant.USER_TRANSACTION)
            val tr = TransactionModelUploader(id_transaction, owner, userId, ServerValue.TIMESTAMP, "waiting", price)
            mRef.child("outcoming").child(userId).child(id_transaction).setValue(tr)
            mRef.child("incoming").child(owner).child(id_transaction).setValue(tr)
        }

    }




}