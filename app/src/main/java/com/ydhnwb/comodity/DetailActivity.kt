package com.ydhnwb.comodity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.Model.PostModel
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.R.id.display_name_detail
import com.ydhnwb.comodity.R.id.photo_profile_on_detail
import com.ydhnwb.comodity.Utilities.Constant
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.bottom_bar_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var mDatabaseRefence : DatabaseReference
    private lateinit var mPostModel : PostModel
    private lateinit var mUserModel : UserModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener({
            finish()
        })
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        initAuth()
        toolbar_layout.title = " "
        initCollapsingToolbar()
        kirimPesanAndBeli()
    }

    private fun kirimPesanAndBeli(){
        kirimpesan.setOnClickListener({
            val c = Intent(this@DetailActivity, ChattingActivity::class.java)
            c.putExtra("KEYPOST", mPostModel.uid)
            startActivity(c)
        })

        beli.setOnClickListener({
            val g = Intent(this@DetailActivity, BeliActivity::class.java)
            g.putExtra("KEYPOST", getKeyPost())
            startActivity(g)
        })
    }

    private fun initCollapsingToolbar(){
        app_bar.addOnOffsetChangedListener { _, verticalOffset ->
            var isShow = false
            var scrollRange = -1
            if (scrollRange == -1) {
                scrollRange = app_bar.totalScrollRange
            }
            when {
                scrollRange + verticalOffset == 0 -> {
                    toolbar_layout.title = mPostModel.nama_barang
                    isShow = true;
                }
                isShow -> {
                    toolbar_layout.title = " "
                    isShow = false;
                }
                scrollRange + verticalOffset > 0 -> {
                    toolbar_layout.title = " "
                    isShow = false;
                }
            }
        }
    }

    private fun initAuth(){
        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if(user == null){
             finish()
            }else{
                mUserModel = UserModel(user.uid,user.displayName.toString(),user.displayName.toString().toLowerCase(),user.email.toString(),user.photoUrl.toString())
                initReference()
            }
        }
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    private fun getKeyPost() : String{
        return intent.getStringExtra("KEYPOST") ?: throw IllegalArgumentException("Keypost is null")
    }

    private fun initReference(){
        mDatabaseRefence = FirebaseDatabase.getInstance().getReference(Constant.POST)
        mDatabaseRefence.child(getKeyPost()).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0 != null){
                 if(p0.exists()){
                     mPostModel = p0.getValue(PostModel::class.java)!!
                     caption_detail.text = mPostModel.caption
                     judul_post.text = mPostModel.nama_barang
                     tanggal_post_detail.text = AnotherMethods.getTimeDate(mPostModel.tanggal_post!!)
                     harga_on_detail.text = "Rp."+mPostModel.harga
                     if (mUserModel.uid.equals(mPostModel.uid)){
                         relative_bottom_detail.visibility = View.GONE
                     }
                     val jDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS).child(mPostModel.uid)
                     jDatabaseReference.addValueEventListener(object : ValueEventListener{
                         override fun onCancelled(p0: DatabaseError?) {}
                         override fun onDataChange(p0: DataSnapshot?) {
                             if(p0 !=  null){
                                 if(p0.exists()){
                                     val uModel = p0.getValue(UserModel::class.java)
                                     if (uModel != null) {
                                         display_name_detail.text = uModel.display_name
                                         Glide.with(this@DetailActivity).load(uModel.url_photo)
                                                 .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)) .into(photo_profile_on_detail)
                                         photo_profile_on_detail.setOnClickListener({
                                             if (mUserModel.uid.equals(uModel.uid)){
                                                 Toast.makeText(this@DetailActivity, "You are clicking yourself", Toast.LENGTH_SHORT).show()
                                             }else{
                                                 Toast.makeText(this@DetailActivity, "U r click other people", Toast.LENGTH_SHORT).show()
                                             }
                                         })
                                     }
                                 }
                             }
                         }
                     })
                 }
                }
            }
        })
    }
}
