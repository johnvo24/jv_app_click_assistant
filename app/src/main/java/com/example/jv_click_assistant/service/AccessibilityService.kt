package com.example.jv_click_assistant.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.*
import android.view.View.OnTouchListener
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.jv_click_assistant.Helper
import com.example.jv_click_assistant.databinding.CircleForClickLayoutBinding
import com.example.jv_click_assistant.databinding.DashboardLayoutBinding
import com.example.jv_click_assistant.model.Bounds
import com.example.jv_click_assistant.model.NodeClick
import com.example.jv_click_assistant.model.Rectangle
import com.example.jv_click_assistant.model.Swipe
import kotlinx.coroutines.*
import java.util.*

class AccessibilityService : AccessibilityService() {
    lateinit var dashBoardBinding: DashboardLayoutBinding
    lateinit var circleForClickLayoutBinding: CircleForClickLayoutBinding
    lateinit var windowManager: WindowManager
    lateinit var layoutParams: WindowManager.LayoutParams
    lateinit var circleLayoutParam: WindowManager.LayoutParams
    private var isSetting: Boolean = false

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    override fun onServiceConnected() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        dashBoardBinding = DashboardLayoutBinding.inflate(LayoutInflater.from(this))
        circleForClickLayoutBinding = CircleForClickLayoutBinding.inflate(LayoutInflater.from(this))
        layoutParams = Helper.getLayoutParams()
        layoutParams.y = 48
        circleLayoutParam = Helper.getLayoutParams()

        //RUN
        dashBoardBinding.btnPlay.setOnClickListener {
            if (!isSetting) {
                Helper.isRunning(this, true)
                setButtons()
            }
        }
        //STOP
        dashBoardBinding.btnStop.setOnClickListener {
            stopAction()
        }
        //ADD
        dashBoardBinding.btnAdd.setOnClickListener {
//            val click = NodeClick()
//            click.setup("text", "Báo lỗi")
//            click.setup("contentDescription", "Báo lỗi")
//            click.action(this)

//            val swipe = Swipe()
//            swipe.setup("bounds1", Bounds(200, 900, 0, 0))
//            swipe.setup("bounds2", Bounds(200, 200, 0, 0))
//            swipe.action(this)
        }
        //SETTING
        dashBoardBinding.btnSetting.setOnClickListener {
            if (!Helper.isRunning(this) && !isSetting) {
                isSetting = true
                var orderNum = 0
                val posToClickList: MutableList<Rectangle?> = MutableList(5) { null }
                var initX = 0f
                var initY = 0f
                var initTouchX = 0f
                var initTouchY = 0f
                var offsetX = 0f
                var offsetY = 0f

                GlobalScope.launch {
                    var preOrderNum: Int = -1
                    while (isSetting && Helper.isEnabled(applicationContext)) {
                        var odrText: String
                        if (orderNum == 0) {
                            odrText = "Get a Job"
                        } else if (orderNum == 1) {
                            odrText = "Error 4"
                        } else if (orderNum == 2) {
                            odrText = "Error 5"
                        } else if (orderNum == 3) {
                            odrText = "Error 6"
                        } else if (orderNum == 4) {
                            odrText = "Submit report"
                        } else {
                            Helper.runOnUiThread {
                                removeCircleForSetting()
                            }
                            isSetting = false
                            break
                        }
                        if (orderNum != preOrderNum) {
                            preOrderNum = orderNum
                            Helper.runOnUiThread {
                                removeCircleForSetting()
                                addCircleForSetting(orderNum)
                                circleForClickLayoutBinding.orderText.text = odrText
                                initX = circleLayoutParam.x.toFloat()
                                initY = circleLayoutParam.y.toFloat()
                            }
                        }
                        delay(50)
                    }
                    removeCircleForSetting()
                    this.cancel()
                }

                circleForClickLayoutBinding.rectangle.setOnTouchListener(object : OnTouchListener {
                    override fun onTouch(v: View?, event: MotionEvent): Boolean {
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                initTouchX = event.rawX
                                initTouchY = event.rawY
                            }

                            MotionEvent.ACTION_MOVE -> {
                                offsetX = initX + event.rawX - initTouchX
                                offsetY = initY + event.rawY - initTouchY
                                circleLayoutParam.x = offsetX.toInt()
                                circleLayoutParam.y = offsetY.toInt()
                                windowManager.updateViewLayout(
                                    circleForClickLayoutBinding.root,
                                    circleLayoutParam
                                )
                            }

                            MotionEvent.ACTION_UP -> {
                                initX = offsetX
                                initY = offsetY
                            }
                        }
                        return true
                    }
                })
                circleForClickLayoutBinding.btnSettingOk.setOnClickListener {
                    posToClickList[orderNum] = Rectangle(
                        circleLayoutParam.x,
                        circleLayoutParam.y + 72,
                        circleLayoutParam.x + 200,
                        circleLayoutParam.y + 72 + 40
                    )
                    if (orderNum == 4) Helper.setPosToClickList(this, posToClickList)
                    orderNum++
                }

                //CLOSE
                dashBoardBinding.btnClose.setOnClickListener {
                    Helper.isEnabled(this, false)
                    removeView()
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        reloadApp()
                    } else {
                        Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (isSetting) isSetting = false
        }
        //EYE
        dashBoardBinding.btnEye.setOnClickListener {
            // Eye function
        }
        //MOVE
        dashBoardBinding.btnMove.setOnTouchListener(
            object : View.OnTouchListener {
                private var initX = 0f
                private var initY = 0f
                private var initTouchX = 0f
                private var initTouchY = 0f
                private var offsetX = 0f
                private var offsetY = 0f
                private var previousTime: Long = 0
                private var currentTime: Long = 0

                override fun onTouch(view: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            //event.raw*: so với cửa sổ, event.* so với view đang được nhấn
                            initTouchX = event.rawX
                            initTouchY = event.rawY
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val deltaX = event.rawX - initTouchX
                            val deltaY = event.rawY - initTouchY
                            offsetX = initX + deltaX
                            offsetY = initY + deltaY
                            layoutParams.x = offsetX.toInt()
                            layoutParams.y = offsetY.toInt()

                            previousTime = System.nanoTime()
                            windowManager.updateViewLayout(dashBoardBinding.root, layoutParams)
                            //limit the update view (1.5ms once)
                            currentTime = System.nanoTime()
                            if ((currentTime - previousTime) / 1000 > 1500) {
                                windowManager.updateViewLayout(
                                    dashBoardBinding.root,
                                    layoutParams
                                )
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            initX = offsetX
                            initY = offsetY
                        }
                    }
                    return true
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
//        val windows = windows //sử dụng windows cần thêm cờ vào file configservice
        val nodeInfo = rootInActiveWindow
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (event.packageName.toString() == "com.example.jv_click_assistant") {
                    if (Helper.isEnabled(this)) {
                        addDashBoard()
                    } else {
                        removeView()
                    }
                }
            }

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {

            }

            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                println(event.source)
            }
        }
    }

    private fun addDashBoard() {
        if (!dashBoardBinding.root.isShown) {
            windowManager.addView(dashBoardBinding.root, layoutParams)
            setButtons()
        }
    }

    private fun removeDashBoard() {
        if (dashBoardBinding.root.isShown) {
            windowManager.removeView(dashBoardBinding.root)
            Helper.isRunning(this, false)
            setButtons()
        }
    }

    private fun addCircleForSetting(order: Int): Rectangle? {
        var rectObj: Rectangle? = null
        if (!circleForClickLayoutBinding.root.isShown) {
            val posToClickList = Helper.getPosToClickList(this)

            if (posToClickList.isNotEmpty())
                rectObj = posToClickList[order]
            else if (order == 0)
                rectObj = Rectangle(366, 742, 366 + 200, 742 + 40)
            else if (order == 1)
                rectObj = Rectangle(300, 852, 300 + 200, 852 + 40)
            else if (order == 2)
                rectObj = Rectangle(300, 910, 300 + 200, 910 + 40)
            else if (order == 3)
                rectObj = Rectangle(300, 968, 300 + 200, 968 + 40)
            else if (order == 4)
                rectObj = Rectangle(300, 1158, 300 + 200, 1158 + 40)
            circleLayoutParam.x = rectObj!!.fromX
            circleLayoutParam.y = rectObj.fromY - 72
            windowManager.addView(circleForClickLayoutBinding.root, circleLayoutParam)
        }
        return rectObj
    }

    private fun removeCircleForSetting() {
        if (circleForClickLayoutBinding.root.isShown) {
            windowManager.removeView(circleForClickLayoutBinding.root)
        }
    }

    private fun removeView() {
        removeDashBoard()
    }

    private fun reloadApp() {
        sendBroadcast(Intent("com.example.app.RELOAD_ACTIVITY"))
    }

    fun stopAction() {
        Helper.isRunning(this, false)
        setButtons()
    }

    private fun setButtons() {
        if (!Helper.isRunning(this)) {
            dashBoardBinding.btnPlay.visibility = View.VISIBLE
            dashBoardBinding.btnAdd.visibility = View.VISIBLE
            dashBoardBinding.btnSetting.visibility = View.VISIBLE
            dashBoardBinding.btnClose.visibility = View.VISIBLE
            dashBoardBinding.btnStop.visibility = View.GONE
        } else {
            dashBoardBinding.btnPlay.visibility = View.GONE
            dashBoardBinding.btnAdd.visibility = View.GONE
            dashBoardBinding.btnSetting.visibility = View.GONE
            dashBoardBinding.btnClose.visibility = View.GONE
            dashBoardBinding.btnStop.visibility = View.VISIBLE
        }
    }

    //OVERRIDE
    override fun onInterrupt() {
        println("====================SERVICE_INTERRUPT")
        Helper.clearSharedPreferences(this)
    }

    override fun onDestroy() {
        println("====================SERVICE_DESTROY")
        Helper.clearSharedPreferences(this)
        reloadApp()
        super.onDestroy()
    }
}

