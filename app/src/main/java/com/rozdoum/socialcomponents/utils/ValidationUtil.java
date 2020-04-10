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
import android.graphics.Rect;
import android.net.Uri;

import com.rozdoum.socialcomponents.Constants;

/**
 * Created by Kristina on 8/8/15.
 */
public class ValidationUtil {
    private static final String [] IMAGE_TYPE = new String[]{"jpg", "png", "jpeg", "bmp", "jp2", "psd", "tif", "gif"};

    public static boolean check_valid_email(String email) {
        String stricterFilterString = "[A-Z0-9a-z\\._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}";
        return email.matches(stricterFilterString);
    }

    public static boolean check_ifLettersLatin(String text) {
        String regular = "[a-zA-Z]+";
        return text.matches(regular);
    }

    public static boolean check_Month(String monthString) {
        if (monthString != null) {
            if (monthString.length() != 2) {
                return false;
            }

            int month = Integer.valueOf(monthString);
            if (month >= 1 && month <= 12) {
                return true;
            }
        }
        return false;
    }

    public static boolean check_Year(String monthString) {
        if (monthString != null) {
            if (monthString.length() == 2) {
                return true;
            }
        }
        return false;
    }

    public static boolean check_TitleofPost(String name) {
        return name.length() <= Constants.Post.TitleLengthMax;
    }

    public static boolean check_Name(String name) {
        return name.length() <= Constants.Profile.LengthOfName;
    }

    public static boolean check_Img(Uri uri, Context context) {
        String mimeType = context.getContentResolver().getType(uri);

        if (mimeType != null) {
            return mimeType.contains("image");
        } else {
            String filenameArray[] = uri.getPath().split("\\.");
            String extension = filenameArray[filenameArray.length - 1];

            if (extension != null) {
                for (String type : IMAGE_TYPE) {
                    if (type.toLowerCase().equals(extension.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean check_Extension(String path){
        String extension = null;
        String filenameArray[] = path.split("\\.");

        if (filenameArray.length > 1) {
            extension = filenameArray[filenameArray.length - 1];
        }

        if (extension != null) {
            return true;
        }
        return false;
    }

    public static boolean check_SymbolIf_Invalid(String name){
        return name.contains("@");
    }

    public static boolean check_Img_size(Rect rect) {
        return rect.height() >= Constants.Profile.SizeOfMinAvatar && rect.width() >= Constants.Profile.SizeOfMinAvatar;
    }
}