package com.ydhnwb.comodity.Fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.Model.ImageModel
import com.ydhnwb.comodity.Model.PostModel
import com.ydhnwb.comodity.Model.TransactionModelRetriever
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.R
import com.ydhnwb.comodity.SeeTransactionActivity
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.ViewHolder.TransactionBeliVH
import kotlinx.android.synthetic.main.fragment_transaction_jual.view.*

class FragmentTransactionJual : Fragment() {
    private var mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USER_TRANSACTION)
    private lateinit var fireAdapter : FirebaseRecyclerAdapter<TransactionModelRetriever, TransactionBeliVH>
    private var mPostRef = FirebaseDatabase.getInstance().getReference(Constant.POST)
    private var listOfPhotos : MutableList<ImageModel> = ArrayList()
    private var mUserRef = FirebaseDatabase.getInstance().getReference(Constant.USERS)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_jual, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        view.rv_trans_jual.layoutManager = layoutManager
        val me = FirebaseAuth.getInstance().currentUser
        if(me != null){
            val fo = FirebaseRecyclerOptions.Builder<TransactionModelRetriever>()
                    .setQuery(mDatabaseReference.child("incoming").child(me.uid).orderByChild("date"), TransactionModelRetriever::class.java).build()
            fireAdapter = object : FirebaseRecyclerAdapter<TransactionModelRetriever, TransactionBeliVH>(fo){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionBeliVH {
                    val v = LayoutInflater.from(parent.context).inflate(R.layout.single_list_transaction ,parent,false)
                    return TransactionBeliVH(v, activity!!)
                }

                override fun onBindViewHolder(holder: TransactionBeliVH, position: Int, model: TransactionModelRetriever) {
                    mPostRef.child(model.id_transaction).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {}
                        override fun onDataChange(p0: DataSnapshot?) {
                            if(p0 != null && p0.exists()){
                                val post = p0.getValue(PostModel::class.java)
                                if(post != null){
                                    holder.name.text = post.nama_barang
                                    p0.ref.child("foto").limitToFirst(1).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onCancelled(p0s: DatabaseError?) {}
                                        override fun onDataChange(p0s: DataSnapshot?) {
                                            if(p0s != null && p0s.exists()){
                                                    listOfPhotos.clear()
                                                    for(ds in p0s.children){
                                                        val im = ds.getValue(ImageModel::class.java)
                                                        listOfPhotos.add(im!!)
                                                    }
                                                    try{
                                                        var i = 0
                                                        while (listOfPhotos[i].photosUrl == null && i < listOfPhotos.size){
                                                            i++
                                                        }
                                                        if(listOfPhotos[i].photosUrl != null){
                                                            Glide.with(activity!!.applicationContext).load(listOfPhotos[i].photosUrl)
                                                                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.placeholder))
                                                                    .into(holder.image)
                                                        }else{
                                                            Glide.with(activity!!.applicationContext).load(R.drawable.placeholder)
                                                                    .into(holder.image)
                                                        }
                                                    }catch (e : Exception){
                                                        Glide.with(activity!!.applicationContext).load(R.drawable.no_image)
                                                                .into(holder.image)
                                                        println("Preview Image Exception : " + e.message)
                                                    }
                                                }else{
                                                    Glide.with(activity!!.applicationContext).load(R.drawable.no_image)
                                                            .into(holder.image)
                                                }
                                            }

                                    })
                                    //holder.status.text = model.status
                                }
                            }
                        }
                    })
                    mUserRef.child(model.buyer).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(p0s: DatabaseError?) {}
                        override fun onDataChange(p0s: DataSnapshot?) {
                            if(p0s != null && p0s.exists()){
                                val user = p0s.getValue(UserModel::class.java)
                                if(user != null){
                                    holder.owner.text = user.display_name
                                }
                            }
                        }
                    })

                    holder.setOnItemClickListener(object : MyClickListener {
                        override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                            val i = Intent(activity, SeeTransactionActivity::class.java)
                            i.putExtra("KEYPOST", model.id_transaction)
                            i.putExtra("OWNER", model.owner)
                            i.putExtra("ISBUYER", 0)
                            activity?.startActivity(i)
                        }
                    })
                }
            }
            view.rv_trans_jual.adapter = fireAdapter
            fireAdapter.startListening()

        }
    }
}