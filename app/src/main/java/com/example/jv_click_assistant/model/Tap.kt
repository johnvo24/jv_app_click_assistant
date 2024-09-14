package com.example.jv_click_assistant.model

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.jv_click_assistant.Helper
import com.example.jv_click_assistant.service.AccessibilityService
import kotlin.random.Random

data class Tap(
    var name: String,
    var bounds: Bounds,
    var tapDuration: Long,
    var randomTap: Boolean,
    var randomTapDuration: Boolean,
    var preDelayTime: Long,
    var posDelayTime: Long,
    var randomErrorTime: Long) {
    constructor(): this("Tap", Bounds(0, 0, 0, 0), 45, false, false, 0, 0, 0)

    @RequiresApi(Build.VERSION_CODES.N)
    fun action(accessibilityService: AccessibilityService): Byte {
        Helper.sleep(preDelayTime, randomErrorTime, 0.8)

        var clickX = bounds.left + bounds.width/2;
        var clickY = bounds.top + bounds.height/2;
        if (randomTap) {
            clickX += Random.nextInt(-bounds.width/2, bounds.width/2+1)
            clickY += Random.nextInt(-bounds.height/2, bounds.height/2+1)
        }
        if (randomTapDuration) {
            tapDuration += Random.nextLong(0, 35)
        }

        //Tạo đường dẫn tới vị trí chạm
        val clickPath = Path()
        clickPath.moveTo(clickX.toFloat(), clickY.toFloat())
        //Tạo cử chỉ
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(
            GestureDescription.StrokeDescription(clickPath, 0, tapDuration)
        )
        val gesture = gestureBuilder.build()
        //Phát cử chỉ
        accessibilityService!!.dispatchGesture(gesture, null, null)

        Helper.sleep(posDelayTime, randomErrorTime, 0.8)
        return 0
    }
}