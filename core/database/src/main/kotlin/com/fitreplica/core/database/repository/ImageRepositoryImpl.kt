package com.fitreplica.core.database.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.fitreplica.core.common.Result
import com.fitreplica.core.database.dao.ImageDao
import com.fitreplica.core.database.entity.ImageEntity
import com.fitreplica.core.domain.repository.ImageRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.Image
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

private const val THUMBNAIL_MAX_DIMENSION_PX = 300
private const val JPEG_QUALITY = 85
private const val TAG = "ImageRepositoryImpl"

class ImageRepositoryImpl
    @Inject
    constructor(
        private val imageDao: ImageDao,
        @ApplicationContext private val context: Context,
    ) : ImageRepository {
        override fun observeImages(itemId: ClothingId): Flow<List<Image>> =
            imageDao.observeImagesForItem(itemId).map { list -> list.map { it.toDomain() } }

        override suspend fun getImages(itemId: ClothingId): List<Image> =
            imageDao.getImagesForItem(itemId).map { it.toDomain() }

        // Copies the picked/captured source URI into app-private storage and generates a
        // downscaled thumbnail, degrading to Result.Error on any failure (I/O, decode, or a
        // DB-level failure like a concurrent-delete FK violation) instead of throwing —
        // per §8g, a photo save failure must never block the item save. Runs on Dispatchers.IO
        // since file copying and bitmap decode/compress are blocking work that would otherwise
        // run on whatever dispatcher the caller happens to be on (e.g. Main).
        @Suppress("TooGenericExceptionCaught")
        override suspend fun addImage(
            itemId: ClothingId,
            sourceUri: String,
            isPrimary: Boolean,
        ): Result<Image> =
            withContext(Dispatchers.IO) {
                val imageId = UUID.randomUUID().toString()
                val imageDir = imagesDirFor(itemId).apply { mkdirs() }
                val photoFile = File(imageDir, "$imageId.jpg")
                val thumbnailFile = File(imageDir, "${imageId}_thumb.jpg")

                try {
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
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Clean up whatever was written before the failure (e.g. a thumbnail
                    // decode failure after the full-size copy succeeded, or a DB-level
                    // failure like a concurrent item delete) so no orphaned file remains.
                    photoFile.delete()
                    thumbnailFile.delete()
                    Result.Error(e)
                }
            }

        override suspend fun setPrimaryImage(
            itemId: ClothingId,
            imageId: String,
        ) {
            imageDao.setPrimaryImage(itemId, imageId)
        }

        // Deletes the DB row and (if it was primary) promotes a replacement atomically —
        // see ImageDao.deleteAndPromotePrimary — before cleaning up the physical files.
        // File cleanup itself stays best-effort/logged rather than throwing: those failures
        // are recoverable disk-space leaks, not a data-consistency problem the way a
        // half-applied delete+promote would be.
        override suspend fun deleteImage(imageId: String) {
            val entity = imageDao.deleteAndPromotePrimary(imageId) ?: return

            if (!File(entity.uri).delete()) {
                Log.w(TAG, "Failed to delete image file: ${entity.uri}")
            }
            if (!File(entity.thumbnailUri).delete()) {
                Log.w(TAG, "Failed to delete thumbnail file: ${entity.thumbnailUri}")
            }
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
                    val compressed = decoded.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
                    if (!compressed) throw IOException("Unable to compress thumbnail: ${destination.absolutePath}")
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
