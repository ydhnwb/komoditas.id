package com.ydhnwb.comodity.Fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.danimahardhika.cafebar.CafeBar
import com.danimahardhika.cafebar.CafeBarTheme
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ydhnwb.comodity.DetailActivity
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.Model.PostModel
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.R
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.ViewHolder.SingleListSearchBarangViewHolder
import com.like.LikeButton
import com.like.OnLikeListener
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.Model.ImageModel
import kotlinx.android.synthetic.main.fragment_barang.view.*


class FragmentBarang : Fragment() {
    private var listOfPhotos : MutableList<ImageModel> = ArrayList()
    private lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<PostModel, SingleListSearchBarangViewHolder>
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var likeDatabaseReference : DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mAuthStateListener : FirebaseAuth.AuthStateListener
    private lateinit var u : UserModel
    private lateinit var userDatabaseReference : DatabaseReference
    private lateinit var searchQuery : String
    private var activityCode = 0
    private lateinit var categoryPost : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_barang, container, false)
        val mLayoutManager = GridLayoutManager(activity, 2)
        view.recyclerViewBarang.layoutManager = mLayoutManager
        val animator = view.recyclerViewBarang.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        initAuth()
        if(arguments != null){
            searchQuery = arguments!!.getString("QUERY_SEARCH").toString()
            activityCode = arguments!!.getInt("ACTIVITY_CODE",0)
            initFire(activityCode)
        }

        val fo : FirebaseRecyclerOptions<PostModel>
        if(activityCode != 0 && searchQuery.isEmpty()){
            fo = FirebaseRecyclerOptions.Builder<PostModel>()
                    .setQuery(mDatabaseReference.orderByChild("tipe_barang").equalTo(categoryPost), PostModel::class.java).build()

        }else if(activityCode == 0){
            fo = FirebaseRecyclerOptions.Builder<PostModel>()
                    .setQuery(mDatabaseReference.orderByChild("nama_barang_idiomatic")
                            .startAt(searchQuery.toLowerCase()).endAt(searchQuery.toLowerCase() + "\uf8ff"), PostModel::class.java).build()

        }else if(activityCode != 0 && !searchQuery.isEmpty()){
            val s = categoryPost+"_"+searchQuery.toLowerCase()
            fo = FirebaseRecyclerOptions.Builder<PostModel>()
                    .setQuery(mDatabaseReference.orderByChild("tipe_nama")
                            .startAt(s).endAt(s + "\uf8ff"), PostModel::class.java).build()
        }else{
            fo = FirebaseRecyclerOptions.Builder<PostModel>()
                    .setQuery(mDatabaseReference.orderByChild("tipe_barang").equalTo(categoryPost), PostModel::class.java).build()
        }


        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<PostModel, SingleListSearchBarangViewHolder>(fo){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleListSearchBarangViewHolder {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_list_search_barang ,parent,false)
                return SingleListSearchBarangViewHolder(v, activity!!)
            }
            override fun onBindViewHolder(holder: SingleListSearchBarangViewHolder, position: Int, model: PostModel) {
                val uid = model.uid
                holder.nama_barang.text = model.nama_barang
                holder.harga.text = "Rp."+model.harga
                userDatabaseReference.child(uid).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            val m = p0.getValue(UserModel::class.java)
                            if (m != null) {
                                Glide.with(context!!).load(m.url_photo)
                                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                                        .into(holder.foto_profil)
                                holder.display_name.text = m.display_name
                            }
                        }
                    }
                })
                holder.setOnItemClickListener(object : MyClickListener {
                    override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                        val i = Intent(activity, DetailActivity::class.java)
                        i.putExtra("KEYPOST", getRef(position).key)
                        startActivity(i)
                    }
                })

                val gDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST).child(getRef(position).key)
                gDatabaseReference.keepSynced(true)
                gDatabaseReference.child("foto").addValueEventListener(object : ValueEventListener{
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
                                    Glide.with(activity!!).load(listOfPhotos[i].photosUrl)
                                            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.placeholder))
                                            .into(holder.image_barang)
                                }else{
                                    Glide.with(activity!!).load(R.drawable.placeholder)
                                            .into(holder.image_barang)
                                }
                            }catch (e : Exception){
                                Glide.with(activity!!).load(R.drawable.no_image)
                                        .into(holder.image_barang)
                                println("Preview Image Exception : " + e.message)
                            }
                        }else{
                            Glide.with(activity!!).load(R.drawable.no_image)
                                    .into(holder.image_barang)
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
                                    if(postModel != null && u.uid.equals(postModel.uid)){
                                        val opt = AlertDialog.Builder(activity!!)
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

                holder.decideLikes(getRef(position).key)
                holder.like_button.setOnLikeListener(object : OnLikeListener {
                    override fun liked(p0: LikeButton?) {
                        var isProgress = true
                        likeDatabaseReference.addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {}
                            override fun onDataChange(p0: DataSnapshot?) {
                                if(p0 != null){
                                    if (isProgress) {
                                        if(!p0.child(getRef(position).key).hasChild(u.uid)){
                                            likeDatabaseReference.child(getRef(position).key).child(u.uid).setValue(true)
                                            AnotherMethods.counter(gDatabaseReference.child("favorite"), true)
                                            isProgress = false
                                            CafeBar.builder(activity!!).content("Favorit")
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
                                        if(p0.child(getRef(position).key).hasChild(u.uid)){
                                            likeDatabaseReference.child(getRef(position).key).child(u.uid).removeValue()
                                            AnotherMethods.counter(gDatabaseReference.child("favorite"), false)
                                            isProgress = false
                                            CafeBar.builder(activity!!).content("Dihapus dari favorit")
                                                    .theme(CafeBarTheme.LIGHT)
                                                    .contentTypeface("OpenSans-Regular.ttf").show()
                                        }
                                    }
                                }
                            }

                        })
                    }
                })
                //holder.like_button.isLiked = false
            }
        }
        view.recyclerViewBarang.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
        return view
    }

    private fun initFire(codeActivity : Int){
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        userDatabaseReference = mFirebaseDatabase.getReference(Constant.USERS)
        likeDatabaseReference = mFirebaseDatabase.getReference(Constant.LIKES)
        mDatabaseReference = mFirebaseDatabase.getReference(Constant.POST)
        when(codeActivity){
            1 -> {categoryPost = Constant.BENIH_REF.toLowerCase()}
            2 -> {categoryPost = Constant.PADI_REF.toLowerCase() }
            3 -> {categoryPost = Constant.PUPUK_REF.toLowerCase()}
            4 -> {categoryPost = Constant.TERNAK_REF.toLowerCase() }
            else -> { println("search all posts")}
        }
    }

    private fun initAuth(){
        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val c = it.currentUser
            if(c != null){
                u = UserModel(c.uid,c.displayName.toString(),c.displayName.toString().toLowerCase(),
                        c.email.toString(), c.photoUrl.toString())
            }
        }

        mAuth.addAuthStateListener(mAuthStateListener)
    }


}