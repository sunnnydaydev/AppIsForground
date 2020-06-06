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
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.provider.Settings
import android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS
import java.util.*
import kotlin.Comparator



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
         * @function  通过usageStateManager 获取一段时间内应用使用的统计信息，来判断指定包名应用是否前台
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
            )  // 10 s 内应用使用情况
            if (usageStatsList == null || usageStatsList.size == 0) {
                if (!isHavePermissionForUsageState(context)) {
                    val intent = Intent(ACTION_USAGE_ACCESS_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    Toast.makeText(
                        context,
                        "权限不够\n请打开手机设置，点击安全-高级，在有权查看使用情况的应用中，为这个App打上勾",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return false
            }
            Collections.sort(usageStatsList, recentUseComparator)
            val currentTopPackage = usageStatsList[0].packageName
            return currentTopPackage == pkgName
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