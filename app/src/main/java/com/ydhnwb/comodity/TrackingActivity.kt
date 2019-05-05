package com.ydhnwb.comodity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.google.firebase.database.*
import com.ydhnwb.comodity.Model.Tracker
import com.ydhnwb.comodity.Model.TransactionModelRetriever
import com.ydhnwb.comodity.Utilities.Constant

import kotlinx.android.synthetic.main.activity_tracking.*
import kotlinx.android.synthetic.main.content_tracking.*

class TrackingActivity : AppCompatActivity() {

    private lateinit var trackingListener : ChildEventListener
    private var listOfTracker : MutableList<Tracker> = mutableListOf()
    private lateinit var trackingAdapter : TimeLineViewHolder
    private val childDatabaseRefence = FirebaseDatabase.getInstance().getReference(Constant.TRANSACTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        decideButton()
        initTimeLine()
        onAdded()
        barangDiterima()
    }

    private fun getKeyPost() : String{
        return intent.getStringExtra("KEYPOST") ?: throw IllegalArgumentException("Keypost is null")
    }

    private fun initTimeLine(){
        trackingAdapter = TimeLineViewHolder(this@TrackingActivity, listOfTracker)
        rv_track.layoutManager = LinearLayoutManager(this@TrackingActivity) as RecyclerView.LayoutManager?
        rv_track.adapter = trackingAdapter
    }

    private fun onAdded(){
        trackingListener = object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot?) {}
            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                if(p0 != null && p0.exists()){
                    val mBody = p0.getValue(Tracker::class.java)
                    if(mBody != null){
                        listOfTracker.add(mBody)
                        trackingAdapter.notifyDataSetChanged()
                        rv_track.scrollToPosition(listOfTracker.size - 1)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        listOfTracker.clear()
        childDatabaseRefence.child(getKeyPost()).child("tracker").addChildEventListener(trackingListener)
    }

    override fun onStop() {
        super.onStop()
        childDatabaseRefence.child(getKeyPost()).child("tracker").removeEventListener(trackingListener)
    }

    private fun barangDiterima(){
        btn_barang_diterima.setOnClickListener {
            val getDriver = FirebaseDatabase.getInstance().getReference(Constant.TRANSACTION)
            getDriver.child(getKeyPost()).child("tracker").orderByKey().limitToFirst(1).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0 != null && p0.exists()){
                        var tracker : Tracker? = null
                        for(data in p0.children){
                            tracker = data.getValue(Tracker::class.java)

                        }
                        if(tracker != null){
                            val orderRef = FirebaseDatabase.getInstance().getReference("ORDER")
                            orderRef.child(tracker.driver).child(getKeyPost()).removeValue()
                            getDriver.child(getKeyPost()).child("status").setValue("done")
                            Toast.makeText(this@TrackingActivity, "Terkonfirmasi", Toast.LENGTH_SHORT).show()
                            btn_barang_diterima.isEnabled = false
                            btn_barang_diterima.text = "Orderan sudah selesai"

                        }
                    }
                }
            })


        }
    }

    private fun decideButton(){
        val isPembeli = intent.getIntExtra("ISBUYER", 0)
        val trRef = FirebaseDatabase.getInstance().getReference(Constant.TRANSACTION)
        trRef.child(getKeyPost()).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0 != null && p0.exists()){
                    val transaction = p0.getValue(TransactionModelRetriever::class.java)
                    if(transaction != null){
                        if(isPembeli == 0){
                            if(transaction.status.equals("done")){
                                btn_barang_diterima.isEnabled = false
                                btn_barang_diterima.text = "Orderan sudah selesai"
                            }else{
                                btn_barang_diterima.isEnabled = false
                                btn_barang_diterima.text = "Menunggu konfirmasi pembeli"
                            }
                        }else{
                            if(transaction.status.equals("done")){
                                btn_barang_diterima.isEnabled = false
                                btn_barang_diterima.text = "Orderan sudah selesai"
                            }else{
                                btn_barang_diterima.text = "Saya sudah menerima barang"
                            }
                        }

                    }
                }
            }
        })
    }

}
