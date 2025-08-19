package com.azaharplus.cast.adapters

import android.view.Surface

/**
 * Common interface for casting implementations
 * Abstracts DisplayManager + Presentation vs MediaProjection + VirtualDisplay
 */
interface CastingAdapter {
    
    /**
     * Attempt to start casting using this adapter's strategy
     * @return true if casting started successfully
     */
    fun startCasting(): Boolean
    
    /**
     * Stop casting and clean up resources
     */
    fun stopCasting()
    
    /**
     * Check if this adapter can currently provide casting
     * @return true if external displays available or user has granted projection permission
     */
    fun isAvailable(): Boolean
    
    /**
     * Get the current casting surface for top screen rendering
     * @return Surface instance or null if not casting
     */
    fun getCastingSurface(): Surface?
    
    /**
     * Check if casting is currently active
     */
    fun isCasting(): Boolean
    
    /**
     * Get display information for aspect ratio calculations
     * @return Pair<width, height> or null if not available
     */
    fun getDisplaySize(): Pair<Int, Int>?
    
    /**
     * Get human-readable description of this casting method
     */
    fun getDescription(): String
}