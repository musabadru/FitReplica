package com.fitreplica.metadata.api

interface MetadataProvider {
    suspend fun lookup(barcode: String): ClothingMetadata?
}
