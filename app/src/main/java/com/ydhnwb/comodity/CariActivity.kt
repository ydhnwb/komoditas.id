package com.ydhnwb.comodity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.ydhnwb.comodity.Fragment.FragmentBarang
import com.ydhnwb.comodity.Fragment.FragmentUser
import com.ydhnwb.comodity.Utilities.FragmentAdapter
import kotlinx.android.synthetic.main.activity_cari.*
import kotlinx.android.synthetic.main.content_cari.*

class CariActivity : AppCompatActivity() {

    private lateinit var viewPagerAdapter: FragmentAdapter
    private var fBarang : FragmentBarang = FragmentBarang()
    private var fUser : FragmentUser = FragmentUser()
    private lateinit var query : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cari)
        setSupportActionBar(toolbar)
        getQueryFromHome()
        setSearchPlaceholder(activityBefore())
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        onSearch()
    }




    private fun initFragmentFirst(querySearch : String, activityCode : Int){
        val b = Bundle()
        b.putString("QUERY_SEARCH", querySearch)
        b.putInt("ACTIVITY_CODE", activityCode)
        fBarang.arguments = b
        fUser.arguments = b
        fBarang.arguments = b
        fUser.arguments = b
        viewPagerAdapter = FragmentAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(fBarang, "BARANG")
        viewPagerAdapter.addFragment(fUser, "PENGGUNA")
        mViewPager.adapter = viewPagerAdapter
        tabs.setupWithViewPager(mViewPager)
    }

    private fun onSearch(){
        search.setOnSearchListener(object : FloatingSearchView.OnSearchListener{
            override fun onSearchAction(currentQuery: String?) {
                if(currentQuery.toString().trim() != ""){
                    initFragmentFirst(currentQuery.toString(), activityBefore())
                }
            }
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion?) {}
        })

        search.setOnHomeActionClickListener { finish() }
    }

    private fun getQueryFromHome(){
        query = intent.getStringExtra("QUERY_SEARCH") ?: throw IllegalArgumentException("Query search is null")
        if(activityBefore() == 0){
            search.setSearchText(query)
            initFragmentFirst(query, activityBefore())
        }else{
            initFragmentFirst(query, activityBefore())
        }
    }

    private fun activityBefore() : Int{
        return intent.getIntExtra("FROM_ACTIVITY", 0)
    }

    private fun setSearchPlaceholder(activityCode: Int){
        when(activityCode){
            1 -> { search.setSearchHint("Cari benih terbaik") }
            2 -> { search.setSearchHint("Cari hasil panen") }
            3 -> { search.setSearchHint("Cari pupuk") }
            4 -> { search.setSearchHint("Cari hasil ternak") }
            else -> { search.setSearchHint("Cari di Komoditas.id") }
        }
    }

}
