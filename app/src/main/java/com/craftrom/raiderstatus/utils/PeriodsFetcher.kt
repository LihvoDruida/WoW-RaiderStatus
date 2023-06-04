package com.craftrom.raiderstatus.utils

import com.craftrom.raiderstatus.core.AffixResponse
import com.craftrom.raiderstatus.core.PeriodsResponse
import com.craftrom.raiderstatus.utils.Constants.BASE_RIO_URL
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class PeriodsFetcher(private val callback: (PeriodsResponse) -> Unit){

    fun fetchPeriods() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$BASE_RIO_URL/periods")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                val periodsResponse = Gson().fromJson(json, PeriodsResponse::class.java)
                callback.invoke(periodsResponse)
            }
        })
    }
}

