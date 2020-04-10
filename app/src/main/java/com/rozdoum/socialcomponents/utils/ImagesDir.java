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
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Kristina on 7/23/14.
 */
public class ImagesDir {

    private static final String PathFor_tempImg = "images/temp";
    private static File imagesTempDir;

    public static File Img_dir_forTempImg(Context context) {
        if (imagesTempDir == null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                imagesTempDir = context.getExternalFilesDir(PathFor_tempImg);
            } else {
                imagesTempDir = context.getCacheDir();
            }
        }

        if (imagesTempDir != null && !imagesTempDir.exists()) {
            imagesTempDir.mkdirs();
        }

        return imagesTempDir;
    }

    public static boolean have_ImgDir(String path, Context context) {
        try {
            return path.startsWith(Img_dir_forTempImg(context).getCanonicalPath());
        } catch (IOException e) {
            LogUtil.to_Log_Error("have_ImgDir", "Failed to get temp images folder", e);
        }

        return false;
    }

    public static void to_clean_dir(File file) {
        //to end the recursive loop
        if (!file.exists())
            return;

        //if directory, go inside and call recursively
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //call recursively
                to_clean_dir(f);
            }
        }
        //call delete to delete files and empty directory
        file.delete();

    }

}
