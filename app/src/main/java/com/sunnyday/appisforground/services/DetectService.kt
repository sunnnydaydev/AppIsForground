package com.sunnyday.appisforground.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.provider.Settings.Secure
import android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
import android.provider.Settings.SettingNotFoundException



/**
 * Create by SunnyDay on 2020/06/07
 */
class DetectService : AccessibilityService() {
    override fun onInterrupt() {}
    /**
     * 监听窗口焦点,并且获取焦点窗口的包名
     * @param event
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            mForegroundPackageName = event.packageName.toString()
        }
    }

    companion object {
        private var mForegroundPackageName = ""

        fun getForegroundPackage(): String {
            return mForegroundPackageName
        }
        /**
         * 判断当前应用的辅助功能服务是否开启
         *
         * @param context
         * @return
         */
        @SuppressLint("DefaultLocale")
        fun isAccessibilitySettingsOn(context: Context): Boolean {
            var accessibilityEnabled = 0
            try {
                accessibilityEnabled = Secure.getInt(
                    context.contentResolver,
                    Secure.ACCESSIBILITY_ENABLED
                )
            } catch (e: SettingNotFoundException) {
               e.printStackTrace()
            }

            if (accessibilityEnabled == 1) {
                val services = Secure.getString(
                    context.contentResolver,
                    ENABLED_ACCESSIBILITY_SERVICES
                )
                if (services != null) {
                    return services.toLowerCase().contains(context.packageName.toLowerCase())
                }
            }
            return false
        }
    }
}