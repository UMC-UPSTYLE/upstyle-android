package com.umc.upstyle

import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabPagerAdapter(fragmentActivity: ChatFragment) : FragmentStateAdapter(fragmentActivity) {
//    override fun getItemCount(): Int = 2  // "투표", "코디 요청" 두 개의 탭
//
//    override fun createFragment(position: Int): Fragment {
//        return when (position) {
//            0 -> VoteFragment()  // 첫 번째 탭: 투표
//            1 -> RequestFragment()  // 두 번째 탭: 코디 요청
//            else -> throw IllegalStateException("Unexpected position: $position")
//        }
//    }



    var fragments : ArrayList<Fragment> = ArrayList()

    // 페이지 갯수 설정
    override fun getItemCount(): Int = fragments.size

    // 불러올 Fragment 정의
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
        notifyItemInserted(fragments.size-1)
    }

    @RequiresApi(35)
    fun removeFragment() {
        fragments.removeLast()
        notifyItemRemoved(fragments.size)
    }
}
