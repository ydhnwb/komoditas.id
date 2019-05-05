package com.ydhnwb.comodity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.ydhnwb.comodity.FirebaseMethods.AnotherMethods
import com.ydhnwb.comodity.Model.PostModel
import com.ydhnwb.comodity.Model.TransactionModelUploader
import com.ydhnwb.comodity.Utilities.Constant
import kotlinx.android.synthetic.main.activity_beli.*
import kotlinx.android.synthetic.main.content_beli.*

class BeliActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mPostModel : PostModel
    private var me : FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beli)
        setSupportActionBar(toolbar)
        getMe()
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        getPackage()
    }

    private fun getMe(){
        mAuth = FirebaseAuth.getInstance()
        me = mAuth.currentUser
        if(me != null){
            beliButton()
        }
    }

    private fun getKeyPost() : String{
        return intent.getStringExtra("KEYPOST") ?: throw IllegalArgumentException("Keypost is null") as Throwable
    }

    private fun beliButton(){
        beli.setOnClickListener {
            val mRef = FirebaseDatabase.getInstance().getReference(Constant.TRANSACTION)
            mRef.child(getKeyPost()).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0 != null) {
                        if (p0.exists()) {
                            val builder: AlertDialog.Builder = AlertDialog.Builder(this@BeliActivity)
                            builder.setMessage(R.string.transaksi_ini_sudah_ada).setCancelable(false)
                                    .setPositiveButton(R.string.paham) { dialog, _ -> finish() }
                                    .setNegativeButton(R.string.kembali) { dialog, _ -> dialog?.cancel() }
                            val alertDialog = builder.create()
                            alertDialog.show()
                        } else {
                            val tr = TransactionModelUploader(getKeyPost(), mPostModel.uid, me!!.uid, ServerValue.TIMESTAMP, "waiting", mPostModel.harga.toInt())
                            p0.ref.setValue(tr)
                            AnotherMethods.userTransaction(tr.buyer, tr.id_transaction, tr.owner, tr.price)
                            Toast.makeText(this@BeliActivity, "Berhasil. Cek Transaksi anda", Toast.LENGTH_LONG).show()
                            finish()
                            //update the post status to reserved
                        }
                    }
                }
            })
        }

    }

    private fun getPackage(){
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST)
        mDatabaseReference.child(getKeyPost()).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if((p0 != null) && p0.exists()){
                    mPostModel = p0.getValue(PostModel::class.java)!!
                    beli_deskripsi_barang.text = mPostModel.caption
                    beli_nama_barang.text = mPostModel.nama_barang
                    beli_harga.text = "Rp. "+mPostModel.harga
                }
            }
        })
    }

}
