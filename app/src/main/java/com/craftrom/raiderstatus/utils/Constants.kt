package com.craftrom.raiderstatus.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object Constants {
    const val BASE_RIO_URL = "https://raider.io/api/v1"

    // Інші константи
    const val DEFAULT_REGION = "eu"
    const val DEFAULT_LOCALE = "en"
    const val DATE_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val DATE_FORMAT_USER = "dd.MM.yyyy HH:mm"

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    fun getSeasonName(season: String): String {
        return when (season) {
            "season-df-1" -> "DF Season 1"
            "season-df-2" -> "DF Season 2"
            "season-df-3" -> "DF Season 3"
            "season-df-4" -> "DF Season 4"
            else -> "Unknown Season"
        }
    }

}
