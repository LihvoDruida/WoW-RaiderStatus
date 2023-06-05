package com.craftrom.raiderstatus.utils

import android.util.Log
import com.craftrom.raiderstatus.core.CharacterData
import com.craftrom.raiderstatus.ui.character.CharacterFragment.Character
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CharacterDataFetcher(private val character: Character) {

    @OptIn(DelicateCoroutinesApi::class)
    fun fetchCharacterData(callback: (CharacterData?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url =
                    URL("https://raider.io/api/v1/characters/profile?region=${character.region}&realm=${character.realm}&name=${character.name}&fields=gear%2Cguild%2Craid_progression%2Cmythic_plus_scores_by_season%3Acurrent")
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

                    val gson = Gson()
                    val characterData = gson.fromJson(response.toString(), CharacterData::class.java)

                    // Повернути дані персонажа через зворотний виклик у головний потік
                    withContext(Dispatchers.Main) {
                        callback.invoke(characterData)
                    }
                } else {
                    Log.e("CharacterDataFetcher", "HTTP Error: $responseCode")
                    withContext(Dispatchers.Main) {
                        callback.invoke(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("CharacterDataFetcher", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback.invoke(null)
                }
            }
        }
    }
}
