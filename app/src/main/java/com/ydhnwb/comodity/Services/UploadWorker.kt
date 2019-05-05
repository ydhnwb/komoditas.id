package com.ydhnwb.comodity.Services

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.app.JobIntentService
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.ydhnwb.comodity.Model.ImageModel
import com.ydhnwb.comodity.Model.PopulateImageModel
import com.ydhnwb.comodity.Model.PopulateImagesModel
import com.ydhnwb.comodity.Utilities.Constant
import java.io.ByteArrayOutputStream
import android.databinding.adapters.SeekBarBindingAdapter.setProgress
import android.os.Handler
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods


class UploadWorker : JobIntentService() {

    companion object {
        val JOB_ID = 112
        fun starNewWork(context : Context, intent : Intent){
            Log.d("Serfis", " startNewWork")
            enqueueWork(context, UploadWorker::class.java, JOB_ID,intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        try{
            val b : Bundle = intent.getBundleExtra("DATA")
            //val list : MutableList<PopulateImageModel> = b.getParcelableArrayList("LIST")
            val generatedKey = b.getString("GENERATED_KEY")
            val email = b.getString("EMAIL")
            val FILENAME = b.getString("FILENAME")
            val FILEPATH = Uri.parse(b.getString("FILEPATH"))
            AnotherMethods.uploadPhotos(applicationContext, generatedKey, email, FILENAME, FILEPATH)
        }catch (e:Exception){
            Log.d("CATCHEX", "$e")
            println("CATCHEX $e")
        }
    }


    override fun onDestroy() {
        println("Service : OnDestroy()")
        super.onDestroy()
    }

    override fun onStopCurrentWork(): Boolean {
        println("Service : CurrentWorkIsStopped")
        return super.onStopCurrentWork()
    }

}