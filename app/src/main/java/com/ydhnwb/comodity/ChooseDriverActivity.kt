package com.ydhnwb.comodity

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ydhnwb.comodity.Interfaces.MyClickListener
import com.ydhnwb.comodity.Model.Driver
import com.ydhnwb.comodity.Model.OrderModel
import com.ydhnwb.comodity.Model.Tracker
import com.ydhnwb.comodity.Model.TransactionModelRetriever
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.ViewHolder.ChooseDriverVH

import kotlinx.android.synthetic.main.activity_choose_driver.*
import kotlinx.android.synthetic.main.content_choose_driver.*

class ChooseDriverActivity : AppCompatActivity() {

    private var fUser : FirebaseUser? = null
    private lateinit var fireAdapter : FirebaseRecyclerAdapter<Driver, ChooseDriverVH>
    private var transaction : TransactionModelRetriever? = null
    private var oRef = FirebaseDatabase.getInstance().getReference("ORDER")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_driver)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener { finish() }
        fetchMe()
    }

    private fun fetchDrivers(transaction : TransactionModelRetriever){
        val driverRef = FirebaseDatabase.getInstance().getReference(Constant.DRIVERS)
        Toast.makeText(this@ChooseDriverActivity, "TRID ${transaction.id_transaction}", Toast.LENGTH_SHORT).show()
        val fo = FirebaseRecyclerOptions.Builder<Driver>()
                .setQuery(driverRef.orderByChild("name"), Driver::class.java).build()

        fireAdapter = object : FirebaseRecyclerAdapter<Driver, ChooseDriverVH>(fo){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseDriverVH {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.single_list_choose_driver ,parent,false)
                return ChooseDriverVH(v, this@ChooseDriverActivity)

            }

            override fun onBindViewHolder(holder: ChooseDriverVH, position: Int, model: Driver) {
                holder.name.text = model.name
                holder.phone.text = model.phone
                Glide.with(this@ChooseDriverActivity.applicationContext).load(model.photo).into(holder.photo)

                holder.setOnItemClickListener(object : MyClickListener{
                    override fun onClick(v: View, position: Int, isLongClick: Boolean) {
                        oRef.child(model.uid).addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {}
                            override fun onDataChange(p0: DataSnapshot?) {
                                if(p0 != null && p0.exists()){
                                    holder.status.text = "Tidak tersedia"
                                    Toast.makeText(this@ChooseDriverActivity, "Jasa ekspedisi ini tidak tersedia", Toast.LENGTH_SHORT).show()
                                }else{
                                    holder.status.text = "Ada"
                                    val mRef = FirebaseDatabase.getInstance().getReference(Constant.TRANSACTION)
                                    //todo lat long default user location
                                    val tracker : Tracker = Tracker(0.0, 0.0, model.uid)
                                    mRef.child(transaction.id_transaction).child("tracker").push().setValue(tracker)
                                    val driverReference = FirebaseDatabase.getInstance().getReference("ORDER")
                                    val ord = OrderModel(transaction.id_transaction, true)
                                    driverReference.child(tracker.driver).child(transaction.id_transaction).setValue(ord)
                                    Toast.makeText(this@ChooseDriverActivity, "${model.name} dipilih", Toast.LENGTH_LONG).show()
                                    finish()
                                }
                            }
                        })

                    }
                })


            }
        }
        rv_choose_driver.adapter = fireAdapter
        fireAdapter.startListening()

    }

    private fun fetchMe(){
        val layoutManager = LinearLayoutManager(this@ChooseDriverActivity)
        rv_choose_driver.layoutManager = layoutManager
        fUser = FirebaseAuth.getInstance().currentUser
        if(fUser != null){
            getData()
        }
    }

    private fun getData(){
        val transaction_id : String = intent.getStringExtra("KEYPOST")
        Toast.makeText(this@ChooseDriverActivity, "TRANSACTION ID ${transaction_id}", Toast.LENGTH_SHORT).show()
        val mRef = FirebaseDatabase.getInstance().getReference(Constant.TRANSACTION)
        mRef.child(transaction_id).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0 != null && p0.exists()){
                    val tr = p0.getValue(TransactionModelRetriever::class.java)
                    if(tr != null){
                        fetchDrivers(tr)
                    }
                }
            }
        })
    }

}
