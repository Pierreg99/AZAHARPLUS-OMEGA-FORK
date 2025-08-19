package com.azaharplus.cast.adapters

import android.app.Presentation
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.azaharplus.cast.surface.TopScreenSurface
import com.azaharplus.cast.ui.AspectFrameLayout

/**
 * Casting adapter for wired/DeX external displays using Presentation API
 * This is the preferred method when external displays are physically connected
 */
class PresentationAdapter(
    private val context: Context,
    private val onSurfaceReady: (Surface) -> Unit,
    private val onSurfaceDestroyed: () -> Unit
) : CastingAdapter, DisplayManager.DisplayListener {
    
    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private var currentPresentation: TopScreenPresentation? = null
    private var castingSurface: Surface? = null
    
    companion object {
        private const val TAG = "PresentationAdapter"
    }
    
    init {
        // Register for display changes
        displayManager.registerDisplayListener(this, null)
        Log.d(TAG, "PresentationAdapter initialized")
    }
    
    override fun startCasting(): Boolean {
        Log.d(TAG, "Attempting to start presentation casting...")
        
        val externalDisplay = findExternalDisplay()
        if (externalDisplay == null) {
            Log.w(TAG, "No external displays found")
            return false
        }
        
        return try {
            currentPresentation = TopScreenPresentation(
                context, 
                externalDisplay,
                onSurfaceReady = { surface ->
                    castingSurface = surface
                    onSurfaceReady(surface)
                    Log.i(TAG, "Presentation surface created: ${surface}")
                },
                onSurfaceDestroyed = {
                    castingSurface = null
                    onSurfaceDestroyed()
                    Log.i(TAG, "Presentation surface destroyed")
                }
            )
            currentPresentation?.show()
            Log.i(TAG, "Presentation started on display: ${externalDisplay.displayId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start presentation", e)
            false
        }
    }
    
    override fun stopCasting() {
        Log.d(TAG, "Stopping presentation casting...")
        currentPresentation?.dismiss()
        currentPresentation = null
        castingSurface = null
    }
    
    override fun isAvailable(): Boolean {
        val available = findExternalDisplay() != null
        Log.d(TAG, "External display available: $available")
        return available
    }
    
    override fun getCastingSurface(): Surface? = castingSurface
    
    override fun isCasting(): Boolean = currentPresentation != null
    
    override fun getDisplaySize(): Pair<Int, Int>? {
        val display = findExternalDisplay() ?: return null
        val metrics = android.util.DisplayMetrics()
        display.getMetrics(metrics)
        return Pair(metrics.widthPixels, metrics.heightPixels)
    }
    
    override fun getDescription(): String = "External Display (HDMI/DeX)"
    
    private fun findExternalDisplay(): Display? {
        return displayManager.displays.firstOrNull { display ->
            display.displayId != Display.DEFAULT_DISPLAY
        }
    }
    
    // DisplayManager.DisplayListener
    override fun onDisplayAdded(displayId: Int) {
        Log.d(TAG, "Display added: $displayId")
        // TODO: Auto-start casting if enabled in settings
    }
    
    override fun onDisplayRemoved(displayId: Int) {
        Log.d(TAG, "Display removed: $displayId")
        if (currentPresentation?.display?.displayId == displayId) {
            stopCasting()
        }
    }
    
    override fun onDisplayChanged(displayId: Int) {
        Log.d(TAG, "Display changed: $displayId")
        // TODO: Handle orientation/resolution changes
    }
    
    fun cleanup() {
        displayManager.unregisterDisplayListener(this)
        stopCasting()
    }
    
    /**
     * Inner presentation class for displaying 3DS top screen
     */
    private class TopScreenPresentation(
        context: Context,
        display: Display,
        private val onSurfaceReady: (Surface) -> Unit,
        private val onSurfaceDestroyed: () -> Unit
    ) : Presentation(context, display), SurfaceHolder.Callback {
        
        private lateinit var surfaceView: SurfaceView
        private val topScreenSurface = TopScreenSurface()
        
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Log.d(TAG, "TopScreenPresentation onCreate")
            
            // Create AspectFrameLayout to maintain 3DS aspect ratio
            val aspectFrameLayout = AspectFrameLayout(context)
            
            surfaceView = SurfaceView(context).apply {
                holder.addCallback(this@TopScreenPresentation)
            }
            
            // Wrap SurfaceView in AspectFrameLayout for proper scaling
            aspectFrameLayout.addView(surfaceView)
            setContentView(aspectFrameLayout)
        }
        
        override fun onStart() {
            super.onStart()
            Log.d(TAG, "TopScreenPresentation started")
        }
        
        override fun onStop() {
            super.onStop()
            Log.d(TAG, "TopScreenPresentation stopped")
        }
        
        // SurfaceHolder.Callback
        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.d(TAG, "Presentation surface created")
            onSurfaceReady(holder.surface)
        }
        
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            Log.d(TAG, "Presentation surface changed: ${width}x${height}")
            // TODO: Notify native renderer of size changes
        }
        
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            Log.d(TAG, "Presentation surface destroyed")
            onSurfaceDestroyed()
        }
    }
}