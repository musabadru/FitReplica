package com.fitreplica.core.model

/**
 * UNKNOWN is a first-class, expected value — a garment whose sizing system can't be
 * confidently identified, not an error state. Fit estimation must degrade gracefully
 * rather than guess across incompatible systems.
 */
enum class SizeSystem {
    US,
    UK,
    EU,
    ALPHA,
    UK_SHOE,
    US_SHOE,
    JP,
    UNKNOWN,
}
