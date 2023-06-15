package com.craftrom.raiderstatus.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.craftrom.raiderstatus.MainActivity
import com.craftrom.raiderstatus.R
import com.craftrom.raiderstatus.core.Affix
import com.craftrom.raiderstatus.core.HorizontalSpaceItemDecoration
import com.craftrom.raiderstatus.core.PeriodsResponse
import com.craftrom.raiderstatus.databinding.FragmentHomeBinding
import com.craftrom.raiderstatus.utils.AffixFetcher
import com.craftrom.raiderstatus.utils.Constants
import com.craftrom.raiderstatus.utils.PeriodsFetcher
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var affixAdapter: AffixAdapter
    private lateinit var countdownTimer: CountDownTimer

    private lateinit var sharedPreferences: SharedPreferences

    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true)
    }

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.homeDeviceWrapper.recyclerView.layoutManager = layoutManager

        val horizontalSpacing = resources.getDimensionPixelSize(R.dimen.margin_generic)
        binding.homeDeviceWrapper.recyclerView.addItemDecoration(
            HorizontalSpaceItemDecoration(
                horizontalSpacing
            )
        )

        val mainActivity = requireActivity() as MainActivity
        mainActivity.setToolbarText(getTitle(), getSubtitle())
        binding.homeDeviceWrapper.homeMythicTitle.text = getString(R.string.mythic_plus)
        binding.homeRebootWrapper.homeRebootTitle.text = getString(R.string.home_reboot)
        val internetAvailable = Constants.isInternetAvailable(requireContext())
        sharedPreferences =
            requireContext().getSharedPreferences("ApiData", Context.MODE_PRIVATE)

        val savedAffixesJson = sharedPreferences.getString("affixes", null)
        if (internetAvailable) {
            fetchAffixes()
            if (savedAffixesJson != null) {
                val affixesType: Type = object : TypeToken<List<Affix>>() {}.type
                val savedAffixes: List<Affix> = Gson().fromJson(savedAffixesJson, affixesType)
                showAffixes(savedAffixes)
            }
            fetchPeriods()
        } else {
            if (savedAffixesJson != null) {
                val affixesType: Type = object : TypeToken<List<Affix>>() {}.type
                val savedAffixes: List<Affix> = Gson().fromJson(savedAffixesJson, affixesType)
                showAffixes(savedAffixes)
            }
            loadSavedPeriods()
        }

        return view
    }

    private fun fetchAffixes() {
        val affixFetcher = AffixFetcher { affixResponse ->
            requireActivity().runOnUiThread {
                showAffixes(affixResponse?.affix_details ?: emptyList())
                saveAffixes(affixResponse?.affix_details ?: emptyList())
            }
        }
        affixFetcher.fetchAffixes()
    }

    private fun showAffixes(affixes: List<Affix>) {
        affixAdapter = AffixAdapter(affixes)
        binding.homeDeviceWrapper.recyclerView.adapter = affixAdapter
    }

    private fun saveAffixes(affixes: List<Affix>) {
        val affixesJson = Gson().toJson(affixes)
        sharedPreferences.edit().putString("affixes", affixesJson).apply()
    }

    private fun fetchPeriods() {
        val periodsFetcher = PeriodsFetcher { periodsResponse ->
            requireActivity().runOnUiThread {
                handlePeriodsData(periodsResponse)
            }
        }
        periodsFetcher.fetchPeriods()
    }

    private fun handlePeriodsData(periodsResponse: PeriodsResponse?) {
        val euPeriod = periodsResponse?.periods?.find { it.region == Constants.DEFAULT_REGION }
        val currentPeriod = euPeriod?.current
        val endTime = currentPeriod?.end

        if (endTime != null) {
            val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val endDate = dateFormat.parse(endTime)

            val userTimeZoneFormat =
                SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault())
            userTimeZoneFormat.timeZone =
                TimeZone.getDefault() // Use the user's time zone
            val userDate = userTimeZoneFormat.format(endDate as Date)

            val userEndDate = userTimeZoneFormat.parse(userDate)

            val userDateFormat =
                SimpleDateFormat(Constants.DATE_FORMAT_USER, Locale.getDefault())
            userDateFormat.timeZone =
                TimeZone.getDefault() // Use the user's time zone
            val restartDateText = userDateFormat.format(endDate)

            val currentTime = Calendar.getInstance().time
            val diffInMillis = userEndDate!!.time - currentTime.time

            binding.homeRebootWrapper.homeRebootNextTime.text = restartDateText
            startCountdownTimer(diffInMillis)

            savePeriods(periodsResponse) // Save periods data
        } else {
            loadSavedPeriods() // Display saved periods data if API data is not available
        }
    }

    private fun loadSavedPeriods() {
        val savedPeriodsJson = sharedPreferences.getString("periods", null)
        if (savedPeriodsJson != null) {
            val periodsType: Type = object : TypeToken<PeriodsResponse>() {}.type
            val savedPeriods: PeriodsResponse = Gson().fromJson(savedPeriodsJson, periodsType)
            handlePeriodsData(savedPeriods)
        }
    }

    private fun savePeriods(periodsResponse: PeriodsResponse) {
        val periodsJson = Gson().toJson(periodsResponse)
        sharedPreferences.edit().putString("periods", periodsJson).apply()
    }

    private fun startCountdownTimer(diffInMillis: Long) {
        countdownTimer = object : CountDownTimer(diffInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60

                val countdownText = StringBuilder()
                    .append(days)
                    .append("d ")
                    .append(hours)
                    .append("h ")
                    .append(minutes)
                    .append("m")
                    .toString()

                binding.homeRebootWrapper.homeRebootTimer.text = countdownText
            }

            override fun onFinish() {
                // Countdown finished
            }
        }

        countdownTimer.start()
    }

    private fun getTitle() = getString(R.string.title_home)
    private fun getSubtitle() = getString(R.string.subtitle_home)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Handle settings menu item click
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::countdownTimer.isInitialized) {
            countdownTimer.cancel()
        }
        _binding = null
    }
}
