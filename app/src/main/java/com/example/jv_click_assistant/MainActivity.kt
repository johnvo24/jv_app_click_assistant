package com.example.jv_click_assistant

import android.annotation.SuppressLint
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.*
import com.example.jv_click_assistant.databinding.ActivityMainBinding
import com.example.jv_click_assistant.model.Bounds
import com.example.jv_click_assistant.model.Step
import com.example.jv_click_assistant.model.Swipe
import com.example.jv_click_assistant.model.Tap
import com.example.jv_click_assistant.service.AccessibilityService
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val reloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            recreate()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        sharedPreferences = getSharedPreferences("jv_click_assistant", Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)
        if (!Helper.isEnabled(this) && !Helper.isRunning(this)) {
            Helper.isEnabled(this, false)
            Helper.isRunning(this, false)
        }
        setUI()
        // Đăng ký BroadcastReceiver
        val filter = IntentFilter("com.example.app.RELOAD_ACTIVITY")
        registerReceiver(reloadReceiver, filter)

        //SWIPE REFRESH
        mainBinding.swipeRefreshLayout.setOnRefreshListener {
            recreate()
            mainBinding.swipeRefreshLayout.isRefreshing = false
        }
        //START
        mainBinding.btnStart.setOnClickListener {
//            if (!checkService()) {
//                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
//            } else {
//                Helper.isEnabled(applicationContext, true)
//                println(">--------------------------------------ENABLED")
//                setUI()
//            }

            val tap = Tap()
            tap.bounds = Bounds(1, 2, 3, 4)
            val tapString: String = Gson().toJson(tap)
            val swipe = Swipe()
            val swipeString = Gson().toJson(swipe)
            val list: List<Step> = listOf(Step("Tap", tapString), Step("Swipe", swipeString))
//            println(list);
            Helper.saveData(applicationContext, list)
            val result = Helper.getData(applicationContext)
            for (step in result) {
                if (step.type == "Tap") {
                    println(Gson().fromJson(step.objectGsonString, Tap::class.java).toString())
                }
            }
        }
        //END
        mainBinding.btnEnd.setOnClickListener {
            Helper.isEnabled(applicationContext, false)
            println(">--------------------------------------DISABLED")
            setUI()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUI() {
        if (Helper.isEnabled(applicationContext)) {
            mainBinding.btnStart.visibility = View.GONE
            mainBinding.btnEnd.visibility = View.VISIBLE
        } else {
            mainBinding.btnStart.visibility = View.VISIBLE
            mainBinding.btnEnd.visibility = View.GONE
        }
    }

    private fun checkService(): Boolean {
        val service = ComponentName(this, AccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service.flattenToString()) ?: false
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}