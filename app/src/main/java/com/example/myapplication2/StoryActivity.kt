package com.example.myapplication2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import me.relex.circleindicator.CircleIndicator3


class StoryActivity : AppCompatActivity(), StoryFragmentListener {



    private lateinit var storyFragment: StoryFragment

    private lateinit var viewPager: ViewPager2
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var circleIndicator3: CircleIndicator3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.story_activity)

        viewPager = findViewById(R.id.viewpager)

        storyFragment = StoryFragment()
        storyFragment.setListener(this)

        storyAdapter = StoryAdapter(supportFragmentManager, lifecycle)
        storyAdapter.addFragment(ImgGenFragment())
        storyAdapter.addFragment(StoryFragment())
        storyAdapter.addFragment(ChoiceFragment())
        viewPager.adapter = storyAdapter
        circleIndicator3 = findViewById(R.id.indicator)
        circleIndicator3.setViewPager(viewPager)
        circleIndicator3.createIndicators(3,0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        // 선택한 페이지를 설정하도록 변경
                        viewPager.setCurrentItem(0, false)
                    }
                    1 -> {
                        viewPager.setCurrentItem(1, false)
                    }
                    2 -> {
                        viewPager.setCurrentItem(2, false)
                    }
                }
            }
        })
        viewPager.setCurrentItem(1, false)
    }

    fun getViewPager(): ViewPager2 {
        return viewPager
    }

    fun receiveData(data: String) {
        storyFragment.setData(data)
    }


    fun getStoryAdapter(): StoryAdapter {
        return storyAdapter
    }

    override fun initializeAndNavigateToStoryFragment() {
        val imggenFragment = storyAdapter.getImgGenFragment()
        val choiceFragment = storyAdapter.getChoiceFragment()

        // 클래스 초기화
        imggenFragment?.reset()
        choiceFragment?.reset()

        // 뷰페이지 이동
        viewPager.setCurrentItem(1, false)
    }

    class StoryAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {
        val fragmentList: MutableList<Fragment> = ArrayList()

        fun addFragment(fragment: Fragment) {
            fragmentList.add(fragment)
        }

        fun getStoryFragment(): StoryFragment? {
            return fragmentList[1] as? StoryFragment
        }

        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }

        fun getImgGenFragment(): ImgGenFragment? {
            return fragmentList[0] as? ImgGenFragment
        }

        fun getChoiceFragment(): ChoiceFragment? {
            return fragmentList[2] as? ChoiceFragment
        }
    }


}