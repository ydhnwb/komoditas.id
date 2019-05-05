package com.ydhnwb.comodity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.JobIntentService
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.ViewHolder.SingleListMainViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.welcome_card.*
import com.google.firebase.database.DataSnapshot
import android.support.v7.widget.SimpleItemAnimator
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.danimahardhika.cafebar.CafeBar
import com.danimahardhika.cafebar.CafeBarTheme
import com.like.LikeButton
import com.like.OnLikeListener
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.Model.*
import com.ydhnwb.comodity.Services.UploadWorker
import com.ydhnwb.comodity.Utilities.*
import kotlinx.android.synthetic.main.bs_upload.*
import kotlinx.android.synthetic.main.filler_upload.*

class MainActivity : AppCompatActivity() , RecyclerItemListener {
    private lateinit var listOfPhotos : MutableList<ImageModel>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var user : UserModel
    private lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<PostModel,SingleListMainViewHolder>
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var likeDatabaseReference : DatabaseReference
    private lateinit var bm : BottomSheetBehavior<*>
    //upload
    private lateinit var rootCoordinatorLayout: CoordinatorLayout
    private lateinit var list: MutableList<PopulateImageModel>
    private lateinit var adapterVH: PopulateImagesViewHolder


    companion object {
        private var calledAlready : Boolean = false
        private var checkingBack = false
        private var bottomSheetIsOpen = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar.title = " "
        initReference()
        search_onSA.setShowSearchKey(true)
        setupBottomSheet()
        getData()
        onSearchPressed()
        initComponent()
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
        fab.setOnClickListener {
            //startActivity(Intent(this@MainActivity, UploadActivity::class.java))
            if(bm.state == BottomSheetBehavior.STATE_HIDDEN){
                bm.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetIsOpen = true
            }else{
                bm.state = BottomSheetBehavior.STATE_HIDDEN
                bottomSheetIsOpen = false
            }

        }
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
        when (item.itemId) {
            R.id.action_settings ->{
                Toast.makeText(this@MainActivity, "Settings", Toast.LENGTH_SHORT).show()
                return true
            }

            R.id.action_logout -> {
                mAuth.signOut()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun initFirebase(){
        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener { it ->
            val firebaseUser = it.currentUser
            if(firebaseUser != null){
                user = UserModel(firebaseUser.uid,firebaseUser.displayName.toString(),
                        firebaseUser.displayName.toString().toLowerCase(),
                        firebaseUser.email.toString(),firebaseUser.photoUrl.toString())

                showHelpSwipe()
                textWatcherCaption()
                addPhotos()
                cardAction()
                push_post.setOnClickListener {view ->
                    pushPost()
                }
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
                fDatabaseReference.child(uid).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            val u : UserModel? = p0.getValue(UserModel::class.java)
                            if(u != null){
                                holder.displayName.text = u.display_name
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
                        likeDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
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
                        likeDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
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
                        tDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
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

    override fun onBackPressed() {
        super.onBackPressed()
        if(bm.state == BottomSheetBehavior.STATE_EXPANDED){
            bm.state = BottomSheetBehavior.STATE_HIDDEN
        }else{
            finish()
        }
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

    //upload
    private fun setupBottomSheet(){
        bm = BottomSheetBehavior.from(bottom_sheet_main)
        bm.state = BottomSheetBehavior.STATE_HIDDEN
        click_bs.setOnClickListener {
            if(bm.state == BottomSheetBehavior.STATE_HIDDEN){
                bm.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetIsOpen = true
            }else{
                bm.state = BottomSheetBehavior.STATE_HIDDEN
                bottomSheetIsOpen = false
            }
        }
        bm.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        helper_text.text = "Usap ke atas untuk membuka"
                        bm.state = BottomSheetBehavior.STATE_HIDDEN
                        bottomSheetIsOpen = false
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        helper_text.text = "...."
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        helper_text.text = "Geser ke bawah untuk menutup"
                        bottomSheetIsOpen = true
                    }
                }
            }
        })

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
                                val finalName : String = AnotherMethods.getFileName(uriFile, this@MainActivity).toString()
                                val p = PopulateImageModel(finalName,uriFile,Constant.STATUS)
                                list.add(p)
                                i++
                            }
                        }
                        data.data != null -> {
                            val uriFile : Uri = data.data
                            val finalName : String = AnotherMethods.getFileName(uriFile, this@MainActivity).toString()
                            val p = PopulateImageModel(finalName,uriFile,Constant.STATUS)
                            list.add(p)
                        }
                        else -> Toast.makeText(this@MainActivity, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
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
            val i : PopulateImageModel = list[viewHolder.adapterPosition]
            val delete = viewHolder.adapterPosition
            adapterVH.removeItem(delete)
            val snack = Snackbar.make(rootCoordinatorLayout,"$s dihapus", Snackbar.LENGTH_SHORT).setAction(R.string.batal) {
                adapterVH.undoDeleteItem(i,delete)
                showHelpSwipe()
            }
            snack.setActionTextColor(Color.RED)
            snack.show()
            showHelpSwipe()
        }
    }

    private fun showHelpSwipe(){
        if(list.isEmpty()){
            swipe_left_help.visibility = View.GONE
        }else{
            swipe_left_help.visibility = View.VISIBLE
        }
    }

    private fun textWatcherCaption(){
        caption_post.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                try {
                    val n = judul_post.text.trim().toString()
                    val h = harga_awal.text.trim().toString()
                    isAcceptable(n,h,s.toString())
                }catch (e:Exception){
                    push_post.isEnabled = false
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
                    isAcceptable(c,h,s.toString())
                }catch (e:Exception){
                    push_post.isEnabled = false

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        harga_awal.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val j = judul_post.text.trim().toString()
                val c = caption_post.text.trim().toString()
                try{
                    isAcceptable(j,c,s.toString())
                }catch (e:Exception){
                    push_post.isEnabled = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun pushPost(){
        try{
            val captions : String = caption_post.text.toString()
            val uid = user.uid.toString()
            val harga = harga_awal.text.toString()
            val namabrg = judul_post.text.toString()
            if(!captions.isEmpty() && !uid.isEmpty() && !harga.isEmpty() && !namabrg.isEmpty() && list.size != 0){
                val individualReference = FirebaseDatabase.getInstance().getReference(Constant.INDIVIDUAL_POST).child(uid)
                val categorizedPost = FirebaseDatabase.getInstance().getReference(Constant.CATEGORIZED_POST).child(tipe_barang.selectedItem.toString())
                val uploadModel = UploadPostModel(uid, ServerValue.TIMESTAMP, captions, harga, namabrg, namabrg.toLowerCase(),
                        tipe_barang.selectedItem.toString().toLowerCase(), 0,
                        tipe_barang.selectedItem.toString().toLowerCase()+"_"+namabrg.toLowerCase())
                val generatedKey = mDatabaseReference.push().key
                mDatabaseReference.child(generatedKey).setValue(uploadModel)
                val ind = IndividualPostModel(generatedKey, uid, tipe_barang.selectedItem.toString().toLowerCase().toLowerCase())
                individualReference.child(generatedKey).setValue(ind)
                categorizedPost.push().setValue(ind)
                uploadBackground(generatedKey)
                harga_awal.text.clear()
                judul_post.text.clear()
                caption_post.text.clear()
                //list.clear()

                bm.state = BottomSheetBehavior.STATE_HIDDEN
            }else{
                Toast.makeText(this@MainActivity, "Please fill all forms", Toast.LENGTH_SHORT).show()
                //Snackbar.make(rootCoordinatorLayout, R.string.harap_isi_semua_form, Snackbar.LENGTH_LONG).show()
            }
        }catch (e:Exception){
            println("pushPost : ${e.message}")
        }
    }

    private fun uploadBackground(generatedKey : String){
        try{
            var i = 0
            while(i < list.size){
                val intent = Intent()
                val b = Bundle()
                //b.putParcelableArrayList("LIST", list as java.util.ArrayList<out Parcelable>)
                b.putString("EMAIL", user.email)
                b.putString("GENERATED_KEY", generatedKey)
                b.putString("FILENAME", list[i].fileName)
                b.putString("FILEPATH", list[i].filePath.toString())
                intent.putExtra("DATA", b)
                JobIntentService.enqueueWork(this@MainActivity, UploadWorker::class.java, UploadWorker.JOB_ID, intent)
                i++
            }
            println("Proses S Executed")

        }catch(e:Exception){
            Log.d("CATCHSome", e.message)
            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            println("CATCHSOME $e.message")
        }

    }

    private fun initComponent(){
        rootCoordinatorLayout = findViewById(R.id.root_mainActivityLayout)
        list = ArrayList()
        adapterVH = PopulateImagesViewHolder(this@MainActivity, list)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        list_photos_catatan.layoutManager = mLayoutManager
        list_photos_catatan.isNestedScrollingEnabled = false
        list_photos_catatan.itemAnimator = DefaultItemAnimator()
        val c = TouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(c).attachToRecyclerView(list_photos_catatan)
    }

    private fun addPhotos() {
        add_photos.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            startActivityForResult(Intent.createChooser(intent, "Select picture"), Constant.REQUEST_CODE_IMAGE)
        }
    }
    private fun isAcceptable(judul : String, harga : String, watcher : String){
        push_post.isEnabled = watcher.length > 0 && (list.size > 0) && !judul.isEmpty() && !harga.isEmpty()
    }

    override fun onResume() {
        super.onResume()
        if(bottomSheetIsOpen){
            bm.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

}