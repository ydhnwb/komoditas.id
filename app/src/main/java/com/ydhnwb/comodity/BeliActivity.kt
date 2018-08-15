package com.ydhnwb.comodity

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ydhnwb.comodity.Model.PostModel
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.Utilities.Constant
import kotlinx.android.synthetic.main.activity_beli.*
import kotlinx.android.synthetic.main.content_beli.*

class BeliActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mAuthListener : FirebaseAuth.AuthStateListener
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mPostModel : PostModel
    private lateinit var me : UserModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beli)
        setSupportActionBar(toolbar)
        initAuth()
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener {
            val builder : AlertDialog.Builder = AlertDialog.Builder(this@BeliActivity)
            builder.setMessage(R.string.batalkan_transaksi).setCancelable(false)
                    .setPositiveButton(R.string.yakin) { dialog, _ -> finish() }.setNegativeButton(R.string.tetap_disini
                    ) { dialog, _ -> dialog?.cancel() }

            val alertDialog = builder.create()
            alertDialog.show()
        }
        getPackage()
    }

    private fun initAuth(){
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            val i = it.currentUser
            if(i == null){
                finish()
            }else{
                me = UserModel(i.uid,i.displayName.toString(),i.displayName.toString().toLowerCase(), i.email.toString(),i.photoUrl.toString())
            }
        }
    }

    private fun getKeyPost() : String{
        return intent.getStringExtra("KEYPOST") ?: throw IllegalArgumentException("Keypost is null") as Throwable
    }

    private fun getPackage(){
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.POST)
        mDatabaseReference.child(getKeyPost()).addValueEventListener(object : ValueEventListener{
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

    override fun onBackPressed() {
        //super.onBackPressed()
        val builder : AlertDialog.Builder = AlertDialog.Builder(this@BeliActivity)
        builder.setMessage(R.string.batalkan_transaksi).setCancelable(false)
                .setPositiveButton(R.string.yakin) {
                    dialog, _ -> finish() }
                .setNegativeButton(R.string.tetap_disini) {
                    dialog, _ -> dialog?.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
    }
}
