package com.fitreplica.core.database

import androidx.room.TypeConverter
import org.json.JSONArray

// Freeform tags have no relational integrity to preserve, so a JSON-encoded column
// (org.json, already on the Android SDK — no extra dependency) is fine here, unlike
// GarmentSize which is flattened into real columns for queryability. Split out from
// Converters to keep that class under the project's TooManyFunctions threshold.
class ListConverters {
    @TypeConverter
    fun fromTagList(value: List<String>): String = JSONArray(value).toString()

    @TypeConverter
    fun toTagList(value: String): List<String> {
        val array = JSONArray(value)
        return List(array.length()) { index -> array.getString(index) }
    }
}
