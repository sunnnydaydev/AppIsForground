package com.sunnyday.appisforground.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.text.TextUtils
import com.sunnyday.appisforground.MyApplication

/**
 * Create by SunnyDay on 2020/06/03
 */
class AndroidProcessUtil {
    companion object {
        /**
         *@function 通过getRunningTask方式判断APP是否位于前台（适用android5.0以下）
         * @param context context
         * @param pkgName App包名
         * @return boolean 指定包名app是否位于栈顶（app处于前台时此app位于runningTask栈顶）
         * */
        @SuppressLint("NewApi")
        fun getRunningTask(context: Context, pkgName: String): Boolean {
            val activityManager: ActivityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val componentName: ComponentName? = activityManager.getRunningTasks(1)[0].topActivity
            return !TextUtils.isEmpty(pkgName) && pkgName == componentName?.packageName
        }

        /**
         *@function 通过RunningAppProcess方式判断APP是否位于前台（适用android5.0以下）
         * @param context context
         * @param pkgName App包名
         * @return boolean 指定包名app是否位于栈顶（app处于前台时此app位于runningTask栈顶）
         * */
        @SuppressLint("NewApi")
        fun getRunningAppProcess(context: Context, pkgName: String): Boolean {
            val activityManager: ActivityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningAppProcessInfo: List<ActivityManager.RunningAppProcessInfo> =
                activityManager.runningAppProcesses
            for (runningProcess in runningAppProcessInfo) {
                if (runningProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && pkgName == runningProcess.processName
                ) {
                    return true
                }
            }
            return false
        }

        /**
         *@function ActivityLifecycleCallback 方式判断app是否处于前台。
         *@param myApplication 自定义的Application
         * */
        fun getApplicationValue(myApplication: MyApplication): Boolean {
            return myApplication.getAppCount() > 0
        }

        /**
         * @param context
         * @param pkgName
         * */
        fun queryUseageState(context: Context, pkgName: String): Boolean {
            // 定义个比较器，方便对象排序
            class RecentUseComparator : Comparator<UsageStats> {
                override fun compare(o1: UsageStats?, o2: UsageStats?): Int {
                    return when {
                        o1!!.lastTimeUsed > o2!!.lastTimeUsed -> -1
                        o1.lastTimeUsed == o2.lastTimeUsed -> -1
                        else -> 1
                    }
                }
            }

            val recentUseComparator = RecentUseComparator()
            val currentTime = System.currentTimeMillis()
            val usageStateManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val usageStatsList = usageStateManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                currentTime - 1000 * 10,
                currentTime
            )
            if (usageStatsList == null || usageStatsList.size == 0) {
              if (!isHavePermissionForUsageState(context)){
                 // todo 待续
              }
            }
            return false
        }

        /**
         * @function 是否有package usage state 权限
         * @param context
         * */
        private fun isHavePermissionForUsageState(context: Context): Boolean {
            return try {
                val packageManager = context.packageManager
                val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
                val appOpsManager =
                    context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode = appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid,
                    applicationInfo.packageName
                )
                mode == AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                true
            }

        }
    }
}