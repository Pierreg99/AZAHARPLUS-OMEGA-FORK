package com.azaharplus.cast

import android.content.Context
import android.util.Log
import android.view.Surface
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.azaharplus.cast.adapters.CastingAdapter
import com.azaharplus.cast.adapters.PresentationAdapter
import com.azaharplus.cast.adapters.ProjectionAdapter

/**
 * Unified casting manager for AzaharPlus 3DS top screen external display
 * 
 * Strategy:
 * 1. Primary: PresentationAdapter for wired/DeX displays (auto-detect)
 * 2. Fallback: ProjectionAdapter for wireless casting (user-initiated)
 * 
 * Responsibilities:
 * - Detect best casting method
 * - Manage adapter lifecycle
 * - Provide Surface to native renderer
 * - Handle display connect/disconnect
 */
class CastingManager(
    private val context: Context,
    private val onCastingStarted: (Surface) -> Unit,
    private val onCastingStopped: () -> Unit
) : DefaultLifecycleObserver {
    
    private val presentationAdapter: PresentationAdapter
    private val projectionAdapter: ProjectionAdapter
    private val adapters: List<CastingAdapter>
    
    private var currentAdapter: CastingAdapter? = null
    private var isCastingEnabled = false
    
    companion object {
        private const val TAG = "CastingManager"
    }
    
    init {
        Log.d(TAG, "Initializing CastingManager")
        
        presentationAdapter = PresentationAdapter(
            context = context,
            onSurfaceReady = { surface ->
                Log.i(TAG, "Presentation surface ready")
                onCastingStarted(surface)
            },
            onSurfaceDestroyed = {
                Log.i(TAG, "Presentation surface destroyed")
                onCastingStopped()
            }
        )
        
        projectionAdapter = ProjectionAdapter(
            context = context,
            onSurfaceReady = { surface ->
                Log.i(TAG, "Projection surface ready")
                onCastingStarted(surface)
            },
            onSurfaceDestroyed = {
                Log.i(TAG, "Projection surface destroyed")
                onCastingStopped()
            }
        )
        
        adapters = listOf(presentationAdapter, projectionAdapter)
        Log.d(TAG, "CastingManager initialized with ${adapters.size} adapters")
    }
    
    /**
     * Enable casting using best available method
     * @return true if casting started successfully
     */
    fun enableCasting(): Boolean {
        if (isCastingEnabled) {
            Log.w(TAG, "Casting already enabled")
            return true
        }
        
        Log.d(TAG, "Attempting to enable casting...")
        
        // Try adapters in priority order (Presentation first, then Projection)
        for (adapter in adapters) {
            if (adapter.isAvailable() && adapter.startCasting()) {
                currentAdapter = adapter
                isCastingEnabled = true
                Log.i(TAG, "Casting enabled using: ${adapter.getDescription()}")
                return true
            }
        }
        
        Log.w(TAG, "No casting methods available")
        return false
    }
    
    /**
     * Disable casting and clean up resources
     */
    fun disableCasting() {
        if (!isCastingEnabled) {
            Log.d(TAG, "Casting already disabled")
            return
        }
        
        Log.d(TAG, "Disabling casting...")
        currentAdapter?.stopCasting()
        currentAdapter = null
        isCastingEnabled = false
        Log.i(TAG, "Casting disabled")
    }
    
    /**
     * Check if casting is currently active
     */
    fun isCasting(): Boolean = isCastingEnabled && currentAdapter?.isCasting() == true
    
    /**
     * Get current casting surface for native renderer
     */
    fun getCastingSurface(): Surface? = currentAdapter?.getCastingSurface()
    
    /**
     * Get current display size for aspect ratio calculations
     */
    fun getDisplaySize(): Pair<Int, Int>? = currentAdapter?.getDisplaySize()
    
    /**
     * Get description of current casting method
     */
    fun getCurrentMethod(): String = currentAdapter?.getDescription() ?: "None"
    
    /**
     * Check which casting methods are available
     */
    fun getAvailableMethods(): List<String> = adapters.filter { it.isAvailable() }.map { it.getDescription() }
    
    /**
     * Get projection adapter for permission handling
     */
    fun getProjectionAdapter(): ProjectionAdapter = projectionAdapter
    
    // Lifecycle callbacks
    override fun onCreate(owner: LifecycleOwner) {
        Log.d(TAG, "CastingManager onCreate")
        // No-op: adapters handle their own initialization
    }
    
    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "CastingManager onStart")
        // TODO: Auto-resume casting if enabled in settings
    }
    
    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "CastingManager onStop")
        // Keep casting active during onStop (e.g., screen rotation)
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "CastingManager onDestroy")
        disableCasting()
        presentationAdapter.cleanup()
        // Note: ProjectionAdapter cleanup is handled in disableCasting()
    }
}