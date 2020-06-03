# 判断App是否位于前台的6种方案

### 方案总览
| 方法   | 原理                         | 是否需要权限 | 是否可以判断其他请用位于前台        | 缺点                 |
| ---- | -------------------------- | ------ | --------------------- | ------------------ |
| 1    | Running Task               | 否      | android 5.0以下可以       | android 5.0开始此方法废弃 |
| 2    | Running Process            | 否      | 当App存在后台常驻的Service时失效 | 无                  |
| 3    | ActivityLifecycleCallbacks | 否      | 否                     | 简单有效代码少            |
| 4    | UsageStateManager          | 是      | 是                     | 需要用户手动授权           |
| 5    | 通过安卓无障碍功能实现                | 否      | 是                     | 需要用户手动授权           |
| 6    | 读取/proc目录下的信息              | 否      | 是                     | 需要用户手动授权           |

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

### 方案三：待续。。。

[参考](https://github.com/wenmingvs/AndroidProcess)