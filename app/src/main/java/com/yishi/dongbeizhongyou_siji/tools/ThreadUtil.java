package com.yishi.dongbeizhongyou_siji.tools;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多线程
 * Created by Administrator on 2017/1/5 0005.
 */

public class ThreadUtil {

    private static ExecutorService executorsInstance;

    public static Executor getExecutor() {
        if (executorsInstance == null) {
            synchronized (ThreadUtil.class) {
                if (executorsInstance == null) {
                    executorsInstance = Executors.newFixedThreadPool(6);
                }
            }
        }
        return executorsInstance;
    }

    public static void runOnThread(Runnable runnable) {
        getExecutor().execute(runnable);
    }
}
