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

package com.rozdoum.socialcomponents.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rozdoum.socialcomponents.Constants;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.controllers.LikeController;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.FormatterUtil;
import com.rozdoum.socialcomponents.utils.Utils;

/**
 * Created by alexey on 27.12.16.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {
    public static final String Attach = PostViewHolder.class.getSimpleName();

    private Context context;
    private ImageView ImagePostLabel;
    private TextView TitleLabel;
    private TextView detailsTextView;
    private TextView CountLikes;
    private ImageView likesImageView;
    private TextView Countcomment;
    private TextView CounterView;
    private TextView DateLabel;
    private ImageView AuthImgLabel;
    private ViewGroup likeViewGroup;

    private ProfileManager ManageProfile;
    private PostManager postManager;

    private LikeController ControlLikes;

    public PostViewHolder(View view, final OnClickListener onClickListener) {
        this(view, onClickListener, true);
    }

    public PostViewHolder(View view, final OnClickListener onClickListener, boolean isAuthorNeeded) {
        super(view);
        this.context = view.getContext();

        ImagePostLabel = (ImageView) view.findViewById(R.id.ImagePostLabel);
        CountLikes = (TextView) view.findViewById(R.id.CountLikes);
        likesImageView = (ImageView) view.findViewById(R.id.likesImageView);
        Countcomment = (TextView) view.findViewById(R.id.Countcomment);
        CounterView = (TextView) view.findViewById(R.id.CounterView);
        DateLabel = (TextView) view.findViewById(R.id.DateLabel);
        TitleLabel = (TextView) view.findViewById(R.id.TitleLabel);
        detailsTextView = (TextView) view.findViewById(R.id.detailsTextView);
        AuthImgLabel = (ImageView) view.findViewById(R.id.AuthImgLabel);
        likeViewGroup = (ViewGroup) view.findViewById(R.id.likesContainer);

        AuthImgLabel.setVisibility(isAuthorNeeded ? View.VISIBLE : View.GONE);

        ManageProfile = ProfileManager.getInstance(context.getApplicationContext());
        postManager = PostManager.getInstance(context.getApplicationContext());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemClick(getAdapterPosition(), v);
                }
            }
        });

        likeViewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onLikeClick(ControlLikes, position);
                }
            }
        });

        AuthImgLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onAuthorClick(getAdapterPosition(), v);
                }
            }
        });
    }

    public void Data_combining(Post post) {

        ControlLikes = new LikeController(context, post, CountLikes, likesImageView, true);

        String title = rmvLine_divider(post.getTitle());
        TitleLabel.setText(title);
        String description = rmvLine_divider(post.getDescription());
        detailsTextView.setText(description);
        CountLikes.setText(String.valueOf(post.getLikesCount()));
        Countcomment.setText(String.valueOf(post.getCommentsCount()));
        CounterView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
        DateLabel.setText(date);

        String imageUrl = post.getImagePath();
        int width = Utils.to_width_display(context);
        int height = (int) context.getResources().getDimension(R.dimen.post_detail_image_height);

        // Displayed and saved to cache image, as needs for post detail.
        Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .error(R.drawable.ic_stub)
                .into(ImagePostLabel);

        if (post.getAuthorId() != null) {
            ManageProfile.retrieve_profilevalue(post.getAuthorId(), createProfileChangeListener(AuthImgLabel));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.User_currentLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private String rmvLine_divider(String text) {
        int decoratedTextLength = text.length() < Constants.Post.TextLengthMax ?
                text.length() : Constants.Post.TextLengthMax;
        return text.substring(0, decoratedTextLength).replaceAll("\n", " ").trim();
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener(final ImageView AuthImgLabel) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void ChangeIn_Object(final Profile obj) {
                if (obj.getPhotoUrl() != null) {

                    Glide.with(context)
                            .load(obj.getPhotoUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .crossFade()
                            .into(AuthImgLabel);
                }
            }
        };
    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return new OnObjectExistListener<Like>() {
            @Override
            public void onDataChanged(boolean exist) {
                ControlLikes.initLike(exist);
            }
        };
    }

    public interface OnClickListener {
        void onItemClick(int position, View view);

        void onLikeClick(LikeController ControlLikes, int position);

        void onAuthorClick(int position, View view);
    }
}