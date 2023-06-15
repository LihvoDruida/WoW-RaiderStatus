package com.craftrom.raiderstatus.ui.about

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.craftrom.raiderstatus.BuildConfig
import com.craftrom.raiderstatus.ItemWebViewActivity
import com.craftrom.raiderstatus.MainActivity
import com.craftrom.raiderstatus.R
import com.craftrom.raiderstatus.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    private lateinit var versionApp: TextView
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()
        val mainActivity = requireActivity() as MainActivity
        mainActivity.setToolbarText(getTitle(), getSubtitle())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val mainActivity = requireActivity() as MainActivity
        mainActivity.setToolbarText(getTitle(), getSubtitle())

        versionApp = binding.version

        val versionName = BuildConfig.VERSION_NAME
        versionApp.text = versionName

        binding.aboutDonate.setOnClickListener {
            openDonate()
        }

        return binding.root
    }

    private fun openDonate() {
        val uri = "https://lihvodruida.github.io/donate"
        val intent = Intent(context, ItemWebViewActivity::class.java).apply {
            putExtra("feedItemUrl", uri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getTitle(): String {
        return getString(R.string.title_about)
    }

    private fun getSubtitle(): String {
        return getString(R.string.subtitle_about)
    }
}
