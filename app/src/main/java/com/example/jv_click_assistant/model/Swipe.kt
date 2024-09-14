package com.example.jv_click_assistant.model

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.jv_click_assistant.Helper
import com.example.jv_click_assistant.service.AccessibilityService
import kotlin.random.Random

data class Swipe (
    var name: String,
    var bounds1: Bounds,
    var bounds2: Bounds,
    var swipeDuration: Long,
    var randomSwipe: Boolean,
    var preDelayTime: Long,
    var posDelayTime: Long,
    var randomErrorTime: Long) {
    constructor(): this("Swipe", Bounds(0, 0, 100, 50), Bounds(0, 0, 100, 50), 150, false, 0, 0, 0)

    @RequiresApi(Build.VERSION_CODES.N)
    fun action(accessibilityService: AccessibilityService): Byte {
        Helper.sleep(preDelayTime, randomErrorTime, 0.8)
        var x1 = bounds1.left + bounds1.width / 2
        var y1 = bounds1.top + bounds1.height / 2
        var x2 = bounds2.left + bounds2.width / 2
        var y2 = bounds2.top + bounds2.height / 2

        if (randomSwipe) {
            x1 += Random.nextInt(-50, 50)
            y1 += Random.nextInt(-25, 25)
            x2 += Random.nextInt(-50, 50)
            y2 += Random.nextInt(-25, 25)
            swipeDuration = Random.nextLong(swipeDuration - 25, swipeDuration + 25)
        }
        // Create path
        val swipePath = Path()
        swipePath.moveTo(x1.toFloat(), y1.toFloat())
        swipePath.lineTo(x2.toFloat(), y2.toFloat())
        // Create gesture
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(
            GestureDescription.StrokeDescription(
                swipePath, 0, swipeDuration
            )
        )
        val gesture = gestureBuilder.build()
        // Launch gesture
        accessibilityService!!.dispatchGesture(gesture, null, null)
        Helper.sleep(posDelayTime, randomErrorTime, 0.8)
        return 0
    }
}