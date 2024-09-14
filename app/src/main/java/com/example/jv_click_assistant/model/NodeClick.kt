package com.example.jv_click_assistant.model

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jv_click_assistant.Helper
import com.example.jv_click_assistant.service.AccessibilityService

data class NodeClick (
    var name: String,
    var boundsInParent: Bounds?,
    var text: String?,
    var contentDescription: String?,
    var readingTimeout: Int,
    var preDelayTime: Long,
    var posDelayTime: Long,
    var randomErrorTime: Long,
    var nextStepWhenNotFound: Byte ) {
    constructor(): this("Node Click", null, null, null, 5000, 0, 0, 0, 0)

    fun action(accessibilityService: AccessibilityService?): Byte {
        Helper.sleep(preDelayTime, randomErrorTime, 0.8)
        var nodeToClick: AccessibilityNodeInfo? = null
        val startTime = System.currentTimeMillis()
        while (nodeToClick == null ) {
            Helper.sleep(50, 0, 0.8)
            val timeDuration = System.currentTimeMillis() - startTime
            if (timeDuration > readingTimeout)
                return nextStepWhenNotFound
            nodeToClick = Helper.findNode(accessibilityService?.rootInActiveWindow, "text", text)
                ?: Helper.findNode(accessibilityService?.rootInActiveWindow, "contentDescription", contentDescription)
                        ?: Helper.findNode(accessibilityService?.rootInActiveWindow, "boundsInParent", boundsInParent)
        }
        Helper.sleep(1500, 500, 0.8)
        nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Helper.sleep(posDelayTime, randomErrorTime, 0.8)
        return 0
    }
}