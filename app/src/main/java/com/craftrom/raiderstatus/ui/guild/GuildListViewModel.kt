package com.craftrom.raiderstatus.ui.guild

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.craftrom.raiderstatus.core.Guild

class GuildListViewModel : ViewModel() {
    val guildsLiveData: MutableLiveData<List<Guild>> = MutableLiveData()
}