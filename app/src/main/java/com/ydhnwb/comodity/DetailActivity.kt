package com.ydhnwb.comodity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import com.daimajia.slider.library.Tricks.ViewPagerEx
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.Model.ImageModel
import com.ydhnwb.comodity.Model.PostModel
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.Utilities.Constant
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.bottom_bar_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import com.google.firebase.database.DataSnapshot



class DetailActivity : AppCompatActivity(), BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var mDatabaseRefence : DatabaseReference
    private lateinit var mPostModel : PostModel
    private lateinit var mUserModel : UserModel
    private lateinit var listOfImages : MutableList<ImageModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        listOfImages = ArrayList()
        relative_bottom_detail.visibility = View.GONE
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
        kirimpesan.setOnClickListener {
            val c = Intent(this@DetailActivity, ChattingActivity::class.java)
            c.putExtra("KEYPOST", mPostModel.uid)
            startActivity(c)
        }

        beli.setOnClickListener {
            val g = Intent(this@DetailActivity, BeliActivity::class.java)
            g.putExtra("KEYPOST", getKeyPost())
            startActivity(g)
        }
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
            if(null == user){
             finish()
            }else{
                mUserModel = UserModel(user.uid,user.displayName.toString(),user.displayName.toString().toLowerCase(),user.email.toString(),user.photoUrl.toString())
                initReference()
            }
        }
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    private fun getKeyPost() : String{
        return intent.getStringExtra("KEYPOST") ?: throw IllegalArgumentException("Keypost is null") as Throwable
    }

    override fun onStart() {
        //detail_image_slider.startAutoCycle()
        mAuth.addAuthStateListener(mAuthStateListener)
        super.onStart()
    }

    override fun onStop() {
        //detail_image_slider.stopAutoCycle()
        mAuth.removeAuthStateListener(mAuthStateListener)
        super.onStop()
    }

    override fun onDestroy() {
        //Glide.with(this@DetailActivity).pauseRequests();
        detail_image_slider.stopAutoCycle()
        super.onDestroy()
    }

    private fun initReference(){
        mDatabaseRefence = FirebaseDatabase.getInstance().getReference(Constant.POST)
        mDatabaseRefence.child(getKeyPost()).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0 != null){
                 if(p0.exists()){
                     mPostModel = p0.getValue(PostModel::class.java)!!
                     caption_detail.text = mPostModel.caption
                     judul_post.text = mPostModel.nama_barang
                     tanggal_post_detail.text = AnotherMethods.getTimeDate(mPostModel.tanggal_post!!)
                     harga_on_detail.text = "Rp."+mPostModel.harga
                     diminati.text = " - diminati ${mPostModel.favorite} orang"
                     if (!mUserModel.uid.equals(mPostModel.uid)){
                         relative_bottom_detail.visibility = View.VISIBLE
                     }
                     val jDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS).child(mPostModel.uid)
                     jDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
                         override fun onCancelled(p0: DatabaseError?) {}
                         override fun onDataChange(p0: DataSnapshot?) {
                             if(p0 != null && p0.exists()){
                                 val uModel = p0.getValue(UserModel::class.java)
                                 if(uModel != null){
                                     display_name_detail.text = uModel.display_name
                                     Glide.with(applicationContext).load(uModel.url_photo)
                                             .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                                             .into(photo_profile_on_detail)
                                     photo_profile_on_detail.setOnClickListener {
                                         if(mUserModel.uid.equals(uModel.uid)){
                                             startActivity(Intent(this@DetailActivity, ProfileActivity::class.java))
                                         }else{
                                             Toast.makeText(this@DetailActivity, "Other profile Activity", Toast.LENGTH_SHORT).show()
                                         }
                                     }
                                 }
                             }
                         }

                     })
                 }
                }
            }
        })
        mDatabaseRefence.child(getKeyPost()).child("foto").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0 != null && p0.exists()){
                    listOfImages.clear()
                    for (ds in p0.children) {
                        val im = ds.getValue(ImageModel::class.java)
                        if (im != null) {
                            listOfImages.add(im)
                        }
                    }
                    setImageSlider()
                }
            }
        })
    }

    private fun setImageSlider(){
        var i = 0
        while(i < listOfImages.size){
            val o = TextSliderView(this)
            o.image(listOfImages[i].photosUrl)
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                    .setOnSliderClickListener(this@DetailActivity)
            o.bundle(Bundle())
            o.bundle.putString("photoUrl", listOfImages[i].photosUrl)
            detail_image_slider.addSlider(o)
            i++
            }
        detail_image_slider.setPresetTransformer(SliderLayout.Transformer.Default)
        detail_image_slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom)
        detail_image_slider.addOnPageChangeListener(this@DetailActivity)
        detail_image_slider.stopAutoCycle()
    }

    override fun onSliderClick(slider: BaseSliderView?) {
        if (slider != null) {
            Toast.makeText(this@DetailActivity, slider.bundle.get("photoUrl").toString(), Toast.LENGTH_SHORT).show()
        }
    }
    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
    }


}

