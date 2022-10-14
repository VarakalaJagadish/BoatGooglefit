package com.example.boatgooglefit.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.boatgooglefit.fragments.FragmentOne
import com.example.boatgooglefit.fragments.FragmentThree
import com.example.boatgooglefit.fragments.FragmentTwo

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {


    private val COUNT = 3

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FragmentOne()
            1 -> FragmentTwo()
            2 -> FragmentThree()
            else -> FragmentOne()
        }
    }

    override fun getCount(): Int {
        return COUNT
    }
}