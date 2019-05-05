package com.ydhnwb.comodity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ydhnwb.comodity.Model.TransactionModelRetriever
import com.ydhnwb.comodity.Utilities.Constant
import kotlinx.android.synthetic.main.activity_see_transaction.*

class SeeTransactionActivity : AppCompatActivity() {

    private var transactionRef = FirebaseDatabase.getInstance().getReference(Constant.TRANSACTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_transaction)
        supportActionBar?.hide()
        //getData()
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    private fun getData(){
        tv_kode_transaksi.text = intent.getStringExtra("KEYPOST")
        val i = intent.getIntExtra("ISBUYER",0)
        if(i == 0){
            lihat_transaksi_penjual.visibility = View.VISIBLE
            lihat_transaksi_pembeli.visibility = View.GONE
            transactionRef.child(intent.getStringExtra("KEYPOST")).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0 != null && p0.exists()){
                        val trans = p0.getValue(TransactionModelRetriever::class.java)
                        if(trans != null){
                            if (p0.hasChild("tracker")){
                                btn_kirim_track.text = "Lihat riwayat pengiriman"
                                btn_kirim_track.setOnClickListener {
                                    //intent to tracker
                                    val mintent = Intent(this@SeeTransactionActivity, TrackingActivity::class.java)
                                    mintent.putExtra("ISBUYER", 0)
                                    mintent.putExtra("KEYPOST", intent.getStringExtra("KEYPOST"))
                                    startActivity(mintent)
                                }
                            }else{
                                btn_kirim_track.text = "Pilih driver"
                                btn_kirim_track.setOnClickListener {
                                    val mintent = Intent(this@SeeTransactionActivity, ChooseDriverActivity::class.java)
                                    mintent.putExtra("KEYPOST", intent.getStringExtra("KEYPOST"))
                                    startActivity(mintent)
                                }
                            }
                        }
                    }
                }

            })
        }else{
            lihat_transaksi_penjual.visibility = View.GONE
            lihat_transaksi_pembeli.visibility = View.VISIBLE
            btn_track.setOnClickListener {
                //intent to see track
                val mintent = Intent(this@SeeTransactionActivity, TrackingActivity::class.java)
                mintent.putExtra("KEYPOST", intent.getStringExtra("KEYPOST"))
                mintent.putExtra("ISBUYER", 1)
                startActivity(mintent)
            }
        }
    }
}
