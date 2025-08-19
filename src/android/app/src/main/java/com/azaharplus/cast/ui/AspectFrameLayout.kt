package com.azaharplus.cast.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.min

/**
 * FrameLayout that maintains aspect ratio for 3DS top screen (400:240 = 5:3)
 * Provides letterboxing/pillarboxing for external displays
 */
class AspectFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    companion object {
        // 3DS top screen aspect ratio (400x240)
        private const val TARGET_ASPECT_RATIO = 400f / 240f // 1.667
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val originalWidth = MeasureSpec.getSize(widthMeasureSpec)
        val originalHeight = MeasureSpec.getSize(heightMeasureSpec)
        
        val displayAspect = originalWidth.toFloat() / originalHeight.toFloat()
        
        val (finalWidth, finalHeight) = if (displayAspect > TARGET_ASPECT_RATIO) {
            // Display is wider than 3DS - fit to height (letterbox)
            val width = (originalHeight * TARGET_ASPECT_RATIO).toInt()
            Pair(width, originalHeight)
        } else {
            // Display is taller than 3DS - fit to width (pillarbox)  
            val height = (originalWidth / TARGET_ASPECT_RATIO).toInt()
            Pair(originalWidth, height)
        }
        
        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY)
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)
        
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }
}