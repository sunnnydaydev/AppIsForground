判断App是否位于前台的6种方案

### 方案总览
| 方法   | 原理                         | 是否需要权限 | 是否可以判断其他请用位于前台 | 缺点                          |
| ---- | -------------------------- | ------ | -------------- | --------------------------- |
| 1    | Running Task               | 否      | 是              | android 5.0开始此方法废弃          |
| 2    | Running Process            | 否      | 是              | 当App存在后台常驻的Service时失效       |
| 3    | ActivityLifecycleCallbacks | 否      | 否              | 简单有效代码少                     |
| 4    | UsageStateManager          | 是      | 是              | 需要用户手动授权                    |
| 5    | 通过安卓无障碍功能实现                | 否      | 是              | 需要用户手动授权                    |
| 6    | 读取/proc目录下的信息              | 否      | 是              | 当proc目录下文件夹过多时,过多的IO操作会引起耗时 |

### 方案一：Running Task

###### 1、代码

```kotlin
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
```

###### 2、原理

> App处于前台时会处于Running Task这和栈的栈顶，取出当前任务栈栈顶任务名（包名），与我们App的包名比较即可。

###### 3、缺点

>android 5.0开始弃用此方法。此方法5.0开始只会返回自己和系统的一些不敏感的task，不再返回其他应用的task，用此方法来判断自身App是否处于后台，仍然是有效的，但是无法判断其他应用是否位于前台，因为不再能获取信息。

###### 4、ActivityManager的 List<RunningTaskInfo> getRunningTasks(int maxNum) 方法补充

> - 此方法会返回正在运行的Task集合，最近使用的app会在list中索引靠前。
> - 参数maxNum指定返回list大小，实际大小取决于正在运行的task数量。判断是否位于栈顶时直接返回一个即可，这个就是栈顶的。
> - android 5.0开始弃用此方法。此方法只会返回自己和系统的一些不敏感的task，不再返回其他应用的task

### 方案二：Running Process

###### 1、代码

```java
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
                if (runningProcess.importance ==                                                                        ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && pkgName == runningProcess.processName
                ){
                    return true
                }
            }
            return false
        }
```



###### 2、原理

> App位于前台时这个app进程的importance属性为ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND。所以如果前台进程名和我们的app一致，并且属性也是这个importance值。那么我们的app就位于前台。

###### 3、缺点

> App 存在后台常驻的service时此方法失败（例如双进程service常驻后台）

### 方案三：ActivityLifecycleCallbacks

###### 1、代码

```kotlin
       /**
         *@function ActivityLifecycleCallback 方式判断app是否处于前台。
         *@param myApplication 自定义的Application
         * */
        fun getApplicationValue(myApplication: MyApplication): Boolean {
            return myApplication.getAppCount() > 0
        }

```

```kotlin

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
```

###### 2、原理

> ActivityLifecycleCallbacks方式，设置个appCount 作为计数器。每当activity可见（onStart时，实际onResume时才位于前台）就认为activity位于前台，appCount+1，activity不可见(onStop)时appCount-1。
>
> - appCount = 0时app位于后台
> - appCount >0时app位于前台

###### 3、优缺点

> 优点：
>
> 1、简单有效,代码最少
>
> 2、当Application因为内存不足而被Kill掉时，这个方法仍然能正常使用。虽然全局变量的值会因此丢失，但是再次进入App时候会重新统计一次的
>
> 3、Application是否被销毁,都不会影响判断的正确性
>
> 缺点：
>
> 1、ActivityLifecycleCallbacks方法在API 14以上有效
>
> 2、需要用户自定义Application并且注册ActivityLifecycleCallbacks接口
>
> 3、不支持多进程下判断

### 方案四、UsageStateManager

###### 1、代码



```java
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
//<uses-permission  android:name="android.permission.PACKAGE_USAGE_STATS" />
```

###### 3、原理

> 通过手机设置里面的usageState功能来获取一段时间内应用使用的统计信息，来判断指定包名应用是否前台

###### 4、优缺点

> 优点：
>
>  可判断其他应用是否位于前台
>
> 缺点：
>
> 1、需要用户授权(打开手机设置，点击安全-高级，在有权查看使用情况的应用中，为这个App打上勾)
>
> 2、此方法只在android5.0以上有效

[参考](https://github.com/wenmingvs/AndroidProcess)