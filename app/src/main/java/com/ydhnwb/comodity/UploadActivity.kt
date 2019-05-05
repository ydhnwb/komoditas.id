package com.ydhnwb.comodity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.JobIntentService
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.Model.*
import com.ydhnwb.comodity.Services.UploadWorker
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.Utilities.PopulateImagesViewHolder
import com.ydhnwb.comodity.Utilities.RecyclerItemListener
import com.ydhnwb.comodity.Utilities.TouchHelper

import kotlinx.android.synthetic.main.activity_upload.*
import kotlinx.android.synthetic.main.content_upload.*
import kotlinx.android.synthetic.main.filler_upload.*
import kotlin.collections.ArrayList

class UploadActivity : AppCompatActivity(), RecyclerItemListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private lateinit var mDatabaseReference: DatabaseReference
    private var userModel: FirebaseUser? = null
    private lateinit var rootCoordinatorLayout: CoordinatorLayout
    private lateinit var list: MutableList<PopulateImageModel>
    private lateinit var adapterVH: PopulateImagesViewHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        initializeFirebase()
        loading_upload.visibility = View.INVISIBLE
        initComponent()
        addPhotos()
        fab.hide()
        fab.setOnClickListener {
            pushPost()
        }
        toolbar.setNavigationOnClickListener {
            val s :String = caption_post.text.toString()
            if(!s.isEmpty() || !harga_awal.text.isEmpty()){
                val builder : AlertDialog.Builder = AlertDialog.Builder(this@UploadActivity)
                builder.setMessage(R.string.anda_yakin).setCancelable(false)
                        .setPositiveButton(R.string.yakin) { _, _ -> finish() }.setNegativeButton(R.string.tetap_disini) { dialog, _ -> dialog?.cancel() }

                val alertDialog = builder.create()
                alertDialog.show()
            }else{
                finish()
            }
        }
        showHelpSwipe()
        textWatcherCaption()
    }

    private fun showHelpSwipe(){
        if(list.isEmpty()){
            swipe_left_help.visibility = View.GONE
            fab.hide()
        }else{
            fab.show()
            swipe_left_help.visibility = View.VISIBLE
        }
    }

    private fun isAcceptable(judul : String, harga : String, watcher : String){
        if(watcher.length > 0 && (list.size > 0) && !judul.isEmpty() && !harga.isEmpty()){
            fab.show()
        }else{
            fab.hide()
        }
    }

    private fun textWatcherCaption(){
        caption_post.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                try {
                    val n = judul_post.text.trim().toString()
                    val h = harga_awal.text.trim().toString()
                    isAcceptable(n,h,s.toString())
                }catch (e:Exception){
                    fab.hide()
                    println(e.message)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        judul_post.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                try{
                    val c = caption_post.text.trim().toString()
                    val h = harga_awal.text.trim().toString()
                    isAcceptable(c,h,s.toString())
                }catch (e:Exception){fab.hide()}
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        harga_awal.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val j = judul_post.text.trim().toString()
                val c = caption_post.text.trim().toString()
                try{
                    isAcceptable(j,c,s.toString())
                }catch (e:Exception){
                    fab.hide()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun pushPost(){
        try{
            loading_upload.isIndeterminate = true
            loading_upload.visibility = View.VISIBLE
            val captions : String = caption_post.text.toString()
            val uid = userModel?.uid.toString()
            val harga = harga_awal.text.toString()
            val namabrg = judul_post.text.toString()
            if(!captions.isEmpty() && !uid.isEmpty() && !harga.isEmpty() && !namabrg.isEmpty() && list.size != 0){
                val individualReference = FirebaseDatabase.getInstance().getReference(Constant.INDIVIDUAL_POST).child(uid)
                val categorizedPost = FirebaseDatabase.getInstance().getReference(Constant.CATEGORIZED_POST).child(tipe_barang.selectedItem.toString())
                val uploadModel = UploadPostModel(uid, ServerValue.TIMESTAMP, captions, harga, namabrg, namabrg.toLowerCase(),
                        tipe_barang.selectedItem.toString().toLowerCase(), 0,
                        tipe_barang.selectedItem.toString().toLowerCase()+"_"+namabrg.toLowerCase())
                val generatedKey = mDatabaseReference.push().key
                mDatabaseReference.child(generatedKey).setValue(uploadModel)
                val ind = IndividualPostModel(generatedKey, uid, tipe_barang.selectedItem.toString().toLowerCase().toLowerCase())
                individualReference.child(generatedKey).setValue(ind)
                categorizedPost.push().setValue(ind)
                uploadBackground(generatedKey)

            }else{
                loading_upload.visibility = View.INVISIBLE
                Snackbar.make(rootCoordinatorLayout, R.string.harap_isi_semua_form, Snackbar.LENGTH_LONG).show()
            }
        }catch (e:Exception){
            println("pushPost : ${e.message}")
        }
    }

    private fun uploadBackground(generatedKey : String){
        try{
            var i = 0
            while(i < list.size){
                val intent = Intent()
                val b = Bundle()
                //b.putParcelableArrayList("LIST", list as java.util.ArrayList<out Parcelable>)
                b.putString("EMAIL", userModel?.email)
                b.putString("GENERATED_KEY", generatedKey)
                b.putString("FILENAME", list[i].fileName)
                b.putString("FILEPATH", list[i].filePath.toString())
                intent.putExtra("DATA", b)
                JobIntentService.enqueueWork(this@UploadActivity, UploadWorker::class.java, UploadWorker.JOB_ID, intent)
                i++
            }
            println("Proses S Executed")
            finish()
        }catch(e:Exception){
            Log.d("CATCHSome", e.message)
            Toast.makeText(this@UploadActivity, e.message, Toast.LENGTH_LONG).show()
            println("CATCHSOME $e.message")
        }

    }

    private fun initComponent(){
        rootCoordinatorLayout = findViewById(R.id.rootLayoutUpload)
        list = ArrayList()
        adapterVH = PopulateImagesViewHolder(this@UploadActivity, list)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        list_photos_catatan.layoutManager = mLayoutManager
        list_photos_catatan.isNestedScrollingEnabled = false
        list_photos_catatan.itemAnimator = DefaultItemAnimator()
        val c = TouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(c).attachToRecyclerView(list_photos_catatan)
    }

    private fun initializeFirebase() {
        mAuth = FirebaseAuth.getInstance()
        userModel = mAuth.currentUser
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST)
    }

    private fun addPhotos() {
        add_photos.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            startActivityForResult(Intent.createChooser(intent, "Select picture"), Constant.REQUEST_CODE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Constant.REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK){
            if(data != null){
                try {
                    when {
                        data.clipData != null -> {
                            val totalSelected = data.clipData.itemCount
                            var i = 0
                            while (i < totalSelected){
                                val uriFile : Uri = data.clipData.getItemAt(i).uri
                                val finalName : String = AnotherMethods.getFileName(uriFile, this@UploadActivity).toString()
                                val p = PopulateImageModel(finalName,uriFile,Constant.STATUS)
                                val takeFlags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                                try {
                                    this@UploadActivity.contentResolver.takePersistableUriPermission(uriFile, takeFlags);
                                } catch (e : SecurityException){
                                    e.printStackTrace()
                                    println("Proses Security Exception ${e.message}")
                                }
                                list.add(p)
                                i++
                            }
                        }
                        data.data != null -> {
                            val uriFile : Uri = data.data
                            val finalName : String = AnotherMethods.getFileName(uriFile, this@UploadActivity).toString()
                            val p = PopulateImageModel(finalName,uriFile,Constant.STATUS)
                            val takeFlags = data.getFlags() and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            try {
                                this@UploadActivity.contentResolver.takePersistableUriPermission(uriFile, takeFlags);
                            } catch (e : SecurityException){
                                e.printStackTrace()
                                println("Proses Security Exception Once ${e.message}")
                            }
                            list.add(p)
                        }
                        else -> Toast.makeText(this@UploadActivity, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                    }
                    list_photos_catatan.adapter = adapterVH
                    showHelpSwipe()
                }catch (e : Exception){
                    println("onActivity Result : ${e.message}")
                }
            }
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if(viewHolder is PopulateImagesViewHolder.MViewHolder){
            val s = list[viewHolder.adapterPosition].fileName
            val i : PopulateImageModel = list[viewHolder.adapterPosition]
            val delete = viewHolder.adapterPosition
            adapterVH.removeItem(delete)
            val snack = Snackbar.make(rootCoordinatorLayout,"$s dihapus",Snackbar.LENGTH_SHORT).setAction(R.string.batal) {
                adapterVH.undoDeleteItem(i,delete)
                showHelpSwipe()
            }
            snack.setActionTextColor(Color.RED)
            snack.show()
            showHelpSwipe()
        }
    }


}
