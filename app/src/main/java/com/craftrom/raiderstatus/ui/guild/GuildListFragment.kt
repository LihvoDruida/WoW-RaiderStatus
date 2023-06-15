package com.craftrom.raiderstatus.ui.guild

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.craftrom.raiderstatus.MainActivity
import com.craftrom.raiderstatus.R
import com.craftrom.raiderstatus.core.Guild
import com.craftrom.raiderstatus.core.database.GuildDatabaseHelper
import com.craftrom.raiderstatus.databinding.FragmentGuildListBinding
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GuildListFragment : Fragment() {

    private var _binding: FragmentGuildListBinding? = null
    private val binding get() = _binding!!
    private lateinit var guildAdapter: GuildAdapter
    private val guildDatabaseHelper: GuildDatabaseHelper by lazy { GuildDatabaseHelper(requireContext()) }
    private val viewModel: GuildListViewModel by viewModels()


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
        _binding = FragmentGuildListBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.guildRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val mainActivity = requireActivity() as MainActivity
        mainActivity.setToolbarText(getTitle(), getSubtitle())

        guildAdapter = GuildAdapter(binding.emptyHelp, binding.guildRecyclerView)
        binding.guildRecyclerView.adapter = guildAdapter

        viewModel.guildsLiveData.observe(viewLifecycleOwner) { guildList ->
            displayData(guildList)
        }
        lifecycleScope.launch {
            fetchData()
        }
        return view
    }

    private suspend fun fetchData() {
        if (viewModel.guildsLiveData.value == null) {
            withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://raw.githubusercontent.com/melles1991/UAGuildsChecker/main/ua_guilds_list.json")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        reader.close()

                        Log.d("com.craftrom.raiderstatus.ui.guild.GuildListFragment", "Response: $response")

                        val gson = Gson()
                        val guildsResponse = gson.fromJson(response.toString(), GuildsResponse::class.java)

                        withContext(Dispatchers.Main) {
                            updateDatabase(guildsResponse.guilds)
                            guildAdapter.setGuilds(guildsResponse.guilds)
                        }

                    } else {
                        Log.e("com.craftrom.raiderstatus.ui.guild.GuildListFragment", "HTTP Error: $responseCode")
                    }
                } catch (e: Exception) {
                    Log.e("com.craftrom.raiderstatus.ui.guild.GuildListFragment", "Error: ${e.message}")
                }
            }
        }
    }



    private fun displayData(guildList: List<Guild>) {
        guildAdapter.setGuilds(guildList)
    }

    private fun updateDatabase(newGuilds: List<Guild>) {
        val existingGuilds = guildDatabaseHelper.getAllGuilds()

        val newGuildsSet = newGuilds.toSet()
        val existingGuildsSet = existingGuilds.toSet()

        val guildsToAdd = newGuildsSet.minus(existingGuildsSet)

        if (guildsToAdd.isNotEmpty()) {
            guildDatabaseHelper.saveGuilds(guildsToAdd.toList().toTypedArray())
        }
    }

    private data class GuildsResponse(val guilds: List<Guild>)

    private fun getTitle() = getString(R.string.title_guild_list)
    private fun getSubtitle() = getString(R.string.subtitle_guild_list)

    override fun onDestroyView() {
        super.onDestroyView()
        guildDatabaseHelper.close()
        _binding = null
    }
}
