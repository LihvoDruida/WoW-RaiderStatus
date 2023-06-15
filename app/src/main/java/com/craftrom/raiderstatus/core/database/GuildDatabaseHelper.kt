package com.craftrom.raiderstatus.core.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.craftrom.raiderstatus.core.Guild

class GuildDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "guilds.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "guilds"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_REALM = "realm"
        private const val COLUMN_FACTION = "faction"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_REALM TEXT," +
                "$COLUMN_FACTION TEXT);"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun saveGuilds(guilds: Array<Guild>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (guild in guilds) {
                if (!isGuildExists(guild)) { // Перевірка на наявність гільдії
                    val values = ContentValues()
                    values.put(COLUMN_NAME, guild.name)
                    values.put(COLUMN_REALM, guild.realm)
                    values.put(COLUMN_FACTION, guild.faction)

                    // Додайте ваші комірки для збереження
                    // values.put(COLUMN_ANNOUNCE, guild.announce)
                    // values.put(COLUMN_DISCORD, guild.discord)
                    // values.put(COLUMN_DESC, guild.desc)

                    db.insert(TABLE_NAME, null, values)
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun isGuildExists(guild: Guild): Boolean {
        val db = readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_NAME = ? AND $COLUMN_REALM = ? AND $COLUMN_FACTION = ?"
        val args = arrayOf(guild.name, guild.realm, guild.faction)
        val cursor = db.rawQuery(query, args)
        cursor.use {
            return it.moveToFirst() && it.getInt(0) > 0
        }
    }

    @SuppressLint("Range")
    fun getAllGuilds(): List<Guild> {
        val guildList = mutableListOf<Guild>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor: Cursor = db.rawQuery(query, null)

        cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                    val realm = cursor.getString(cursor.getColumnIndex(COLUMN_REALM))
                    val faction = cursor.getString(cursor.getColumnIndex(COLUMN_FACTION))

                    val guild = Guild(name, realm, faction, null, null, null)
                    guildList.add(guild)
                } while (cursor.moveToNext())
            }
        }

        return guildList
    }
}
