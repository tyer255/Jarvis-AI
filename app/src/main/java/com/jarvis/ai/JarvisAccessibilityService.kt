package com.jarvis.ai

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("JarvisA11y", "onServiceConnected called - accessibility service bound")
        // Notify MainActivity/UI that service is connected
        val i = Intent("com.jarvis.ai.ACCESSIBILITY_CONNECTED")
        sendBroadcast(i)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        Log.d("JarvisA11y", "onInterrupt called")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        Log.d("JarvisA11y", "onUnbind called - accessibility service unbound")
        return super.onUnbind(intent)
    }

    fun performScrollDown() {
        val path = Path().apply {
            moveTo(500f, 1500f)
            lineTo(500f, 500f)
        }
        dispatchGesture(
            GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
                .build(),
            null, null
        )
    }

    fun performScrollUp() {
        val path = Path().apply {
            moveTo(500f, 500f)
            lineTo(500f, 1500f)
        }
        dispatchGesture(
            GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
                .build(),
            null, null
        )
    }

    fun goBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun goHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun typeText(text: String) {
        val rootNode = rootInActiveWindow ?: return
        val focusNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

        if (focusNode != null && focusNode.className == "android.widget.EditText") {
            val args = Bundle()
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            focusNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        } else {
            val clip = ClipData.newPlainText("text", text)
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(clip)
            focusNode?.performAction(AccessibilityNodeInfo.ACTION_PASTE)
        }
    }

    companion object {
        var instance: JarvisAccessibilityService? = null
    }
}
