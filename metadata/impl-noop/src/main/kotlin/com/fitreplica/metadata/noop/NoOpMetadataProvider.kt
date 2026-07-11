package com.fitreplica.metadata.noop

import com.fitreplica.metadata.api.ClothingMetadata
import com.fitreplica.metadata.api.MetadataProvider
import javax.inject.Inject

class NoOpMetadataProvider
    @Inject
    constructor() : MetadataProvider {
        override suspend fun lookup(barcode: String): ClothingMetadata? = null
    }
