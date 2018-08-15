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
import android.widget.Toast
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.danimahardhika.cafebar.CafeBar
import com.danimahardhika.cafebar.CafeBarTheme
import com.like.LikeButton
import com.like.OnLikeListener
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.R.drawable.placeholder
import com.ydhnwb.comodity.Utilities.NetworkManager

class MainActivity : AppCompatActivity() {
    private lateinit var listOfPhotos : MutableList<ImageModel>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var user : UserModel
    private lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<PostModel,SingleListMainViewHolder>
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var likeDatabaseReference : DatabaseReference
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
        search_onSA.setShowSearchKey(true)
        fab.setOnClickListener {
            startActivity(Intent(this@MainActivity, UploadActivity::class.java))
        }
        cardAction()
        getData()
        onSearchPressed()
    }

    private fun onSearchPressed(){
        search_onSA.setOnSearchListener(object : FloatingSearchView.OnSearchListener{
            override fun onSearchAction(currentQuery: String?) {
                if(currentQuery.toString().trim() != ""){
                    val i = Intent(this@MainActivity, CariActivity::class.java)
                    i.putExtra("QUERY_SEARCH", currentQuery)
                    i.putExtra("FROM_ACTIVITY", 0)
                    startActivity(i)
                    search_onSA.clearQuery()
                }
            }
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion?) {}
        })
    }

    private fun cardAction(){
        main_to_profil.setOnClickListener { startActivity(Intent(this@MainActivity, ProfileActivity::class.java)) }
        main_to_bibit.setOnClickListener {
            val i = Intent(this@MainActivity,CariActivity::class.java)
            i.putExtra("QUERY_SEARCH", "")
            i.putExtra("FROM_ACTIVITY", Constant.BENIH)
            startActivity(i)
        }
        main_to_padi.setOnClickListener {
            val i = Intent(this@MainActivity,CariActivity::class.java)
            i.putExtra("QUERY_SEARCH", "")
            i.putExtra("FROM_ACTIVITY", Constant.PADI)
            startActivity(i)
        }
        main_to_pupuk.setOnClickListener {
            val i = Intent(this@MainActivity,CariActivity::class.java)
            i.putExtra("QUERY_SEARCH", "")
            i.putExtra("FROM_ACTIVITY", Constant.PUPUK)
            startActivity(i)
        }
        main_to_ternak.setOnClickListener {
            val i = Intent(this@MainActivity,CariActivity::class.java)
            i.putExtra("QUERY_SEARCH", "")
            i.putExtra("FROM_ACTIVITY", Constant.TERNAK)
            startActivity(i)
        }
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
                CafeBar.builder(this@MainActivity).content("Koneksi internet telah pulih")
                        .theme(CafeBarTheme.LIGHT)
                        .contentTypeface("OpenSans-Regular.ttf").show()
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
        likeDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.LIKES)
        likeDatabaseReference.keepSynced(true)

        if(!NetworkManager.isConnected(this@MainActivity)){
            checkingBack = true
            CafeBar.builder(this@MainActivity).content("Tidak ada koneksi internet")
                    .theme(CafeBarTheme.LIGHT)
                    .contentTypeface("OpenSans-Regular.ttf").show()
        }
    }

    private fun getData(){
        val fo = FirebaseRecyclerOptions.Builder<PostModel>()
                .setQuery(mDatabaseReference.orderByChild("tanggal_post").limitToLast(25), PostModel::class.java).build()
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<PostModel, SingleListMainViewHolder>(fo){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleListMainViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_list_main,parent,false)
                return SingleListMainViewHolder(view, this@MainActivity)
            }

            override fun onBindViewHolder(holder: SingleListMainViewHolder, position: Int, model: PostModel) {
                val uid : String = model.uid
                holder.harga.text = "Rp.${model.harga}"
                holder.nama_barang.text = model.nama_barang
                val fDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS)
                //fDatabaseReference.keepSynced(true)
                fDatabaseReference.child(uid).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            val u : UserModel? = p0.getValue(UserModel::class.java)
                            if(u != null){
                                holder.displayName.text = u.display_name
                                Glide.with(this@MainActivity)
                                        .load(u.url_photo).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
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

                val gDatabaseReference = getRef(position)
                gDatabaseReference.child("foto").orderByKey().limitToLast(1).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0s: DatabaseError?) {}
                    override fun onDataChange(p0s: DataSnapshot?) {
                        if(p0s != null && p0s.exists()){
                            var key = ""
                            for (child in p0s.children) {
                                key = child.key
                            }
                            gDatabaseReference.child("foto").child(key).addValueEventListener(object : ValueEventListener{
                                override fun onCancelled(p0sx: DatabaseError?) {}
                                override fun onDataChange(p0sx: DataSnapshot?) {
                                    if(p0sx != null && p0sx.exists() ){
                                        val im = p0sx.getValue(ImageModel::class.java)
                                        if (im != null) {
                                            Glide.with(applicationContext)
                                                    .load(im.photosUrl.toString())
                                                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                                            .placeholder(R.drawable.placeholder)).into(holder.preview_image)
                                        }else{
                                            Glide.with(applicationContext).load(R.drawable.no_image).into(holder.preview_image)
                                        }
                                    }
                                }
                            })
                        }else{
                            Glide.with(this@MainActivity).load(R.drawable.no_image).into(holder.preview_image)
                        }
                    }
                })
                holder.decideLikes(getRef(position).key)
                holder.likeButton.setOnLikeListener(object : OnLikeListener{
                    override fun liked(p0: LikeButton?) {
                        var isProgress = true
                        likeDatabaseReference.addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {}
                            override fun onDataChange(p0: DataSnapshot?) {
                                if(p0 != null){
                                    if (isProgress) {
                                            if(!p0.child(getRef(position).key).hasChild(user.uid)){
                                                likeDatabaseReference.child(getRef(position).key).child(user.uid).setValue(true)
                                                AnotherMethods.counter(gDatabaseReference.child("favorite"), true)
                                                isProgress = false
                                                CafeBar.builder(this@MainActivity).content("Favorit")
                                                        .theme(CafeBarTheme.LIGHT)
                                                        .contentTypeface("OpenSans-Regular.ttf").show()
                                            }
                                    }
                                }
                            }
                        })
                    }

                    override fun unLiked(p0: LikeButton?) {
                        var isProgress = true
                        likeDatabaseReference.addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {}
                            override fun onDataChange(p0: DataSnapshot?) {
                                if(p0 != null){
                                    if (isProgress) {
                                        if(p0.child(getRef(position).key).hasChild(user.uid)){
                                            likeDatabaseReference.child(getRef(position).key).child(user.uid).removeValue()
                                            AnotherMethods.counter(gDatabaseReference.child("favorite"), false)
                                            isProgress = false
                                            CafeBar.builder(this@MainActivity).content("Dihapus dari favorit")
                                                    .theme(CafeBarTheme.LIGHT)
                                                    .contentTypeface("OpenSans-Regular.ttf").show()
                                        }
                                    }
                                }
                            }

                        })
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