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

        private const val TABLE_NAME = "character"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_REALM = "realm"
        private const val COLUMN_REGION = "region"
        // Додайте стовпці для збереження інших даних персонажа
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME " +
                "($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_REALM TEXT, " +
                "$COLUMN_REGION TEXT)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Ви можете додати код для оновлення бази даних при зміні версії
    }

    fun saveCharacter(character: CharacterFragment.Character) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, character.name)
            put(COLUMN_REALM, character.realm)
            put(COLUMN_REGION, character.region)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun loadCharacters(): MutableList<CharacterFragment.Character> {
        val characters = mutableListOf<CharacterFragment.Character>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            val realm = cursor.getString(cursor.getColumnIndex(COLUMN_REALM))
            val region = cursor.getString(cursor.getColumnIndex(COLUMN_REGION))
            val character = CharacterFragment.Character(name, realm, region)
            characters.add(character)
        }
        cursor.close()
        db.close()
        return characters
    }

    fun deleteCharacter(character: String) {
        val db = writableDatabase
        val selection = "$COLUMN_NAME = ?"
        val selectionArgs = arrayOf(character)
        db.delete(TABLE_NAME, selection, selectionArgs)
        db.close()
    }
}
