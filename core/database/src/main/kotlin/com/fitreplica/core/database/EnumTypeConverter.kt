package com.fitreplica.core.database

/**
 * Converter stores `enum.name` — renaming a stored enum constant silently orphans
 * existing rows (no compile-time or runtime error, just a failed match on read).
 * Treat enum constant renames as a real migration event, not a free refactor.
 */
object EnumTypeConverter {
    inline fun <reified T : Enum<T>> toStorage(value: T?): String? = value?.name

    inline fun <reified T : Enum<T>> toEnum(value: String?): T? = value?.let { enumValueOf<T>(it) }
}
