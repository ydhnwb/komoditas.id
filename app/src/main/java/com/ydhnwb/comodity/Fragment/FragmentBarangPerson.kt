package com.ydhnwb.comodity.Fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.danimahardhika.cafebar.CafeBar
import com.danimahardhika.cafebar.CafeBarTheme
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.like.LikeButton
import com.like.OnLikeListener
import com.ydhnwb.comodity.DetailActivity
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.Model.ImageModel
import com.ydhnwb.comodity.Model.IndividualPostModel
import com.ydhnwb.comodity.Model.PostModel
import com.ydhnwb.comodity.R
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.ViewHolder.SingleListItemPerson
import kotlinx.android.synthetic.main.fragment_person_barang.view.*

class FragmentBarangPerson : Fragment() {

    private lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<IndividualPostModel, SingleListItemPerson>
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var likeDatabaseReference : DatabaseReference
    private lateinit var userDatabaseReference : DatabaseReference
    private lateinit var categoryPost : String
    private var uid = ""
    private var myUid = ""
    private lateinit var iDatabaseReference : DatabaseReference
    private var fotoRef = FirebaseDatabase.getInstance().getReference(Constant.POST)
    private lateinit var fotoListener : ValueEventListener
    private lateinit var mContext: Context
    private var me : FirebaseUser? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_person_barang, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mLayoutManager = GridLayoutManager(activity!!, 2)
        view.rv_person_barang.layoutManager = mLayoutManager
        val animator = view.rv_person_barang.itemAnimator
        var i = 0
        if (arguments != null){
            i = arguments!!.getInt("ACTIVITY_CODE")
            uid = arguments!!.getString("UID")
            myUid = arguments!!.getString("MYUID")
        }

        me = FirebaseAuth.getInstance().currentUser
        if(me != null){
            myUid = me?.uid.toString()
        }

        initFire(i, uid)
        if(animator is SimpleItemAnimator){ animator.supportsChangeAnimations = false }
        val fo : FirebaseRecyclerOptions<IndividualPostModel>
        if (categoryPost.equals("all")){
            fo = FirebaseRecyclerOptions.Builder<IndividualPostModel>()
                    .setQuery(iDatabaseReference, IndividualPostModel::class.java).build()
        }else{
            fo = FirebaseRecyclerOptions.Builder<IndividualPostModel>()
                    .setQuery(iDatabaseReference.orderByChild("tipe_barang").equalTo(categoryPost), IndividualPostModel::class.java).build()
        }

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<IndividualPostModel, SingleListItemPerson>(fo){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleListItemPerson {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_list_barang_person, parent, false)
                return SingleListItemPerson(v, activity!!)
            }

            override fun onBindViewHolder(holder: SingleListItemPerson, position: Int, model: IndividualPostModel) {
                mDatabaseReference.child(getRef(position).key).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0 != null && p0.exists()){
                            val post = p0.getValue(PostModel::class.java)
                            if(post != null){
                                holder.harga.text = post.harga
                                holder.nama_barang.text = post.nama_barang
                                fotoRef.child(getRef(position).key).child("foto").orderByKey().limitToFirst(1).addValueEventListener(object : ValueEventListener{
                                    override fun onCancelled(p0x: DatabaseError?) {}
                                    override fun onDataChange(p0x: DataSnapshot?) {
                                        if(p0x != null && p0x.exists()){
                                            var img : ImageModel? = null
                                            for (child in p0x.children) {
                                                img = child.getValue(ImageModel::class.java)
                                            }
                                            if(img != null){
                                                Glide.with(context!!.applicationContext).load(img.photosUrl.toString()).into(holder.image_barang)
                                            }else{
                                                Glide.with(context!!.applicationContext).load(R.drawable.no_image).into(holder.image_barang)
                                            }
                                        }else{
                                            Glide.with(context!!.applicationContext).load(R.drawable.no_image).into(holder.image_barang)
                                        }
                                    }
                                })

                                holder.decideLikes(getRef(position).key)
                                holder.like_button.setOnLikeListener(object : OnLikeListener {
                                    override fun unLiked(p0: LikeButton?) {
                                        var isProgress = true
                                        likeDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
                                            override fun onCancelled(p0: DatabaseError?) {}
                                            override fun onDataChange(p0: DataSnapshot?) {
                                                if(p0 != null){
                                                    if (isProgress) {
                                                        if(p0.child(getRef(position).key).hasChild(myUid)){
                                                            likeDatabaseReference.child(getRef(position).key).child(myUid).removeValue()
                                                            AnotherMethods.counter(mDatabaseReference.child(getRef(position).key).child("favorite"), false)
                                                            isProgress = false
                                                            CafeBar.builder(activity!!).content("Dihapus dari favorite")
                                                                    .theme(CafeBarTheme.LIGHT)
                                                                    .contentTypeface("OpenSans-Regular.ttf").show()
                                                        }
                                                    }
                                                }
                                            }
                                        })
                                    }
                                    override fun liked(p0: LikeButton?) {
                                        var isProgress = true
                                        likeDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
                                            override fun onCancelled(p0: DatabaseError?) {}
                                            override fun onDataChange(p0: DataSnapshot?) {
                                                if(p0 != null){
                                                    if (isProgress) {
                                                        if(!p0.child(getRef(position).key).hasChild(myUid)){
                                                            likeDatabaseReference.child(getRef(position).key).child(myUid).setValue(true)
                                                            AnotherMethods.counter(mDatabaseReference.child(getRef(position).key).child("favorite"), true)
                                                            isProgress = false
                                                            CafeBar.builder(activity!!).content("Favorite")
                                                                    .theme(CafeBarTheme.LIGHT)
                                                                    .contentTypeface("OpenSans-Regular.ttf").show()
                                                        }
                                                    }
                                                }
                                            }
                                        })
                                    }
                            })
                                holder.setOnItemClickListener(object : MyClickListener{
                                    override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                                        val intent = Intent(activity, DetailActivity::class.java)
                                        intent.putExtra("KEYPOST", getRef(position).key)
                                        startActivity(intent)
                                    }
                                })
                        }
                    }
                }
                })
            }
        }

        firebaseRecyclerAdapter.startListening()
        view.rv_person_barang.adapter = firebaseRecyclerAdapter
    }

    private fun initFire(codeActivity : Int, me : String){
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        iDatabaseReference = mFirebaseDatabase.getReference(Constant.INDIVIDUAL_POST).child(me)
        userDatabaseReference = mFirebaseDatabase.getReference(Constant.USERS)
        likeDatabaseReference = mFirebaseDatabase.getReference(Constant.LIKES)
        mDatabaseReference = mFirebaseDatabase.getReference(Constant.POST)

        when(codeActivity){
            1 -> {categoryPost = Constant.BENIH_REF.toLowerCase()}
            2 -> {categoryPost = Constant.PADI_REF.toLowerCase() }
            3 -> {categoryPost = Constant.PUPUK_REF.toLowerCase()}
            4 -> {categoryPost = Constant.TERNAK_REF.toLowerCase() }
            else -> { categoryPost = "all" }
        }
    }

}