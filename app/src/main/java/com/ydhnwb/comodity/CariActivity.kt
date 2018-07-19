package com.ydhnwb.comodity

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ydhnwb.comodity.Fragment.FragmentBarang
import com.ydhnwb.comodity.Fragment.FragmentUser
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.Utilities.FragmentAdapter

import kotlinx.android.synthetic.main.activity_cari.*
import kotlinx.android.synthetic.main.content_cari.*

class CariActivity : AppCompatActivity() {

    private lateinit var viewPagerAdapter: FragmentAdapter
    private lateinit var fBarang : FragmentBarang
    private lateinit var fUser : FragmentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cari)
        setSupportActionBar(toolbar)
        initFragmentFirst()
        viewPagerAdapter = FragmentAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(
                FragmentBarang(),

                "BARANG")
        viewPagerAdapter.addFragment(FragmentUser(), "PENGGUNA")
        mViewPager.adapter = viewPagerAdapter
        tabs.setupWithViewPager(mViewPager)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }


    private fun initFragmentFirst(){
        fBarang = FragmentBarang()
        fUser = FragmentUser()
    }




}
