package com.azaharplus.cast.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Log
import android.view.Surface
import com.azaharplus.cast.surface.TopScreenSurface

/**
 * Casting adapter using MediaProjection + VirtualDisplay
 * Fallback method when no external displays are available
 * Requires user permission and creates a virtual display for casting apps
 */
class ProjectionAdapter(
    private val context: Context,
    private val onSurfaceReady: (Surface) -> Unit,
    private val onSurfaceDestroyed: () -> Unit
) : CastingAdapter {
    
    private val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) 
            as MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var castingSurface: Surface? = null
    private val topScreenSurface = TopScreenSurface()
    
    companion object {
        private const val TAG = "ProjectionAdapter"
        const val REQUEST_CODE_PROJECTION = 2000
        
        // Virtual display properties for 3DS top screen
        private const val VIRTUAL_DISPLAY_NAME = "AzaharPlus_TopScreen"
        private const val VIRTUAL_DISPLAY_WIDTH = 800   // 2x 3DS top screen width (400)
        private const val VIRTUAL_DISPLAY_HEIGHT = 480  // 2x 3DS top screen height (240)
        private const val VIRTUAL_DISPLAY_DPI = 160
    }
    
    override fun startCasting(): Boolean {
        Log.d(TAG, "Starting projection casting...")
        
        if (!isPermissionGranted()) {
            Log.w(TAG, "Media projection permission not granted")
            return false
        }
        
        // TODO: Start media projection with cached intent result
        Log.d(TAG, "Media projection permission available, creating virtual display...")
        return createVirtualDisplay()
    }
    
    override fun stopCasting() {
        Log.d(TAG, "Stopping projection casting...")
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        mediaProjection = null
        castingSurface = null
    }
    
    override fun isAvailable(): Boolean {
        // Always available, but requires user permission
        return true
    }
    
    override fun getCastingSurface(): Surface? = castingSurface
    
    override fun isCasting(): Boolean = virtualDisplay != null
    
    override fun getDisplaySize(): Pair<Int, Int> = 
        Pair(VIRTUAL_DISPLAY_WIDTH, VIRTUAL_DISPLAY_HEIGHT)
    
    override fun getDescription(): String = "Wireless Casting (Screen Share)"
    
    /**
     * Request media projection permission from user
     * Call this from Activity.startActivityForResult()
     */
    fun requestPermission(activity: Activity) {
        Log.d(TAG, "Requesting media projection permission...")
        val intent = projectionManager.createScreenCaptureIntent()
        activity.startActivityForResult(intent, REQUEST_CODE_PROJECTION)
    }
    
    /**
     * Handle permission result from Activity.onActivityResult()
     */
    fun handlePermissionResult(resultCode: Int, data: Intent?): Boolean {
        Log.d(TAG, "Handling projection permission result: $resultCode")
        
        if (resultCode != Activity.RESULT_OK || data == null) {
            Log.w(TAG, "Media projection permission denied")
            return false
        }
        
        try {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)
            Log.i(TAG, "Media projection permission granted")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get media projection", e)
            return false
        }
    }
    
    private fun isPermissionGranted(): Boolean = mediaProjection != null
    
    private fun createVirtualDisplay(): Boolean {
        val projection = mediaProjection ?: return false
        
        return try {
            // Create surface for virtual display
            castingSurface = topScreenSurface.createSurface(VIRTUAL_DISPLAY_WIDTH, VIRTUAL_DISPLAY_HEIGHT)
            
            virtualDisplay = projection.createVirtualDisplay(
                VIRTUAL_DISPLAY_NAME,
                VIRTUAL_DISPLAY_WIDTH,
                VIRTUAL_DISPLAY_HEIGHT,
                VIRTUAL_DISPLAY_DPI,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                castingSurface,
                null, // VirtualDisplay.Callback
                null  // Handler
            )
            
            if (virtualDisplay != null) {
                Log.i(TAG, "Virtual display created: $VIRTUAL_DISPLAY_NAME")
                onSurfaceReady(castingSurface!!)
                true
            } else {
                Log.e(TAG, "Failed to create virtual display")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating virtual display", e)
            false
        }
    }
}