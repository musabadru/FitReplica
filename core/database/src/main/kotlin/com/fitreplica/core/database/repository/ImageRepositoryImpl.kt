package com.fitreplica.core.database.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.fitreplica.core.common.Result
import com.fitreplica.core.database.dao.ImageDao
import com.fitreplica.core.database.entity.ImageEntity
import com.fitreplica.core.domain.repository.ImageRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.Image
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

private const val THUMBNAIL_MAX_DIMENSION_PX = 300
private const val JPEG_QUALITY = 85

class ImageRepositoryImpl
    @Inject
    constructor(
        private val imageDao: ImageDao,
        @ApplicationContext private val context: Context,
    ) : ImageRepository {
        override fun observeImages(itemId: ClothingId): Flow<List<Image>> =
            imageDao.observeImagesForItem(itemId).map { list -> list.map { it.toDomain() } }

        // Copies the picked/captured source URI into app-private storage and generates a
        // downscaled thumbnail, degrading to Result.Error on any I/O failure (e.g. disk full)
        // instead of throwing — per §8g, a photo save failure must never block the item save.
        override suspend fun addImage(
            itemId: ClothingId,
            sourceUri: String,
            isPrimary: Boolean,
        ): Result<Image> =
            try {
                val imageId = UUID.randomUUID().toString()
                val imageDir = imagesDirFor(itemId).apply { mkdirs() }
                val photoFile = File(imageDir, "$imageId.jpg")
                val thumbnailFile = File(imageDir, "${imageId}_thumb.jpg")

                copySourceInto(sourceUri, photoFile)
                writeThumbnail(photoFile, thumbnailFile)

                val entity =
                    ImageEntity(
                        id = imageId,
                        itemId = itemId,
                        uri = photoFile.absolutePath,
                        thumbnailUri = thumbnailFile.absolutePath,
                        isPrimary = isPrimary,
                        takenAt = System.currentTimeMillis(),
                    )
                imageDao.insertImage(entity)
                Result.Success(entity.toDomain())
            } catch (e: IOException) {
                Result.Error(e)
            } catch (e: SecurityException) {
                Result.Error(e)
            }

        override suspend fun setPrimaryImage(
            itemId: ClothingId,
            imageId: String,
        ) {
            imageDao.setPrimaryImage(itemId, imageId)
        }

        override suspend fun deleteImage(imageId: String) {
            val entity = imageDao.findById(imageId) ?: return
            File(entity.uri).delete()
            File(entity.thumbnailUri).delete()
            imageDao.deleteImage(imageId)
        }

        private fun imagesDirFor(itemId: ClothingId): File = File(context.filesDir, "images/${itemId.value}")

        private fun copySourceInto(
            sourceUri: String,
            destination: File,
        ) {
            val input =
                context.contentResolver.openInputStream(Uri.parse(sourceUri))
                    ?: throw IOException("Unable to open source image: $sourceUri")
            input.use { stream -> destination.outputStream().use { output -> stream.copyTo(output) } }
        }

        private fun writeThumbnail(
            source: File,
            destination: File,
        ) {
            val bounds =
                BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    .also { BitmapFactory.decodeFile(source.absolutePath, it) }
            val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, THUMBNAIL_MAX_DIMENSION_PX)

            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap =
                BitmapFactory.decodeFile(source.absolutePath, options)
                    ?: throw IOException("Unable to decode image: ${source.absolutePath}")
            bitmap.use { decoded ->
                destination.outputStream().use { output ->
                    decoded.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
                }
            }
        }

        private fun calculateSampleSize(
            width: Int,
            height: Int,
            maxDimension: Int,
        ): Int {
            var sampleSize = 1
            while (maxOf(width, height) / (sampleSize * 2) >= maxDimension) {
                sampleSize *= 2
            }
            return sampleSize
        }

        private inline fun Bitmap.use(block: (Bitmap) -> Unit) {
            try {
                block(this)
            } finally {
                recycle()
            }
        }
    }

private fun ImageEntity.toDomain(): Image =
    Image(
        id = id,
        itemId = itemId,
        uri = uri,
        thumbnailUri = thumbnailUri,
        isPrimary = isPrimary,
        takenAt = takenAt,
    )
