package com.ydhnwb.comodity

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.Model.ImageModel
import com.ydhnwb.comodity.Model.PostModel
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.ViewHolder.SingleListMainViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.welcome_card.*
import com.google.firebase.database.DataSnapshot
import com.ydhnwb.comodity.Adapter.HorizontalRVAdapter
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods


class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var user : UserModel
    private lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<PostModel,SingleListMainViewHolder>
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFirebaseDatabase: FirebaseDatabase

    companion object {
        private var calledAlready : Boolean = false
        private var acceptableToLoad : Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar.title = " "
        initReference()

        fab.setOnClickListener { view ->
            startActivity(Intent(this@MainActivity, UploadActivity::class.java))
        }

        cardAction()
        getData()

    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthStateListener)
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthStateListener)
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
                acceptableToLoad = true
                user = UserModel(firebaseUser.uid,firebaseUser.displayName.toString(),firebaseUser.displayName.toString().toLowerCase(),firebaseUser.email.toString(),firebaseUser.phoneNumber.toString())
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
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.stackFromEnd = true
        mLayoutManager.reverseLayout = true
        primary_recycler.layoutManager = mLayoutManager
        primary_recycler.itemAnimator = DefaultItemAnimator()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mFirebaseDatabase.getReference(Constant.POST)
        mDatabaseReference.keepSynced(true)
        initFirebase()
    }

    private fun getData(){
        val fo = FirebaseRecyclerOptions.Builder<PostModel>().setQuery(mDatabaseReference, PostModel::class.java).build()
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<PostModel, SingleListMainViewHolder>(fo){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleListMainViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_list_main,parent,false)
                return SingleListMainViewHolder(view, this@MainActivity)
            }

            override fun onBindViewHolder(holder: SingleListMainViewHolder, position: Int, model: PostModel) {
                holder.dateUploaded.text = AnotherMethods.getTimeDate(model.tanggal_post!!)
                holder.caption.text = model.caption
                val uid : String = model.uid
                holder.harga.text = "Rp. " + model.harga
                holder.nama_barang.text = model.nama_barang
                val fDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS)
                fDatabaseReference.keepSynced(true)
                fDatabaseReference.child(uid).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null){
                            if(p0.exists()){
                                var u : UserModel? = p0.getValue(UserModel::class.java)
                                if(u != null){
                                    holder.displayName.text = u.display_name
                                    Glide.with(applicationContext).load(u.url_photo)
                                            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                            .into(holder.profilePicture)
                                }
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
                val fo2 = FirebaseRecyclerOptions.Builder<ImageModel>().setQuery(gDatabaseReference, ImageModel::class.java).build()
                val firebaseAdapter = object :  FirebaseRecyclerAdapter<ImageModel, HorizontalRVAdapter.ViewHolder>(fo2){
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalRVAdapter.ViewHolder {
                        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.images_on_list, parent, false)
                        return HorizontalRVAdapter.ViewHolder(view)
                    }

                    override fun onBindViewHolder(holder: HorizontalRVAdapter.ViewHolder, position: Int, model: ImageModel) {
                        Glide.with(this@MainActivity).load(model.photosUrl)
                                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.placeholder))
                                .into(holder.imageView)


                        holder.setOnItemClickListener(object : MyClickListener {
                            override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                                Toast.makeText(this@MainActivity, "You clicked ${model.photosUrl}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }

                }

                firebaseAdapter.notifyDataSetChanged()
                holder.horizontal_rv.adapter = firebaseAdapter
                firebaseAdapter.startListening()

                holder.setOnLongItemClickListener(object : MyClickListener{
                    override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                        val tDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST).child(getRef(position).key)
                        tDatabaseReference.addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {}
                            override fun onDataChange(p0: DataSnapshot?) {
                                if(p0 != null){
                                    if(p0.exists()){
                                        val postModel = p0.getValue(PostModel::class.java)
                                        if(postModel != null){
                                            if(user.uid.equals(postModel.uid)){
                                                val opt = AlertDialog.Builder(this@MainActivity)
                                                opt.setMessage("Lorem ipsum dolor sir amet consectuer").setCancelable(false)
                                                        .setPositiveButton("LOREM", object : DialogInterface.OnClickListener{
                                                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                                                dialog?.cancel()
                                                            }

                                                        }).setNegativeButton("CONSECTUER", object : DialogInterface.OnClickListener{
                                                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                                                dialog?.cancel()
                                                            }

                                                        })

                                                val alertDialog = opt.create()
                                                alertDialog.show()
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                })
            }
        }

        firebaseRecyclerAdapter.notifyDataSetChanged()
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

    private fun signOut(){
        mAuth.signOut()
    }

    private fun cardAction(){
        main_to_profil.setOnClickListener({
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
        })
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
