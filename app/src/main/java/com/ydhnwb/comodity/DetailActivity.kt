package com.ydhnwb.comodity

import android.content.Intent
import android.os.Bundle
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
import com.danimahardhika.cafebar.CafeBar
import com.danimahardhika.cafebar.CafeBarTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
    private var mUserModel : FirebaseUser? = null
    private lateinit var listOfImages : MutableList<ImageModel>
    private lateinit var likeDatabaseReference : DatabaseReference
    private lateinit var fotoListener : ValueEventListener
    private lateinit var likeListener : ValueEventListener
    private lateinit var postListener : ValueEventListener
    private var isLiked = false

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
            val isShow = false
            var scrollRange = -1
            if (scrollRange == -1) {
                scrollRange = app_bar.totalScrollRange
            }
            when {
                scrollRange + verticalOffset == 0 -> {
                    toolbar_layout.title = mPostModel.nama_barang
                }
                isShow -> {
                    toolbar_layout.title = " "
                }
                scrollRange + verticalOffset > 0 -> {
                    toolbar_layout.title = " "
                }
            }
        }
    }

    private fun initAuth(){
        mAuth = FirebaseAuth.getInstance()
        mUserModel = mAuth.currentUser
        if(mUserModel != null){
            initReference()
        }
    }

    private fun getKeyPost() : String{
        return intent.getStringExtra("KEYPOST") ?: throw IllegalArgumentException("Keypost is null") as Throwable
    }

    private fun fabAction(){
        if(mUserModel?.uid.equals(mPostModel.uid)){
            fab.setImageDrawable(resources.getDrawable(R.drawable.ic_action_create_white))
            fab.setOnClickListener { CafeBar.builder(this@DetailActivity).content("Edit activity")
                    .theme(CafeBarTheme.LIGHT).contentTypeface("OpenSans-Regular.ttf").show()
            }

        }else{
            decideLikes(mUserModel?.uid.toString(), likeDatabaseReference)
            fabClickLike()
        }
    }
    private fun decideLikes(uidUser : String, likesReference : DatabaseReference){
        likeListener = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0 != null && p0.exists()){
                    if(p0.hasChild(uidUser)){
                        fab.setImageDrawable(resources.getDrawable(R.drawable.ic_action_loved))
                        isLiked = true
                    }else{
                        fab.setImageDrawable(resources.getDrawable(R.drawable.ic_action_love_white))
                        isLiked = false
                    }
                }else{
                    fab.setImageDrawable(resources.getDrawable(R.drawable.ic_action_love_white))
                    isLiked = false
                }
            }

        }

        likesReference.child(getKeyPost()).addListenerForSingleValueEvent(likeListener)
    }
    private fun fabClickLike(){
        fab.setOnClickListener {
            var isProgress = true
            likeDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0 != null){
                        if (isProgress) {
                            if(p0.child(getKeyPost()).hasChild(mUserModel?.uid)){
                                likeDatabaseReference.child(getKeyPost()).child(mUserModel?.uid).removeValue()
                                AnotherMethods.counter(mDatabaseRefence.child(getKeyPost()).child("favorite"), false)
                                isLiked = false
                                decideLikes(mUserModel?.uid.toString(), likeDatabaseReference)
                                CafeBar.builder(this@DetailActivity).content("Dihapus dari favorite")
                                        .theme(CafeBarTheme.LIGHT)
                                        .contentTypeface("OpenSans-Regular.ttf").show()
                                isProgress = false
                            }else{
                                likeDatabaseReference.child(getKeyPost()).child(mUserModel?.uid).setValue(true)
                                AnotherMethods.counter(mDatabaseRefence.child(getKeyPost()).child("favorite"), true)
                                isLiked = true
                                decideLikes(mUserModel?.uid.toString(), likeDatabaseReference)
                                CafeBar.builder(this@DetailActivity).content("Favorite")
                                        .theme(CafeBarTheme.LIGHT)
                                        .contentTypeface("OpenSans-Regular.ttf").show()
                                isProgress = false
                            }
                        }
                    }
                }
            })
        }
    }
    override fun onDestroy() {
        mDatabaseRefence.removeEventListener(fotoListener)
        detail_image_slider.stopAutoCycle()
        super.onDestroy()
    }
    private fun initReference(){

        likeDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.LIKES)
        likeDatabaseReference.keepSynced(true)
        mDatabaseRefence = FirebaseDatabase.getInstance().getReference(Constant.POST)

        postListener = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0 != null){
                    if(p0.exists()){
                            mPostModel = p0.getValue(PostModel::class.java)!!
                            caption_detail.text = mPostModel.caption
                            judul_post.text = mPostModel.nama_barang
                            tanggal_post_detail.text = AnotherMethods.getTimeDate(mPostModel.tanggal_post)
                            harga_on_detail.text = "Rp."+mPostModel.harga
                            diminati.text = "Diminati ${mPostModel.favorite} orang"
                            if (!mUserModel?.uid.equals(mPostModel.uid)){
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
                                                if(mUserModel?.uid.equals(uModel.uid)){
                                                    startActivity(Intent(this@DetailActivity, ProfileActivity::class.java))
                                                }else{
                                                    val i = Intent(this@DetailActivity, PersonsActivity::class.java)
                                                    i.putExtra("UIDUSER", uModel.uid)
                                                    startActivity(i)
                                                }
                                            }
                                        }
                                    }
                                }

                            })
                            fabAction()
                        }
                }
            }

        }


        mDatabaseRefence.child(getKeyPost()).addListenerForSingleValueEvent(postListener)


        fotoListener = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                println("Cannot fetch image from server")
            }

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

        }

        mDatabaseRefence.child(getKeyPost()).child("foto").addListenerForSingleValueEvent(fotoListener)

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
            //Toast.makeText(this@DetailActivity, slider.bundle.get("photoUrl").toString(), Toast.LENGTH_SHORT).show()
        }
    }
    override fun onPageScrollStateChanged(state: Int) {
    }
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }
    override fun onPageSelected(position: Int) {
    }



}

