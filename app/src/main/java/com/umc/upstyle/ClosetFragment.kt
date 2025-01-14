package com.umc.upstyle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.umc.upstyle.databinding.ActivityClosetBinding

class ClosetFragment : Fragment() {
    private var _binding: ActivityClosetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityClosetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack() // 이전 Fragment로 이동
        }

        binding.btnGoOuter.setOnClickListener {
            val fragment = ClosetItemFragment.newInstance("OUTER") // 카테고리 전달
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnGoTop.setOnClickListener {
            val fragment = ClosetItemFragment.newInstance("TOP") // 카테고리 전달
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnGoBottom.setOnClickListener {
            val fragment = ClosetItemFragment.newInstance("BOTTOM") // 카테고리 전달
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnGoShoes.setOnClickListener {
            val fragment = ClosetItemFragment.newInstance("SHOES") // 카테고리 전달
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnGoBag.setOnClickListener {
            val fragment = ClosetItemFragment.newInstance("BAG") // 카테고리 전달
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnGoOther.setOnClickListener {
            val fragment = ClosetItemFragment.newInstance("OTHER") // 카테고리 전달
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ViewBinding 해제
    }
}
