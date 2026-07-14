package com.fitreplica.core.domain.repository

import com.fitreplica.core.model.WeatherSnapshot
import javax.inject.Inject

interface WeatherProvider {
    suspend fun currentWeather(): WeatherSnapshot?
}

class NoOpWeatherProvider
    @Inject
    constructor() : WeatherProvider {
        override suspend fun currentWeather(): WeatherSnapshot? = null
    }
