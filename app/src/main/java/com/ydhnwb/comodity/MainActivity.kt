package com.ydhnwb.comodity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.Model.ImageModel
import com.ydhnwb.comodity.Model.PostModel
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.ViewHolder.SingleListMainViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.welcome_card.*
import com.google.firebase.database.DataSnapshot
import android.support.v7.widget.SimpleItemAnimator
import com.ydhnwb.comodity.Utilities.NetworkManager

class MainActivity : AppCompatActivity() {
    private lateinit var listOfPhotos : MutableList<ImageModel>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var user : UserModel
    private lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<PostModel,SingleListMainViewHolder>
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    companion object {
        private var calledAlready : Boolean = false
        private var checkingBack = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar.title = " "
        initReference()
        fab.setOnClickListener { startActivity(Intent(this@MainActivity, UploadActivity::class.java)) }
        cardAction()
        getData()
    }

    override fun onStop() {
        mAuth.removeAuthStateListener(mAuthStateListener)
        super.onStop()
    }

    override fun onStart() {
        mAuth.addAuthStateListener(mAuthStateListener)
        if(checkingBack){
            if(NetworkManager.isConnected(this@MainActivity)){
                checkingBack = false
                Snackbar.make(root_mainActivityLayout, "Koneksi telah kembali", Snackbar.LENGTH_LONG).show()
            }
        }
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings ->
                true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initFirebase(){
        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val firebaseUser = it.currentUser
            if(firebaseUser != null){
                user = UserModel(firebaseUser.uid,firebaseUser.displayName.toString(),firebaseUser.displayName.toString().toLowerCase(),firebaseUser.email.toString(),firebaseUser.phoneNumber.toString())
                val ind = FirebaseDatabase.getInstance().getReference(Constant.INDIVIDUAL_POST).child(user.uid)
                ind.keepSynced(true)
                setupPermission()
            }else{
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    private fun initReference(){
        if(!calledAlready){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            calledAlready = true
        }
        listOfPhotos = ArrayList()
        val animator = primary_recycler.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        val mLayoutManager = LinearLayoutManager(this@MainActivity)
        mLayoutManager.reverseLayout = true
        mLayoutManager.stackFromEnd = true
        primary_recycler.layoutManager = mLayoutManager
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mFirebaseDatabase.getReference(Constant.POST)
        mDatabaseReference.keepSynced(true)
        initFirebase()
        if(!NetworkManager.isConnected(this@MainActivity)){
            checkingBack = true
            Snackbar.make(root_mainActivityLayout, "Tidak ada koneksi internet", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun getData(){
        val fo = FirebaseRecyclerOptions.Builder<PostModel>().setQuery(mDatabaseReference.orderByChild("tanggal_post"), PostModel::class.java).build()
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<PostModel, SingleListMainViewHolder>(fo){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleListMainViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_list_main,parent,false)
                return SingleListMainViewHolder(view, this@MainActivity)
            }

            override fun onBindViewHolder(holder: SingleListMainViewHolder, position: Int, model: PostModel) {
                val uid : String = model.uid
                holder.harga.text = "Rp. ${model.harga}"
                holder.nama_barang.text = model.nama_barang
                val fDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS)
                fDatabaseReference.keepSynced(true)
                fDatabaseReference.child(uid).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            val u : UserModel? = p0.getValue(UserModel::class.java)
                            if(u != null){
                                holder.displayName.text = " - " + u.display_name
                                Glide.with(applicationContext).load(u.url_photo)
                                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                        .into(holder.profilePicture)
                            }
                        }
                    }
                })
                holder.setOnItemClickListener(object : MyClickListener{
                    override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                        val i = Intent(this@MainActivity, DetailActivity::class.java)
                        i.putExtra("KEYPOST", getRef(position).key)
                        startActivity(i)
                    }
                })

                val gDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST).child(getRef(position).key).child("foto")
                gDatabaseReference.keepSynced(true)
                gDatabaseReference.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) { println("Cannot fetch image from the server...") }
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            listOfPhotos.clear()
                            for(ds in p0.children){
                                val im = ds.getValue(ImageModel::class.java)
                                listOfPhotos.add(im!!)
                            }
                            try{
                                var i = 0
                                while (listOfPhotos[i].photosUrl == null && i < listOfPhotos.size){
                                    i++
                                }
                                if(listOfPhotos[i].photosUrl != null){
                                    Glide.with(this@MainActivity).load(listOfPhotos[i].photosUrl)
                                            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.placeholder))
                                            .into(holder.preview_image)
                                }else{
                                    //todo acceptable bug tidak muncul placeholdernya
                                    Glide.with(this@MainActivity).load(R.drawable.placeholder)
                                            .into(holder.preview_image)
                                }
                            }catch (e:Exception){
                                Glide.with(this@MainActivity).load(R.drawable.no_image)
                                        .into(holder.preview_image)
                                println("Preview Image Exception : " + e.message)
                            }
                        }else{
                            Glide.with(this@MainActivity).load(R.drawable.no_image)
                                    .into(holder.preview_image)
                        }
                    }
                })

                holder.setOnLongItemClickListener(object : MyClickListener{
                    override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                        val tDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST).child(getRef(position).key)
                        tDatabaseReference.addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {}
                            override fun onDataChange(p0: DataSnapshot?) {
                                if(p0 != null && p0.exists()){
                                    val postModel = p0.getValue(PostModel::class.java)
                                    if(postModel != null && user.uid.equals(postModel.uid)){
                                        val opt = AlertDialog.Builder(this@MainActivity)
                                        opt.setMessage("Lorem ipsum dolor sir amet consectuer").setCancelable(false)
                                                .setPositiveButton("LOREM") { dialog, _ -> dialog?.cancel() }
                                                .setNegativeButton("CONSECTUER") { dialog, _ -> dialog?.cancel() }
                                        val alertDialog = opt.create()
                                        alertDialog.show()
                                    }
                                }
                            }
                        })
                    }
                })
            }
        }
        primary_recycler.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }

    private fun setupPermission(){
        val mPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if(mPerm != PackageManager.PERMISSION_GRANTED){
            makeReq()
        }
    }

    private fun makeReq(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 22)
    }

    /*private fun signOut(){
        mAuth.signOut()
    }*/

    private fun cardAction(){
        main_to_profil.setOnClickListener({ startActivity(Intent(this@MainActivity, ProfileActivity::class.java)) })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            22 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    println("Permission denied")
                }
            }
        }
    }
}