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

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtil {

    private static final String Attach = PreferencesUtil.class.getSimpleName();

    private static final String to_share_name_preferences = "com.rozdoum.socialcomponents";
    private static final String to_creationOf_Profile = "Check_created_profile";
    private static final String Check_Loaded_Posts = "isPostsWasLoadedAtLeastOnce";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(to_share_name_preferences, Context.MODE_PRIVATE);
    }

    public static Boolean Check_created_profile(Context context) {
        return getSharedPreferences(context).getBoolean(to_creationOf_Profile, false);
    }

    public static Boolean Check_post_loadedOnce(Context context) {
        return getSharedPreferences(context).getBoolean(Check_Loaded_Posts, false);
    }

    public static void to_setprofile(Context context, Boolean Check_created_profile) {
        getSharedPreferences(context).edit().putBoolean(to_creationOf_Profile, Check_created_profile).commit();
    }

    public static void to_setOnceLoaded_post(Context context, Boolean Check_post_loadedOnce) {
        getSharedPreferences(context).edit().putBoolean(Check_Loaded_Posts, Check_post_loadedOnce).commit();
    }

    public static void to_clear_Pref(Context context){
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }
}
