package com.agarwaldevs.alarm

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AlarmDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "alarm.db"
        private const val DATABASE_VERSION = 2
        const val TABLE_NAME = "alarms"
        const val COLUMN_ID = "id"
        const val COLUMN_TIME = "time"
        const val COLUMN_NAME = "name"
        const val COLUMN_DAYS = "days"
        const val COLUMN_RINGTONE = "ringtone"
        const val COLUMN_VIBRATE = "vibrate"
        const val COLUMN_IS_ENABLED = "is_enabled"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TIME TEXT,
            $COLUMN_NAME TEXT,
            $COLUMN_DAYS TEXT,
            $COLUMN_RINGTONE TEXT,
            $COLUMN_VIBRATE INTEGER,
            $COLUMN_IS_ENABLED INTEGER DEFAULT 1
        )
    """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_IS_ENABLED INTEGER DEFAULT 1")
        }
    }

    fun addAlarm(alarm: Alarm) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIME, alarm.time)
            put(COLUMN_NAME, alarm.name)
            put(COLUMN_DAYS, alarm.days.joinToString(","))
            put(COLUMN_RINGTONE, alarm.ringtone)
            put(COLUMN_VIBRATE, if (alarm.vibrate) 1 else 0)
            put(COLUMN_IS_ENABLED, if (alarm.isEnabled) 1 else 0)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun fetchAlarms(): List<Alarm> {
        val alarms = mutableListOf<Alarm>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            alarms.add(cursorToAlarm(cursor))
        }
        cursor.close()
        db.close()
        return alarms
    }

    fun toggleAlarm(alarmId: Int, isEnabled: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_ENABLED, if (isEnabled) 1 else 0)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(alarmId.toString()))
        db.close()
    }

    @SuppressLint("Range")
    fun getAlarm(alarmId: Int): Alarm? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(alarmId.toString()),
            null,
            null,
            null
        )
        var alarm: Alarm? = null
        if (cursor.moveToFirst()) {
            alarm = cursorToAlarm(cursor)
        }
        cursor.close()
        db.close()
        return alarm
    }

    @SuppressLint("Range")
    private fun cursorToAlarm(cursor: android.database.Cursor): Alarm {
        return Alarm(
            id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
            time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME)),
            name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
            days = cursor.getString(cursor.getColumnIndex(COLUMN_DAYS)).split(",").filter { it.isNotBlank() },
            ringtone = cursor.getString(cursor.getColumnIndex(COLUMN_RINGTONE)),
            vibrate = cursor.getInt(cursor.getColumnIndex(COLUMN_VIBRATE)) == 1,
            isEnabled = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_ENABLED)) == 1
        )
    }
}
