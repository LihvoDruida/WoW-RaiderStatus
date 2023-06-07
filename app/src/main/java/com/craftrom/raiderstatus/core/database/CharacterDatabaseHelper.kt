/**
 * CharacterDatabaseHelper.kt
 *
 * This class provides helper methods for managing a SQLite database for storing character data.
 * It includes methods for creating the database, saving characters, loading characters, deleting characters,
 * and searching for characters based on name, realm, and region.
 *
 * Database schema:
 * - Table name: character
 * - Columns: id (integer), name (text), realm (text), region (text), guild (text), gear (integer), mythicScore (text)
 *
 * Usage:
 * 1. Create an instance of CharacterDatabaseHelper by passing the context.
 * 2. Call saveCharacter() method to save a character to the database.
 * 3. Call loadCharacters() method to retrieve all characters from the database.
 * 4. Call deleteCharacter() method to delete a character from the database.
 * 5. Call searchCharacter() method to search for a character based on name, realm, and region.
 */

package com.craftrom.raiderstatus.core.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.craftrom.raiderstatus.ui.character.CharacterFragment

class CharacterDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "character.db"
        private const val DATABASE_VERSION = 1

        private const val CHAR_TABLE_NAME = "character"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_REALM = "realm"
        private const val COLUMN_REGION = "region"
        private const val COLUMN_GUILD = "guild"
        private const val COLUMN_GEAR = "gear"
        private const val COLUMN_MYTHIC_SCORE = "mythicScore"
        // Add other character data columns here
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $CHAR_TABLE_NAME " +
                "($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_REALM TEXT, " +
                "$COLUMN_REGION TEXT, " +
                "$COLUMN_GUILD TEXT, " +
                "$COLUMN_GEAR INTEGER, " +
                "$COLUMN_MYTHIC_SCORE TEXT)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // You can add code to upgrade the database when the version changes
    }

    fun saveCharacter(character: CharacterFragment.Character) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, character.name)
            put(COLUMN_REALM, character.realm)
            put(COLUMN_REGION, character.region)
            put(COLUMN_GUILD, character.guild)
            put(COLUMN_GEAR, character.gear)
        }
        db.insert(CHAR_TABLE_NAME, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun loadCharacters(): MutableList<CharacterFragment.Character> {
        val characters = mutableListOf<CharacterFragment.Character>()
        val db = readableDatabase
        val query = "SELECT * FROM $CHAR_TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            val realm = cursor.getString(cursor.getColumnIndex(COLUMN_REALM))
            val region = cursor.getString(cursor.getColumnIndex(COLUMN_REGION))
            val character = CharacterFragment.Character(name, realm, region, null, "-", 0)
            characters.add(character)
        }
        cursor.close()
        db.close()
        return characters
    }

    @SuppressLint("Range")
    fun searchCharacter(name: String, realm: String, region: String): CharacterFragment.Character? {
        val db = readableDatabase
        val selection = "LOWER($COLUMN_NAME) = ? AND LOWER($COLUMN_REALM) = ? AND LOWER($COLUMN_REGION) = ?"
        val selectionArgs = arrayOf(name.toLowerCase(), realm.toLowerCase(), region.toLowerCase())
        val query = "SELECT * FROM $CHAR_TABLE_NAME WHERE $selection"
        val cursor = db.rawQuery(query, selectionArgs)
        val character: CharacterFragment.Character?
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
            val guild = cursor.getString(cursor.getColumnIndex(COLUMN_GUILD))
            val gear = cursor.getInt(cursor.getColumnIndex(COLUMN_GEAR))
            character = CharacterFragment.Character(name, realm, region, null, guild, gear)
        } else {
            character = null
        }
        cursor.close()
        db.close()
        return character
    }



    fun deleteCharacter(character: String) {
        val db = writableDatabase
        val selection = "$COLUMN_NAME = ?"
        val selectionArgs = arrayOf(character)
        db.delete(CHAR_TABLE_NAME, selection, selectionArgs)
        db.close()
    }
}
