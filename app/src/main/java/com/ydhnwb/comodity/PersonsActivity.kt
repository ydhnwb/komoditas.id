package com.ydhnwb.comodity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ydhnwb.comodity.Fragment.FragmentBarangPerson
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.Utilities.FragmentAdapter
import kotlinx.android.synthetic.main.activity_persons.*
import kotlinx.android.synthetic.main.content_persons.*

class PersonsActivity : AppCompatActivity() {


    private var fragments = FragmentBarangPerson()
    private var fragmentsBenih = FragmentBarangPerson()
    private var fragmentsPanen = FragmentBarangPerson()
    private var fragmentsTernak = FragmentBarangPerson()
    private var fragmentsPupuk = FragmentBarangPerson()
    private lateinit var fragmentAdapter : FragmentAdapter
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mAuthStateList: FirebaseAuth.AuthStateListener
    private var mUserModel : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_persons)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener {
            this.finish()
        }
        toolbar_layout.title = " "
        fetchMe()
    }


    private fun initAppBar(nama : String) {
        app_bar.addOnOffsetChangedListener { _, verticalOffset ->
            val isShow = false
            var scrollRange = -1
            if (scrollRange == -1) {
                scrollRange = app_bar.totalScrollRange
            }

            when {
                scrollRange + verticalOffset == 0 -> {
                    toolbar_layout.title = nama
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

    private fun initFragment(uid : String){
        val b = Bundle()
        b.putInt("ACTIVITY_CODE", 0)
        b.putString("UID", getUidUser())
        b.putString("MYUID", uid)
        initParams(uid)
        fragmentAdapter = FragmentAdapter(supportFragmentManager)
        fragments.arguments = b
        fragmentAdapter.addFragment(fragments, "SEMUA")
        fragmentAdapter.addFragment(fragmentsPanen, Constant.PADI_REF)
        fragmentAdapter.addFragment(fragmentsBenih, Constant.BENIH_REF)
        fragmentAdapter.addFragment(fragmentsTernak, Constant.TERNAK_REF)
        fragmentAdapter.addFragment(fragmentsPupuk, Constant.PUPUK_REF)
        mViewPager_person.adapter = fragmentAdapter
        tabs.setupWithViewPager(mViewPager_person)

    }

    private fun initParams(uid : String){
        val benihBundle = Bundle()
        benihBundle.putInt("ACTIVITY_CODE", 1)
        benihBundle.putString("UID", getUidUser())
        benihBundle.putString("MYUID", uid)
        fragmentsBenih.arguments = benihBundle

        val panenBundle = Bundle()
        panenBundle.putInt("ACTIVITY_CODE", 2)
        panenBundle.putString("UID", getUidUser())
        panenBundle.putString("MYUID", uid)
        fragmentsPanen.arguments = panenBundle

        val pupukBundle = Bundle()
        pupukBundle.putInt("ACTIVITY_CODE", 3)
        pupukBundle.putString("UID", getUidUser())
        pupukBundle.putString("MYUID", uid)
        fragmentsPupuk.arguments = pupukBundle

        val ternakBundle = Bundle()
        ternakBundle.putInt("ACTIVITY_CODE", 4)
        ternakBundle.putString("UID", getUidUser())
        ternakBundle.putString("MYUID", uid)
        fragmentsTernak.arguments = ternakBundle
    }


    private fun getUidUser() : String{
        return intent.getStringExtra("UIDUSER") ?: throw IllegalArgumentException("Keypost is null") as Throwable
    }

    private fun fetchMe(){
        mAuth = FirebaseAuth.getInstance()
        mUserModel = mAuth.currentUser
        if(mUserModel != null){
            fetchUser()
            initFragment(mUserModel!!.uid)
            sendMessage()
        }
        /*mAuthStateList = FirebaseAuth.AuthStateListener {
            val i = it.currentUser
            if(i != null){
                mUserModel = UserModel(i.uid, i.displayName.toString(), i.displayName.toString().toLowerCase(), i.email.toString(),
                        i.photoUrl.toString())
                fetchUser()
                initFragment(mUserModel.uid)
                sendMessage()
            }
        }
        mAuth.addAuthStateListener(mAuthStateList)*/
    }

    private fun sendMessage() {
        person_kirim_pesan.setOnClickListener {
            val c = Intent(this@PersonsActivity, ChattingActivity::class.java)
            c.putExtra("KEYPOST", getUidUser())
            startActivity(c)
        }
    }

    private fun fetchUser(){
        val uRef = FirebaseDatabase.getInstance().getReference(Constant.USERS).child(getUidUser())
        uRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0 != null && p0.exists()){
                    val user = p0.getValue(UserModel::class.java)
                    if(user != null){
                        Glide.with(applicationContext).load(user.url_photo).into(
                                person_photo_profile)
                        person_display_name.text = user.display_name
                        initAppBar(user.display_name)
                    }
                }
            }
        })
    }


}
