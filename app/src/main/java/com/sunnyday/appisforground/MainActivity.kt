package com.sunnyday.appisforground

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.sunnyday.appisforground.utils.AndroidProcessUtil

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "getRunningTask方式--> APP 是否位于前台：${AndroidProcessUtil.getRunningTask(this, packageName)}")
        Log.d(TAG, "getRunningAppProcess方式--> APP 是否位于前台：${AndroidProcessUtil.getRunningAppProcess(this, packageName)}")
        Log.d(TAG,"ActivityLifecycleCallbacks方式-->${AndroidProcessUtil.getApplicationValue(application as MyApplication)}")
        Log.d(TAG,"UsageStateManager方式-->${AndroidProcessUtil.queryUsageState(this,packageName)}")
        Log.d(TAG,"AccessibilityService方式-->${AndroidProcessUtil.getFromAccessibilityService(this,packageName)}")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "getRunningTask方式--> APP 是否位于前台：${AndroidProcessUtil.getRunningTask(this, packageName)}")
        Log.d(TAG, "getRunningAppProcess方式--> APP 是否位于前台：${AndroidProcessUtil.getRunningAppProcess(this, packageName)}")
        Log.d(TAG,"ActivityLifecycleCallbacks方式-->${AndroidProcessUtil.getApplicationValue(application as MyApplication)}")
        Log.d(TAG,"UsageStateManager方式-->${AndroidProcessUtil.queryUsageState(this,packageName)}")
        Log.d(TAG,"AccessibilityService方式-->${AndroidProcessUtil.getFromAccessibilityService(this,packageName)}")
    }
}
