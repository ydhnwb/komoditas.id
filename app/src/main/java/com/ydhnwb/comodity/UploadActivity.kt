package com.ydhnwb.comodity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.ydhnwb.comodity.Model.*
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.Utilities.PopulateImagesViewHolder
import com.ydhnwb.comodity.Utilities.RecyclerItemListener
import com.ydhnwb.comodity.Utilities.TouchHelper

import kotlinx.android.synthetic.main.activity_upload.*
import kotlinx.android.synthetic.main.filler_upload.*
import java.io.ByteArrayOutputStream
import java.util.*

class UploadActivity : AppCompatActivity(), RecyclerItemListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var userModel: UserModel
    private lateinit var rootCoordinatorLayout: CoordinatorLayout
    private lateinit var list: MutableList<PopulateImagesModel>
    private lateinit var adapterVH: PopulateImagesViewHolder
    private lateinit var mStorageTask : StorageTask<UploadTask.TaskSnapshot>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        initializeFirebase()
        initComponent()
        addPhotos()
        fab.hide()
        fab.setOnClickListener {
            this.pushPost()
            finish()
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

    private fun checkListImagesAcceptable() : Boolean{ return list.size > 0 }

    private fun showHelpSwipe(){
        if(list.isEmpty()){
            swipe_left_help.visibility = View.GONE
            fab.hide()
        }else{
            fab.show()
            swipe_left_help.visibility = View.VISIBLE
        }
    }

    private fun textWatcherCaption(){
        caption_post.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                try {
                    val n = judul_post.text.trim().toString()
                    val h = harga_awal.text.trim().toString()
                    if(s?.length!! > 0 && checkListImagesAcceptable() && !n.isEmpty() && !h.isEmpty()){
                        fab.show()
                    }else{
                        fab.hide()
                    }
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
                    if(s?.length!! > 0 && checkListImagesAcceptable() && !c.isEmpty() && !h.isEmpty()){
                        fab.show()
                    }else{
                        fab.hide()
                    }
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
                    if(s?.length!! > 0 && checkListImagesAcceptable() && !j.isEmpty() && !c.isEmpty()){
                        fab.show()
                    }else{
                        fab.hide()
                    }

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
            val captions : String = caption_post.text.toString()
            val uid = userModel.uid
            val harga = harga_awal.text.toString()
            val namabrg = judul_post.text.toString()
            if(!captions.isEmpty() && !uid.isEmpty() && !harga.isEmpty() && !namabrg.isEmpty() && list.size != 0){
                val individualReference = FirebaseDatabase.getInstance().getReference(Constant.INDIVIDUAL_POST).child(uid)
                val categorizedPost = FirebaseDatabase.getInstance().getReference(Constant.CATEGORIZED_POST).child(tipe_barang.selectedItem.toString())
                val uploadModel = UploadPostModel(uid, ServerValue.TIMESTAMP, captions, harga, namabrg, tipe_barang.selectedItem.toString().toLowerCase(), 0)
                val generatedKey = mDatabaseReference.push().key
                mDatabaseReference.child(generatedKey).setValue(uploadModel)
                val ind = IndividualPostModel(generatedKey, uid)
                individualReference.push().setValue(ind)
                categorizedPost.push().setValue(ind)
                uploadPhotos(generatedKey)
            }else{ Snackbar.make(rootCoordinatorLayout, R.string.harap_isi_semua_form, Snackbar.LENGTH_LONG).show() }
        }catch (e:Exception){
            println("pushPost : ${e.message}")
        }
    }

    private fun uploadPhotos(generatedKey : String){
        try{
            val mFirebaseDatabase= FirebaseDatabase.getInstance()
            val sDatabaseReference = mFirebaseDatabase.getReference(Constant.POST)
            var i =0
            val mFirebaseStorage = FirebaseStorage.getInstance().getReference(Constant.IMAGE_POST)
            lateinit var storageReference: StorageReference
            if(!list.isEmpty()){
                while(i < list.size){
                    storageReference = mFirebaseStorage.child(userModel.email).child(generatedKey).child("${list[i].fileName}-${System.currentTimeMillis()}")
                    val bitmap : Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, list[i].filePath)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
                    val data = byteArrayOutputStream.toByteArray()
                    mStorageTask = storageReference.putBytes(data).addOnSuccessListener { taskSnapshot ->
                        val imageUrl = ImageModel(taskSnapshot.downloadUrl.toString())
                        sDatabaseReference.child(generatedKey).child("foto").push().setValue(imageUrl)
                    }.addOnFailureListener {
                        println("Cannot upload caused by ${it.message}")
                    }
                    i++
                }
            }
        }catch (e:Exception){
            println("Exception " +e.message + " in uploadPhotos")
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
        mAuthListener = FirebaseAuth.AuthStateListener {
            val fireUser = it.currentUser
            if (fireUser == null) {
                finish()
            } else {
                userModel = UserModel(fireUser.uid, fireUser.displayName.toString(),fireUser.displayName.toString().toLowerCase(), fireUser.email.toString(), fireUser.photoUrl.toString())
                mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST)
            }
        }
        mAuth.addAuthStateListener(mAuthListener)
    }

    private fun addPhotos() {
        add_photos.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select picture"), Constant.REQUEST_CODE_IMAGE)
        }
    }

    private fun getFileName(uri : Uri) : String? {
        var res : String? = null
        if(uri.scheme.equals("content")){
            val cursor = contentResolver.query(uri,null,null,null,null)
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
                                val finalName : String = getFileName(uriFile).toString()
                                val p = PopulateImagesModel(finalName,uriFile,Constant.STATUS)
                                list.add(p)
                                i++
                            }
                        }
                        data.data != null -> {
                            val uriFile : Uri = data.data
                            val finalName : String = getFileName(uriFile).toString()
                            val p = PopulateImagesModel(finalName,uriFile,Constant.STATUS)
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
            val i : PopulateImagesModel = list[viewHolder.adapterPosition]
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
