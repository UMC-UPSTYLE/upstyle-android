package com.umc.upstyle

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.umc.upstyle.databinding.FragmentWebViewBinding

class WebViewFragment : Fragment(R.layout.fragment_web_view) {

    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWebViewBinding.bind(view)

        val url = arguments?.getString("URL") ?: "https://www.google.com" // 기본 URL 설정

        binding.webView.apply {
            settings.javaScriptEnabled = true  // JavaScript 활성화 (필요한 경우)
            settings.domStorageEnabled = true // Notion 같은 사이트는 DOM 저장소 필요
            webViewClient = WebViewClient()    // WebView에서 내부에서 페이지 로딩
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
        }

        binding.webView.webViewClient = WebViewClient() // 내부 WebView에서 열리도록 설정
        binding.webView.loadUrl(url)

        // 뒤로 가기 버튼 클릭 시 WebView에서 뒤로 가도록 설정
        binding.backButton.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()  // WebView에서 뒤로 가기
            } else {
                findNavController().navigateUp() // 뒤로 가기 버튼을 눌렀을 때 뒤로 가기
            }
        }

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()  // WebView에서 뒤로 가기
            } else {
                findNavController().navigateUp() // 뒤로 가기 버튼을 눌렀을 때 뒤로 가기
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
