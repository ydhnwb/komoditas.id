package com.ydhnwb.comodity.Utilities

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class FragmentAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm){


    private val listFragment = ArrayList<Fragment>()
    private val listTitles = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
        return listFragment.get(position)
    }

    override fun getCount(): Int {
        return listTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return listTitles.get(position)
    }

    fun addFragment(fragment: Fragment, title: String){
        listFragment.add(fragment)
        listTitles.add(title)
    }


}