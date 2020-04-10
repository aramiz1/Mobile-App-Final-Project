package com.rozdoum.socialcomponents.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;


public class Utils {

    public static int to_width_display(Context context) {
        return getSize(context).x;
    }

    public static int to_height_display(Context context) {
        return getSize(context).y;
    }

    private static Point getSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }
}
