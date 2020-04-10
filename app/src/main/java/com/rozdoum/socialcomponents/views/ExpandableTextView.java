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

package com.rozdoum.socialcomponents.views;

/**
 * Created by alexey on 21.12.16.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rozdoum.socialcomponents.R;


public class ExpandableTextView extends LinearLayout implements View.OnClickListener {

    private static final String Attach = ExpandableTextView.class.getSimpleName();

    /* The default number of lines */
    private static final int default_num_Lines = 8;

    /* The default animation duration */
    private static final int DurationOfAnimation = 300;

    /* The default alpha value when the animation starts */
    private static final float DurationofStart = 0.7f;

    /* The default text of collapse button */
    private static final String ButtonForCollapse = "show less";

    /* The default text of expand button  */
    private static final String BttnToExpnd = "show more";

    protected TextView Text;

    protected TextView Button; // Button to expand/collapse

    private boolean OutDisplay;

    private boolean Collapse = true; // Show short version as default.

    private int ButtonHeightOfCollapse;

    private int LineHeight;

    private int MaxLine;

    private int MarginbtwnLines;

    private String EnlargeText;

    private String TextClose;

    private int TimerForAnim;

    private float BeginAnim;

    private boolean Animate;

    /* Listener for callback */
    private to_changeOfState_expand mListener;

    /* For saving collapsed status when used in ListView */
    private SparseBooleanArray Stop;
    private int mPosition;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ExpandableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    @Override
    public void setOrientation(int orientation) {
        if (LinearLayout.HORIZONTAL == orientation) {
            throw new IllegalArgumentException("ExpandableTextView only supports Vertical Orientation.");
        }
        super.setOrientation(orientation);
    }

    @Override
    public void onClick(View view) {
        if (Button.getVisibility() != View.VISIBLE) {
            return;
        }

        Collapse = !Collapse;
        Button.setText(Collapse ? EnlargeText : TextClose);

        if (Stop != null) {
            Stop.put(mPosition, Collapse);
        }

        // mark that the animation is in progress
        Animate = true;

        Animation animation;
        if (Collapse) {
            animation = new ExpandCollapseAnimation(this, getHeight(), ButtonHeightOfCollapse);
        } else {
            animation = new ExpandCollapseAnimation(this, getHeight(), getHeight() +
                    LineHeight - Text.getHeight());
        }

        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                applyAlphaAnimation(Text, BeginAnim);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // clear animation here to avoid repeated applyTransformation() calls
                clearAnimation();
                // clear the animation flag
                Animate = false;

                // notify the listener
                if (mListener != null) {
                    mListener.onExpandStateChanged(Text, !Collapse);
                }

                if (Collapse) {
                    Text.setMaxLines(MaxLine);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        clearAnimation();
        startAnimation(animation);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // while an animation is in progress, intercept all the touch events to children to
        // prevent extra clicks during the animation
        return Animate;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        To_views();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // If no change, measure and return
        if (!OutDisplay || getVisibility() == View.GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        OutDisplay = false;

        // Setup with optimistic case
        // i.e. Everything fits. No button needed
        Button.setVisibility(View.GONE);
        Text.setMaxLines(Integer.MAX_VALUE);

        // Measure
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // If the text fits in collapsed mode, we are done.
        if (Text.getLineCount() <= MaxLine) {
            return;
        }

        // Saves the text height w/ max lines
        LineHeight = to_HeightOf_Text(Text);

        // Doesn't fit in collapsed mode. Collapse text view as needed. Show
        // button.
        if (Collapse) {
            Text.setMaxLines(MaxLine);
        }
        Button.setVisibility(View.VISIBLE);

        // Re-measure with new setup
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (Collapse) {
            // Gets the margin between the TextView's bottom and the ViewGroup's bottom
            Text.post(new Runnable() {
                @Override
                public void run() {
                    MarginbtwnLines = getHeight() - Text.getHeight();
                }
            });
            // Saves the collapsed height of this ViewGroup
            ButtonHeightOfCollapse = getMeasuredHeight();
        }
    }

    public void ShowChange_state(@Nullable to_changeOfState_expand listener) {
        mListener = listener;
    }

    public void setText(@Nullable CharSequence text) {
        OutDisplay = true;
        Text.setText(text);
        setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }

    public void setText(@Nullable CharSequence text, @NonNull SparseBooleanArray collapsedStatus, int position) {
        Stop = collapsedStatus;
        mPosition = position;
        boolean isCollapsed = collapsedStatus.get(position, true);
        clearAnimation();
        Collapse = isCollapsed;
        Button.setText(Collapse ? EnlargeText : TextClose);
        setText(text);
        getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        requestLayout();
    }

    @Nullable
    public CharSequence getText() {
        if (Text == null) {
            return "";
        }
        return Text.getText();
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        MaxLine = typedArray.getInt(R.styleable.ExpandableTextView_maxCollapsedLines, default_num_Lines);
        TimerForAnim = typedArray.getInt(R.styleable.ExpandableTextView_animDuration, DurationOfAnimation);
        BeginAnim = typedArray.getFloat(R.styleable.ExpandableTextView_animAlphaStart, DurationofStart);
        EnlargeText = typedArray.getString(R.styleable.ExpandableTextView_expandText);
        TextClose = typedArray.getString(R.styleable.ExpandableTextView_collapseText);

        if (EnlargeText == null) {
            EnlargeText = BttnToExpnd;
        }
        if (TextClose == null) {
            TextClose = ButtonForCollapse;
        }

        typedArray.recycle();

        // enforces vertical orientation
        setOrientation(LinearLayout.VERTICAL);

        // default visibility is gone
        setVisibility(GONE);
    }

    private void To_views() {
        Text = (TextView) findViewById(R.id.expandable_text);
        // TODO: 12.05.17 for enabling expand/collapse comment by click on text remove comment
//        Text.setOnClickListener(this);
        Button = (TextView) findViewById(R.id.expand_collapse);
        Button.setText(Collapse ? EnlargeText : TextClose);
        Button.setOnClickListener(this);
    }

    private static boolean check_post() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void applyAlphaAnimation(View view, float alpha) {
        if (check_post()) {
            view.setAlpha(alpha);
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
            // make it instant
            alphaAnimation.setDuration(0);
            alphaAnimation.setFillAfter(true);
            view.startAnimation(alphaAnimation);
        }
    }

    private static int to_HeightOf_Text(@NonNull TextView textView) {
        int textHeight = textView.getLayout().getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
        return textHeight + padding;
    }

    class ExpandCollapseAnimation extends Animation {
        private final View mTargetView;
        private final int mStartHeight;
        private final int mEndHeight;

        public ExpandCollapseAnimation(View view, int startHeight, int endHeight) {
            mTargetView = view;
            mStartHeight = startHeight;
            mEndHeight = endHeight;
            setDuration(TimerForAnim);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final int newHeight = (int) ((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
            Text.setMaxHeight(newHeight - MarginbtwnLines);

            if (Float.compare(BeginAnim, 1.0f) != 0) {
                applyAlphaAnimation(Text, BeginAnim + interpolatedTime * (1.0f - BeginAnim));
            }

            mTargetView.getLayoutParams().height = newHeight;
            mTargetView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    public interface to_changeOfState_expand {
        /**
         * Called when the expand/collapse animation has been finished
         *
         * @param textView   - TextView being expanded/collapsed
         * @param isExpanded - true if the TextView has been expanded
         */
        void onExpandStateChanged(TextView textView, boolean isExpanded);
    }
}