package com.azaharplus.cast.surface

import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface

/**
 * Manages Surface creation and lifecycle for 3DS top screen rendering
 * Provides abstraction over different Surface sources (Presentation, VirtualDisplay)
 */
class TopScreenSurface {
    
    companion object {
        private const val TAG = "TopScreenSurface"
        
        // 3DS top screen native resolution
        const val NATIVE_WIDTH = 400
        const val NATIVE_HEIGHT = 240
        const val ASPECT_RATIO = NATIVE_WIDTH.toFloat() / NATIVE_HEIGHT.toFloat() // 1.67
    }
    
    private var currentSurface: Surface? = null
    private var surfaceTexture: SurfaceTexture? = null
    
    /**
     * Create a Surface for the given dimensions
     * Used by ProjectionAdapter for virtual display
     */
    fun createSurface(width: Int, height: Int): Surface {
        Log.d(TAG, "Creating surface: ${width}x${height}")
        
        // Create SurfaceTexture for virtual display scenarios
        surfaceTexture = SurfaceTexture(0).apply {
            setDefaultBufferSize(width, height)
        }
        
        currentSurface = Surface(surfaceTexture)
        return currentSurface!!
    }
    
    /**
     * Use an existing Surface from Presentation
     */
    fun setSurface(surface: Surface) {
        Log.d(TAG, "Setting existing surface: $surface")
        currentSurface = surface
    }
    
    /**
     * Get current surface for native renderer
     */
    fun getSurface(): Surface? = currentSurface
    
    /**
     * Calculate optimal dimensions maintaining 3DS aspect ratio
     */
    fun calculateOptimalSize(displayWidth: Int, displayHeight: Int): Pair<Int, Int> {
        val displayAspect = displayWidth.toFloat() / displayHeight.toFloat()
        
        return if (displayAspect > ASPECT_RATIO) {
            // Display is wider than 3DS - fit to height
            val width = (displayHeight * ASPECT_RATIO).toInt()
            Pair(width, displayHeight)
        } else {
            // Display is taller than 3DS - fit to width  
            val height = (displayWidth / ASPECT_RATIO).toInt()
            Pair(displayWidth, height)
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up TopScreenSurface")
        currentSurface?.release()
        currentSurface = null
        surfaceTexture?.release()
        surfaceTexture = null
    }
}