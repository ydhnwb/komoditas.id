package com.ydhnwb.comodity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.danimahardhika.cafebar.CafeBar
import com.danimahardhika.cafebar.CafeBarTheme
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.like.LikeButton
import com.like.OnLikeListener
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.Model.ImageModel
import com.ydhnwb.comodity.Model.IndividualPostModel
import com.ydhnwb.comodity.Model.PostModel
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.ViewHolder.SingleListMainViewHolder
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.filler_profile.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mAuthStateListeer : FirebaseAuth.AuthStateListener
    private lateinit var me : UserModel
    private lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<IndividualPostModel,SingleListMainViewHolder>
    private var sDatabaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.INDIVIDUAL_POST)
    private lateinit var listOfPhotos : MutableList<ImageModel>
    private lateinit var likeDatabaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        listOfPhotos = ArrayList()
        initAuth()
        val animator = profile_recycle.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.stackFromEnd = true
        mLayoutManager.reverseLayout = true
        profile_recycle.layoutManager = mLayoutManager
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        fab.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, UploadActivity::class.java))
        }
        aksi()
        getData()
    }

    private fun aksi(){
        profile_go_to_pengaturan.setOnClickListener {
            Toast.makeText(this@ProfileActivity, "Settings activity", Toast.LENGTH_SHORT).show()
        }

        profile_go_to_pesan.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ListChatActivity::class.java))
        }

        profile_go_to_transaksi.setOnClickListener {
            Toast.makeText(this@ProfileActivity, "Transaction activity", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initAuth(){
        mAuth = FirebaseAuth.getInstance()
        mAuthStateListeer = FirebaseAuth.AuthStateListener {
            val i = it.currentUser
            if(i == null){
                finish()
            }else{
                me = UserModel(i.uid,i.displayName.toString(),i.displayName.toString().toLowerCase(),i.email.toString(),i.photoUrl.toString())
                bindData()
//                getData()
            }
        }
        mAuth.addAuthStateListener(mAuthStateListeer)
        likeDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.LIKES)
        likeDatabaseReference.keepSynced(true)
    }

    override fun onStart() {
        mAuth.addAuthStateListener(mAuthStateListeer)
        super.onStart()
    }

    override fun onStop() {
        mAuth.removeAuthStateListener(mAuthStateListeer)
        super.onStop()
    }

    private fun bindData(){
        Glide.with(applicationContext).load(me.url_photo)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(profile_pic)
        profile_display_name.text = me.display_name
        profile_email.text = me.email
    }

    private fun getData(){
        val fo = FirebaseRecyclerOptions.Builder<IndividualPostModel>()
                .setQuery(sDatabaseReference.child(mAuth.currentUser!!.uid), IndividualPostModel::class.java)
                .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<IndividualPostModel, SingleListMainViewHolder>(fo){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleListMainViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_list_main,parent,false)
                return SingleListMainViewHolder(view, this@ProfileActivity)

            }

            override fun onBindViewHolder(holder: SingleListMainViewHolder, position: Int, model: IndividualPostModel) {
                val mainDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST).child(model.key)
                mainDatabaseReference.keepSynced(true)
                mainDatabaseReference.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            val p = p0.getValue(PostModel::class.java)
                            if (p != null) {
                                holder.harga.text = "Rp.${p.harga}"
                                holder.nama_barang.text = p.nama_barang
                                val uDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS).child(p.uid)
                                uDatabaseReference.keepSynced(true)
                                uDatabaseReference.addValueEventListener(object : ValueEventListener{
                                    override fun onCancelled(p0x: DatabaseError?) {}
                                    override fun onDataChange(p0x: DataSnapshot?) {
                                        if((p0x != null) && p0x.exists()){
                                            val u = p0x.getValue(UserModel::class.java)
                                            if (u != null) {
                                                Glide.with(applicationContext).load(u.url_photo)
                                                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                                        .into(holder.profilePicture)
                                                holder.displayName.text = u.display_name
                                            }
                                        }
                                    }

                                })
                                //==============
                                val gDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST).child(p0.key)
                                gDatabaseReference.child("foto").orderByKey().limitToFirst(1).addValueEventListener(object : ValueEventListener{
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
                                                            Glide.with(applicationContext).load(im.photosUrl.toString()).apply(RequestOptions().placeholder(R.drawable.placeholder)).into(holder.preview_image)
                                                        }else{
                                                            Glide.with(applicationContext).load(R.drawable.no_image).into(holder.preview_image)
                                                        }
                                                    }
                                                }
                                            })
                                        }else{
                                            Glide.with(this@ProfileActivity).load(R.drawable.no_image).into(holder.preview_image)
                                        }
                                    }
                                })
                                //===============
                                holder.decideLikes(mainDatabaseReference.key)
                                holder.likeButton.setOnLikeListener(object : OnLikeListener {
                                    override fun liked(p0: LikeButton?) {
                                        var isProgress = true
                                        likeDatabaseReference.addValueEventListener(object : ValueEventListener{
                                            override fun onCancelled(p0: DatabaseError?) {}
                                            override fun onDataChange(p0: DataSnapshot?) {
                                                if(p0 != null){
                                                    if (isProgress) {
                                                        if(!p0.child(mainDatabaseReference.key).hasChild(me.uid)){
                                                            likeDatabaseReference.child(mainDatabaseReference.key).child(me.uid).setValue(true)
                                                            AnotherMethods.counter(mainDatabaseReference.child("favorite"), true)
                                                            isProgress = false
                                                            CafeBar.builder(this@ProfileActivity).content("Favorite")
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
                                                        if(p0.child(mainDatabaseReference.key).hasChild(me.uid)){
                                                            likeDatabaseReference.child(mainDatabaseReference.key).child(me.uid).removeValue()
                                                            AnotherMethods.counter(mainDatabaseReference.child("favorite"), false)
                                                            isProgress = false
                                                            CafeBar.builder(this@ProfileActivity).content("Dihapus dari favorite")
                                                                    .theme(CafeBarTheme.LIGHT)
                                                                    .contentTypeface("OpenSans-Regular.ttf").show()
                                                        }
                                                    }
                                                }
                                            }

                                        })
                                    }
                                })
                                holder.likeButton.isLiked = false
                                holder.setOnLongItemClickListener(object : MyClickListener {
                                    override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                                        val tDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST).child(getRef(position).key)
                                        tDatabaseReference.addValueEventListener(object : ValueEventListener{
                                            override fun onCancelled(p0: DatabaseError?) {}
                                            override fun onDataChange(p0: DataSnapshot?) {
                                                if(p0 != null && p0.exists()){
                                                    val postModel = p0.getValue(PostModel::class.java)
                                                    if(postModel != null && me.uid.equals(postModel.uid)){
                                                        val opt = AlertDialog.Builder(this@ProfileActivity)
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
                    }
                })


            }
        }

        profile_recycle.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }


}
