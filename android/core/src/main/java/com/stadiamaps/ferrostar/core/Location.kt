package com.stadiamaps.ferrostar.core

import uniffi.ferrostar.CourseOverGround
import uniffi.ferrostar.GeographicCoordinates
import java.util.concurrent.Executor

interface Location {
    val coordinates: GeographicCoordinates
    val horizontalAccuracy: Float
    val courseOverGround: CourseOverGround?
}

data class SimulatedLocation(
    override val coordinates: GeographicCoordinates,
    override val horizontalAccuracy: Float,
    override val courseOverGround: CourseOverGround?
) : Location

// TODO: Decide if we want to have a compile-time dependency on Android
data class AndroidLocation(
    override val coordinates: GeographicCoordinates,
    override val horizontalAccuracy: Float,
    override val courseOverGround: CourseOverGround?
) : Location {
    constructor(location: android.location.Location) : this(
        GeographicCoordinates(location.latitude, location.longitude),
        location.accuracy,
        if (location.hasBearing() && location.hasBearingAccuracy()) {
            CourseOverGround(
                location.bearing.toInt().toUShort(),
                location.bearingAccuracyDegrees.toInt().toUShort()
            )
        } else {
            null
        }
    )
}

interface LocationProvider {
    val lastLocation: Location?
    // TODO: Decide how to handle this on Android
    val lastHeading: Float?

    fun addListener(listener: LocationUpdateListener, executor: Executor)
    fun removeListener(listener: LocationUpdateListener)
}

interface LocationUpdateListener {
    fun onLocationUpdated(location: Location)
    fun onHeadingUpdated(heading: Float)
}

/**
 * Location provider for testing without relying on simulator location spoofing.
 *
 * This allows for more granular unit tests.
 */
class SimulatedLocationProvider : LocationProvider {
    override var lastLocation: Location? = null
        set(value) {
            field = value
            onLocationUpdated()
        }
    override var lastHeading: Float? = null
        set(value) {
            field = value
            onHeadingUpdated()
        }

    override fun addListener(listener: LocationUpdateListener, executor: Executor) {
        listeners.add(listener to executor)
    }

    override fun removeListener(listener: LocationUpdateListener) {
        listeners.removeIf { it.first == listener }
    }

    private var listeners: MutableList<Pair<LocationUpdateListener, Executor>> = mutableListOf()

    private fun onLocationUpdated() {
        val location = lastLocation
        if (location != null) {
            for ((listener, executor) in listeners) {
                executor.execute {
                    listener.onLocationUpdated(location)
                }
            }
        }
    }

    private fun onHeadingUpdated() {
        val heading = lastHeading
        if (heading != null) {
            for ((listener, executor) in listeners) {
                executor.execute {
                    listener.onHeadingUpdated(heading)
                }
            }
        }
    }
}

// TODO: Real implementation