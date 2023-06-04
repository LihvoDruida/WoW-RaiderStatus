package com.craftrom.raiderstatus.utils

import com.craftrom.raiderstatus.core.AffixResponse
import com.craftrom.raiderstatus.utils.Constants.BASE_RIO_URL
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class AffixFetcher(private val callback: (AffixResponse) -> Unit) {

    fun fetchAffixes() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$BASE_RIO_URL/mythic-plus/affixes?region=eu&locale=en")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                val affixResponse = Gson().fromJson(json, AffixResponse::class.java)
                callback.invoke(affixResponse)
            }
        })
    }
}
