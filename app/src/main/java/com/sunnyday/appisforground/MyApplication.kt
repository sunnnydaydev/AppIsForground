package com.sunnyday.appisforground

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Create by SunnyDay on 2020/06/04
 */
class MyApplication : Application() {

    private var appCount = 0

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStarted(activity: Activity) {
               appCount++
            }

            override fun onActivityDestroyed(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityStopped(activity: Activity) {
               appCount--
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityResumed(activity: Activity) {}
        })
    }

    fun getAppCount() = appCount

    fun setAppCount(appCount: Int) {
        this.appCount = appCount
    }
}