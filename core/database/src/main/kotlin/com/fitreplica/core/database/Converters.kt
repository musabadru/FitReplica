package com.fitreplica.core.database

import androidx.room.TypeConverter
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.ConditionEventId
import com.fitreplica.core.model.LaundryLoadId
import com.fitreplica.core.model.OutfitId
import com.fitreplica.core.model.SizeCategory
import com.fitreplica.core.model.SizeSystem
import com.fitreplica.core.model.Status
import com.fitreplica.core.model.WearEventId

class Converters {
    @TypeConverter
    fun fromClothingId(id: ClothingId?): String? = id?.value

    @TypeConverter
    fun toClothingId(value: String?): ClothingId? = value?.let(::ClothingId)

    @TypeConverter
    fun fromOutfitId(id: OutfitId?): String? = id?.value

    @TypeConverter
    fun toOutfitId(value: String?): OutfitId? = value?.let(::OutfitId)

    @TypeConverter
    fun fromLaundryLoadId(id: LaundryLoadId?): String? = id?.value

    @TypeConverter
    fun toLaundryLoadId(value: String?): LaundryLoadId? = value?.let(::LaundryLoadId)

    @TypeConverter
    fun fromWearEventId(id: WearEventId?): String? = id?.value

    @TypeConverter
    fun toWearEventId(value: String?): WearEventId? = value?.let(::WearEventId)

    @TypeConverter
    fun fromConditionEventId(id: ConditionEventId?): String? = id?.value

    @TypeConverter
    fun toConditionEventId(value: String?): ConditionEventId? = value?.let(::ConditionEventId)

    @TypeConverter
    fun fromClothingType(value: ClothingType?): String? = EnumTypeConverter.toStorage(value)

    @TypeConverter
    fun toClothingType(value: String?): ClothingType? = EnumTypeConverter.toEnum<ClothingType>(value)

    @TypeConverter
    fun fromCondition(value: Condition?): String? = EnumTypeConverter.toStorage(value)

    @TypeConverter
    fun toCondition(value: String?): Condition? = EnumTypeConverter.toEnum<Condition>(value)

    @TypeConverter
    fun fromStatus(value: Status?): String? = EnumTypeConverter.toStorage(value)

    @TypeConverter
    fun toStatus(value: String?): Status? = EnumTypeConverter.toEnum<Status>(value)

    @TypeConverter
    fun fromSizeSystem(value: SizeSystem?): String? = EnumTypeConverter.toStorage(value)

    @TypeConverter
    fun toSizeSystem(value: String?): SizeSystem? = EnumTypeConverter.toEnum<SizeSystem>(value)

    @TypeConverter
    fun fromSizeCategory(value: SizeCategory?): String? = EnumTypeConverter.toStorage(value)

    @TypeConverter
    fun toSizeCategory(value: String?): SizeCategory? = EnumTypeConverter.toEnum<SizeCategory>(value)
}
