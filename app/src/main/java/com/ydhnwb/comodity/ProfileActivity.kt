package com.ydhnwb.comodity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ydhnwb.comodity.Adapter.HorizontalRVAdapter
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
    var sDatabaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.INDIVIDUAL_POST)
    companion object {
        private var acceptableToLoad : Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.stackFromEnd = true
        mLayoutManager.reverseLayout = true
        profile_recycle.layoutManager = mLayoutManager
        profile_recycle.itemAnimator = DefaultItemAnimator()
        initAuth()
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        fab.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, UploadActivity::class.java))
        }
        aksi()

    }

    private fun aksi(){
        profile_go_to_pengaturan.setOnClickListener({
            Toast.makeText(this@ProfileActivity, "Settings activity", Toast.LENGTH_SHORT).show()
        })

        profile_go_to_pesan.setOnClickListener({
            startActivity(Intent(this@ProfileActivity, ListChatActivity::class.java))
        })

        profile_go_to_transaksi.setOnClickListener({
            Toast.makeText(this@ProfileActivity, "Transaction activity", Toast.LENGTH_SHORT).show()
        })
    }

    private fun initAuth(){
        mAuth = FirebaseAuth.getInstance()
        mAuthStateListeer = FirebaseAuth.AuthStateListener {
            val i = it.currentUser
            if(i == null){
                finish()
            }else{
                acceptableToLoad = true
                me = UserModel(i.uid,i.displayName.toString(),i.displayName.toString().toLowerCase(),i.email.toString(),i.photoUrl.toString())
                bindData()
                getData()
            }
        }
        mAuth.addAuthStateListener(mAuthStateListeer)

    }

    private fun bindData(){
        Glide.with(applicationContext).load(me.url_photo)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(profile_pic)
        profile_display_name.text = me.display_name
        profile_email.text = me.email
    }

    private fun getData(){
        val fo = FirebaseRecyclerOptions.Builder<IndividualPostModel>().setQuery(sDatabaseReference.child(me.uid), IndividualPostModel::class.java)
                .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<IndividualPostModel, SingleListMainViewHolder>(fo){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleListMainViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_list_main,parent,false)
                return SingleListMainViewHolder(view, this@ProfileActivity)

            }

            override fun onBindViewHolder(holder: SingleListMainViewHolder, position: Int, model: IndividualPostModel) {
                val mainDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST).child(model.key)
                mainDatabaseReference.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            val p = p0.getValue(PostModel::class.java)
                            if (p != null) {
                                holder.harga.text = "Rp.${p.harga}"
                                holder.nama_barang.text = p.nama_barang
                                holder.caption.text = p.caption
                                holder.dateUploaded.text = AnotherMethods.getTimeDate(p.tanggal_post!!)
                                val uDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS).child(p.uid)
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
                                val ref2 : DatabaseReference = p0.ref.child("foto")
                                val fo2 = FirebaseRecyclerOptions.Builder<ImageModel>().setQuery(ref2, ImageModel::class.java).build()
                                val firebaseAdapter = object :  FirebaseRecyclerAdapter<ImageModel, HorizontalRVAdapter.ViewHolder>(fo2){
                                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalRVAdapter.ViewHolder {
                                        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.images_on_list, parent, false)
                                        return HorizontalRVAdapter.ViewHolder(view)
                                    }

                                    override fun onBindViewHolder(holder: HorizontalRVAdapter.ViewHolder, position: Int, model: ImageModel) {
                                        Glide.with(this@ProfileActivity).load(model.photosUrl)
                                                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.placeholder))
                                                .into(holder.imageView)


                                        holder.setOnItemClickListener(object : MyClickListener {
                                            override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                                                Toast.makeText(this@ProfileActivity, "You clicked ${model.photosUrl}", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                    }

                                }
                                firebaseAdapter.notifyDataSetChanged()
                                holder.horizontal_rv.adapter = firebaseAdapter
                                firebaseAdapter.startListening()
                            }
                        }
                    }

                })
            }
        }

        firebaseRecyclerAdapter.notifyDataSetChanged()
        profile_recycle.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }


}
