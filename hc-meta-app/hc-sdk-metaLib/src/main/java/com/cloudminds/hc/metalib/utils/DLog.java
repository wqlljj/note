/*
 * Copyright 2016 kk.zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudminds.hc.metalib.utils;

import android.util.Log;

import java.util.Locale;


public class DLog {

    private static String className;
    private static String methodName;
    private static int lineNumber;

    private DLog() {
    }

    public static boolean isDebuggable() {
        return Log.isLoggable("DLog", Log.INFO);
    }

    private static String createLog(String log) {
        return String.format(Locale.ENGLISH, "%06d", Thread.currentThread().getId()) +
                "-(" + className + ":" + lineNumber + ") " + methodName + " " + log;
    }

    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    public static void e(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.e(DLog.class.getSimpleName(), createLog(message));
    }

    public static void e(Exception e) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.e(DLog.class.getSimpleName(), createLog(e.getMessage()));
    }

    public static void d(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.d(className, createLog(message));
    }
}
