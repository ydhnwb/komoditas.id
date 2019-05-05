package com.ydhnwb.comodity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import com.ydhnwb.comodity.Fragment.FragmentTransactionBeli
import com.ydhnwb.comodity.Fragment.FragmentTransactionJual
import com.ydhnwb.comodity.Utilities.FragmentAdapter
import kotlinx.android.synthetic.main.activity_transaction.*
import kotlinx.android.synthetic.main.content_transaction.*

class TransactionActivity : AppCompatActivity() {
    private lateinit var viewPagerAdapter: FragmentAdapter
    private var fIncoming = FragmentTransactionBeli()
    private var fOutcoming = FragmentTransactionJual()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener { finish() }
        initFragment()
    }

    private fun initFragment(){
        viewPagerAdapter = FragmentAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(fIncoming, "PEMBELIAN")
        viewPagerAdapter.addFragment(fOutcoming, "PENJUALAN")
        mViewPager_transaction.adapter = viewPagerAdapter
        tabs_transaction.setupWithViewPager(mViewPager_transaction)
    }

}
