
/*

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class InventoryFragment : Fragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var inventory: Array<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inventory = arguments?.getStringArray("inventory") ?: emptyArray()

        viewPager = view.findViewById(R.id.viewPager)
        pagerAdapter = InventoryPagerAdapter(childFragmentManager, inventory)
        viewPager.adapter = pagerAdapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (position == inventory.size) {
                    findNavController().navigate(R.id.action_inventoryFragment_to_storyFragment)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }
}

class InventoryPagerAdapter(fragmentManager: FragmentManager, private val inventory: Array<String>) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return inventory.size + 1
    }

    override fun getItem(position: Int): Fragment {
        return if (position < inventory.size) {
            ItemFragment.newInstance(inventory[position])
        } else {
            StoryFragment()
        }
    }
}

class ItemFragment : Fragment() {

    companion object {
        private const val ARG_ITEM = "arg_item"

        fun newInstance(item: String): ItemFragment {
            val fragment = ItemFragment()
            val args = Bundle()
            args.putString(ARG_ITEM, item)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = arguments?.getString(ARG_ITEM)
        // item을 사용하여 필요한 작업 수행
    }
}*/