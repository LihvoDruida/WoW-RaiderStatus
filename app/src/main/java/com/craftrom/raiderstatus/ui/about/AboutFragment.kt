package com.craftrom.raiderstatus.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.craftrom.raiderstatus.BuildConfig
import com.craftrom.raiderstatus.MainActivity
import com.craftrom.raiderstatus.R

class AboutFragment : Fragment() {
    private lateinit var versionApp: TextView
    private var _binding: View? = null
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
        _binding = inflater.inflate(R.layout.fragment_about, container, false)
        val mainActivity = requireActivity() as MainActivity
        mainActivity.setToolbarText(getTitle(), getSubtitle())

        versionApp = binding.findViewById(R.id.version)

        val versionName = BuildConfig.VERSION_NAME
        versionApp.text = versionName

        return binding
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
