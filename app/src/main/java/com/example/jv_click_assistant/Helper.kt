package com.example.jv_click_assistant

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.example.jv_click_assistant.model.Bounds
import com.example.jv_click_assistant.model.Rectangle
import com.example.jv_click_assistant.model.Step
import com.example.jv_click_assistant.model.Tap
import com.google.gson.Gson
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random

class Helper {
    companion object {
        private lateinit var sharedPreferences: SharedPreferences
//THỜI GIAN
        //Thời gian nghỉ với xác suất
        fun sleep(timeMin: Long, randomErrorTime: Long, probability: Double) {
            val p = Random.nextInt(1, 101)
            val awaitTime = if(1  <= p && p <= probability * 100) {
                Random.nextLong(timeMin, timeMin + 1 + randomErrorTime/2)
            } else {
                Random.nextLong(timeMin, timeMin + 1 + randomErrorTime)
            }
            println("   - Wait: $awaitTime ms")
            Thread.sleep(awaitTime)
        }
        fun convertToTimeString(time: Int): String {
            val h = time/(24*60*60)
            val m = (time%(h*24*60*60))/60*60
            val s = (time%(m*60*60))
            return "${h}h : ${m}m : ${s}s"
        }
        fun getValueWithProbability(fromLimit: Int, untilLimit: Int, fromP: Int, untilP: Int, probability: Double): Int {
            val p = Random.nextInt(1, 101)
            return if(1  <= p && p <= probability * 100) {
                Random.nextInt(fromP, untilP)
            } else {
                Random.nextInt(fromLimit, untilLimit)
            }
        }
        fun showToast(context: Context, text: String) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }
        fun runOnUiThread(logic: () -> Unit) {
            Handler(Looper.getMainLooper()).post {
                logic()
            }
        }
        fun openAppWithPackageName(context: Context, packageManager: PackageManager, packageName: String): Boolean {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                return false
            }
            return true
        }
        fun getLayoutParams(): WindowManager.LayoutParams {
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
            )
            layoutParams.gravity = Gravity.TOP or Gravity.START //quy định gốc tọa độ
            layoutParams.x = 0
            layoutParams.y = 0

            return layoutParams
        }
//SHARED PREFERENCE
        //isEnabled for display overlay
        fun isEnabled(context: Context): Boolean {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("isEnabled", false)
        }
        fun isEnabled(context: Context, isEnabled: Boolean) {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isEnabled", isEnabled).apply()
        }
        //isRunning for auto click
        fun isRunning(context: Context): Boolean {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("isRunning", false)
        }
        fun isRunning(context: Context, isRunning: Boolean) {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isRunning", isRunning).apply()
        }
        //isLoggedIn
        fun isLoggedIn(context: Context): Boolean {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("isLoggedIn", false)
        }
        fun isLoggedIn(context: Context, isLoggedIn: Boolean) {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
        }
        fun getCurrentUsername(context: Context): String {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            return sharedPreferences.getString("currentUsername", "").toString()
        }
        fun setCurrentUsername(context: Context, currentUsername: String) {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("currentUsername", currentUsername).apply()
        }
        fun getLoginToken(context: Context): String {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            return sharedPreferences.getString("token", "").toString()
        }
        fun setLoginToken(context: Context, token: String) {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("token", token).apply()
        }
        // position to click
        fun setPosToClickList(context: Context, posToClickList: MutableList<Rectangle?>) {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant_PosToClickList", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("pos1", Gson().toJson(posToClickList.toList())).apply()
        }
        fun getPosToClickList(context: Context): List<Rectangle> {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant_PosToClickList", Context.MODE_PRIVATE)
            if(sharedPreferences.getString("pos1", "").toString().isEmpty()) return emptyList()
            return Gson().fromJson(sharedPreferences.getString("pos1", "").toString(), Array<Rectangle>::class.java).toList()
        }
        //clear sharedpreferences
        fun clearSharedPreferences(context: Context) {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
        }

        fun saveData(context: Context, list: List<Step>) {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("list", Gson().toJson(list.toList())).apply()
        }
        fun getData(context: Context): List<Step> {
            sharedPreferences = context.getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
            if(sharedPreferences.getString("list", "").toString().isEmpty()) return emptyList()
            return Gson().fromJson(sharedPreferences.getString("list", "").toString(), Array<Step>::class.java).toList()
        }



    //FIND NODE FUNCTIONS
        fun findNode(node: AccessibilityNodeInfo?, attr: String, value: Any?): AccessibilityNodeInfo? {
            if (value == null) return null
            if (node == null) return null
            if (attr == "boundsInParent") {
                val boundsInParent = Rect()
                node.getBoundsInParent(boundsInParent)
                value as Bounds
                if (boundsInParent.left == value.left
                    && boundsInParent.top == value.top
                    && boundsInParent.right == value.left + value.width
                    && boundsInParent.bottom == value.top + value.height)
                    return node
            } else if (attr == "contentDescription" && node.contentDescription != null && node.contentDescription.toString() == value)
                return node
            else if (attr == "text" && node.text != null && node.text.toString() == value)
                return node

            for (i in 0 until node.childCount) {
                val foundNode = findNode(node.getChild(i), attr, value)
                if (foundNode != null) {
                    return foundNode
                }
            }
            return null
        }

    //LOGIN AND REGISTER FUNCTION
        fun hashPassword(password: String): String {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val bytes = messageDigest.digest(password.toByteArray())
            val stringBuilder = StringBuilder()

            for (byte in bytes) {
                stringBuilder.append(String.format("%02x", byte))
            }

            return stringBuilder.toString()
        }

    //TEST FUNCTIONS
    //UTILITIES FUNCTION
        fun generateToken(): String {
            val uuid = UUID.randomUUID()
            return uuid.toString()
        }
    }
}