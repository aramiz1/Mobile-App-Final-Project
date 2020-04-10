/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.rozdoum.socialcomponents.utils;

import android.util.Log;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kristina on 6/7/16.
 */
public class LogUtil {
    private static final boolean Enabling_Time = true;
    private static final boolean Enabling_Debug = true;
    private static final boolean Enabling_Info = true;
    private static final boolean SearchTitleMaps = false;

    private static final String TIMING = "Timing";

    private static Map<String, Long> timings = new HashMap<String, Long>();

    private static boolean to_beingDebugged() {
        return Enabling_Debug;
    }

    public static void to_Start_log_time(String Attach, String operation) {
        if (to_beingDebugged() && Enabling_Time) {
            timings.put(Attach + operation, new Date().getTime());
            Log.i(TIMING, Attach + ": " + operation + " started");
        }
    }

    public static void to_Stop_log_time(String Attach, String operation) {
        if (to_beingDebugged() && Enabling_Time) {
            if (timings.containsKey(Attach + operation)) {
                Log.i(TIMING, Attach + ": " + operation + " finished for "
                        + (new Date().getTime() - timings.get(Attach + operation)) / 1000 + "sec");
            }
        }
    }

    public static void to_Log_debug(String Attach, String message) {
        if (to_beingDebugged()) {
            Log.d(Attach, message);
        }
    }

    public static void to_Log_Info(String Attach, String message) {
        if (Enabling_Info) {
            Log.i(Attach, message);
        }
    }

    public static void to_Log_searchingTitle(String Attach, String message) {
        if (to_beingDebugged() && SearchTitleMaps) {
            Log.d(Attach, message);
        }
    }

    public static void to_Log_Error(String Attach, String message, Exception e) {
        Log.e(Attach, message, e);
    }

    public static void to_Log_Error(String Attach, String message, Error e) {
        Log.e(Attach, message, e);
    }
}
