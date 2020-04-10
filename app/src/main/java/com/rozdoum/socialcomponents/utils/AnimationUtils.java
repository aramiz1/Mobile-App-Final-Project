/*
 *  Copyright 2017 Rozdoum
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
 *
 */

package com.rozdoum.socialcomponents.utils;

import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AlphaAnimation;

@SuppressWarnings("UnnecessaryLocalVariable")
public class AnimationUtils {

    public final static int Delay_default = 0;
    public final static int Have_shortDuration = 200;
    public final static int ALPHA_Have_shortDuration = 400;

    /**
     * Reduces the X & Y
     *
     * @param v the view to be scaled
     *
     * @return the ViewPropertyAnimation to manage the animation
     */
    public static ViewPropertyAnimator to_hideScale (View v) {

        ViewPropertyAnimator propertyAnimator = v.animate().setStartDelay(Delay_default).setDuration(Have_shortDuration)
          .scaleX(0).scaleY(0);

        return propertyAnimator;
    }

    /**
     * Shows a view by scaling
     *
     * @param v the view to be scaled
     *
     * @return the ViewPropertyAnimation to manage the animation
     */
    public static ViewPropertyAnimator to_ViewScale (View v) {

        ViewPropertyAnimator propertyAnimator = v.animate().setStartDelay(Delay_default)
            .scaleX(1).scaleY(1);

        return propertyAnimator;
    }

    public static ViewPropertyAnimator to_hideScaleAndVisibility (final View v) {

        ViewPropertyAnimator propertyAnimator = v.animate().setStartDelay(Delay_default).setDuration(Have_shortDuration)
                .scaleX(0).scaleY(0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        v.setVisibility(View.GONE);
                    }
                });

        return propertyAnimator;
    }

    public static AlphaAnimation to_notshowAlpha_view(final View v) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(ALPHA_Have_shortDuration);
        v.setAnimation(alphaAnimation);
        return alphaAnimation;
    }

    public static ViewPropertyAnimator to_ViewScaleAndVisibility (View v) {
        v.setVisibility(View.VISIBLE);

        ViewPropertyAnimator propertyAnimator = v.animate().setStartDelay(Delay_default)
                .scaleX(1).scaleY(1);

        return propertyAnimator;
    }
}
