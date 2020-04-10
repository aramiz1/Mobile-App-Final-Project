/*
 *
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
 *
 */

package com.rozdoum.socialcomponents.adapters.holders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.CommentsAdapter;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.model.Comment;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.FormatterUtil;
import com.rozdoum.socialcomponents.views.ExpandableTextView;

/**
 * Created by alexey on 10.05.17.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    private final ImageView displayPicLabel;
    private final ExpandableTextView CmntLabel;
    private final TextView DateLabel;
    private final ProfileManager ManageProfile;
    private CommentsAdapter.Callback callback;
    private Context context;

    public CommentViewHolder(View itemView, final CommentsAdapter.Callback callback) {
        super(itemView);

        this.callback = callback;
        this.context = itemView.getContext();
        ManageProfile = ProfileManager.getInstance(itemView.getContext().getApplicationContext());

        displayPicLabel = (ImageView) itemView.findViewById(R.id.displayPicLabel);
        CmntLabel = (ExpandableTextView) itemView.findViewById(R.id.commentText);
        DateLabel = (TextView) itemView.findViewById(R.id.DateLabel);

        if (callback != null) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        callback.onLongItemClick(v, position);
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    public void Data_combining(Comment comment) {
        final String authorId = comment.getAuthorId();
        if (authorId != null)
            ManageProfile.retrieve_profilevalue(authorId, createOnProfileChangeListener(CmntLabel,
                    displayPicLabel, comment.getText()));

        CmntLabel.setText(comment.getText());

        CharSequence date = FormatterUtil.getRelativeTimeSpanString(context, comment.getCreatedDate());
        DateLabel.setText(date);

        displayPicLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onAuthorClick(authorId, v);
            }
        });
    }

    private OnObjectChangedListener<Profile> createOnProfileChangeListener(final ExpandableTextView expandableTextView, final ImageView displayPicLabel, final String comment) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void ChangeIn_Object(Profile obj) {
                String userName = obj.getUsername();
                AddComment(userName, comment, expandableTextView);

                if (obj.getPhotoUrl() != null) {
                    Glide.with(context)
                            .load(obj.getPhotoUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .crossFade()
                            .error(R.drawable.ic_stub)
                            .into(displayPicLabel);
                }
            }
        };
    }

    private void AddComment(String userName, String comment, ExpandableTextView CmntLabel) {
        Spannable contentString = new SpannableStringBuilder(userName + "   " + comment);
        contentString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.highlight_text)),
                0, userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        CmntLabel.setText(contentString);
    }
}
