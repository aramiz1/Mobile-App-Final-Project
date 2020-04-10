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

package com.rozdoum.socialcomponents.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.rozdoum.socialcomponents.ApplicationHelper;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.activities.BaseActivity;
import com.rozdoum.socialcomponents.activities.MainActivity;
import com.rozdoum.socialcomponents.enums.ProfileStatus;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.model.Post;

/**
 * Created by Kristina on 12/30/16.
 */

public class LikeController {

    private static final int DurationofAnimation = 300;

    public enum AnimationType {
        Animation_color, Animation_bounce
    }

    private Context context;
    private String postId;
    private String postAuthorId;

    private AnimationType isAnimated_Type = LikeController.AnimationType.Animation_bounce;

    private TextView CountLikes;
    private ImageView likesImageView;

    private boolean isListView = false;

    private boolean isLiked = false;
    private boolean CounterOfLikes_updated = true;

    public LikeController(Context context, Post post, TextView CountLikes,
                          ImageView likesImageView, boolean isListView) {
        this.context = context;
        this.postId = post.getId();
        this.postAuthorId = post.getAuthorId();
        this.CountLikes = CountLikes;
        this.likesImageView = likesImageView;
        this.isListView = isListView;
    }

    public void Clickable(long prevValue) {
        if (!CounterOfLikes_updated) {
            beginAnimation_button(isAnimated_Type);

            if (!isLiked) {
                Like_Inc(prevValue);
            } else {
                Like_removal(prevValue);
            }
        }
    }

    public void ClickableLocal(Post post) {
        Likeupdate_counter(false);
        Clickable(post.getLikesCount());
        Likeupdate_postcounter(post);
    }

    private void Like_Inc(long prevValue) {
        CounterOfLikes_updated = true;
        isLiked = true;
        CountLikes.setText(String.valueOf(prevValue + 1));
        ApplicationHelper.to_DB_helper().createOrUpdateLike(postId, postAuthorId);
    }

    private void Like_removal(long prevValue) {
        CounterOfLikes_updated = true;
        isLiked = false;
        CountLikes.setText(String.valueOf(prevValue - 1));
        ApplicationHelper.to_DB_helper().Like_removal(postId, postAuthorId);
    }

    private void beginAnimation_button(AnimationType animationType) {
        switch (animationType) {
            case Animation_bounce:
                Image_AnimatedBounce();
                break;
            case Animation_color:
                Image_AnimatedColor();
                break;
        }
    }

    private void Image_AnimatedBounce() {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(likesImageView, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(DurationofAnimation);
        bounceAnimX.setInterpolator(new BounceInterpolator());

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(likesImageView, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(DurationofAnimation);
        bounceAnimY.setInterpolator(new BounceInterpolator());
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                likesImageView.setImageResource(!isLiked ? R.drawable.ic_like_active
                        : R.drawable.ic_like);
            }
        });

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY);
        animatorSet.start();
    }

    private void Image_AnimatedColor() {
        final int activatedColor = context.getResources().getColor(R.color.like_icon_activated);

        final ValueAnimator colorAnim = !isLiked ? ObjectAnimator.ofFloat(0f, 1f)
                : ObjectAnimator.ofFloat(1f, 0f);
        colorAnim.setDuration(DurationofAnimation);
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float mul = (Float) animation.getAnimatedValue();
                int alpha = adjustAlpha(activatedColor, mul);
                likesImageView.setColorFilter(alpha, PorterDuff.Mode.SRC_ATOP);
                if (mul == 0.0) {
                    likesImageView.setColorFilter(null);
                }
            }
        });

        colorAnim.start();
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public AnimationType getLikeAnimationType() {
        return isAnimated_Type;
    }

    public void setLikeAnimationType(AnimationType isAnimated_Type) {
        this.isAnimated_Type = isAnimated_Type;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isUpdatingLikeCounter() {
        return CounterOfLikes_updated;
    }

    public void Likeupdate_counter(boolean CounterOfLikes_updated) {
        this.CounterOfLikes_updated = CounterOfLikes_updated;
    }

    public void initLike(boolean isLiked) {
            likesImageView.setImageResource(isLiked ? R.drawable.ic_like_active : R.drawable.ic_like);
            this.isLiked = isLiked;
    }

    private void Likeupdate_postcounter(Post post) {
        if (isLiked) {
            post.setLikesCount(post.getLikesCount() + 1);
        } else {
            post.setLikesCount(post.getLikesCount() - 1);
        }
    }

    public void handleLikeClickAction(final BaseActivity baseActivity, final Post post) {
        PostManager.getInstance(baseActivity.getApplicationContext()).having_exitingpostvalue(post.getId(), new OnObjectExistListener<Post>() {
            @Override
            public void onDataChanged(boolean exist) {
                if (exist) {
                    if (baseActivity.has_connection()) {
                        Action_likeclick(baseActivity, post);
                    } else {
                        Warning_message(baseActivity, R.string.internet_connection_failed);
                    }
                } else {
                    Warning_message(baseActivity, R.string.message_post_was_removed);
                }
            }
        });
    }

    private void Warning_message(BaseActivity baseActivity, int messageId) {
        if (baseActivity instanceof MainActivity) {
            ((MainActivity) baseActivity).display_flaotbutton(messageId);
        } else {
            baseActivity.display_snackBar(messageId);
        }
    }

    private void Action_likeclick(BaseActivity baseActivity, Post post) {
        ProfileStatus profileStatus = ProfileManager.getInstance(baseActivity).to_Profile_check();

        if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
            if (isListView) {
                ClickableLocal(post);
            } else {
                Clickable(post.getLikesCount());
            }
        } else {
            baseActivity.Authorize(profileStatus);
        }
    }
}
