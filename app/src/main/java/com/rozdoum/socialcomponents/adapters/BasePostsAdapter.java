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

package com.rozdoum.socialcomponents.adapters;

import android.support.v7.widget.RecyclerView;

import com.rozdoum.socialcomponents.activities.BaseActivity;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnPostChangedListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.LogUtil;

import java.util.LinkedList;
import java.util.List;

public abstract class BasePostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String Attach = BasePostsAdapter.class.getSimpleName();

    protected List<Post> Posts = new LinkedList<>();
    protected BaseActivity activity;
    protected int Posts_positionSelection = -1;

    public BasePostsAdapter(BaseActivity activity) {
        this.activity = activity;
    }

    protected void deletePostInfo() {
        Posts_positionSelection = -1;
    }

    @Override
    public int getItemCount() {
        return Posts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Posts.get(position).getItemType().getTypeCode();
    }

    protected Post findIndex(int position) {
        return Posts.get(position);
    }

    private OnPostChangedListener createOnPostChangeListener(final int postPosition) {
        return new OnPostChangedListener() {
            @Override
            public void ChangeIn_Object(Post obj) {
                Posts.set(postPosition, obj);
                notifyItemChanged(postPosition);
            }

            @Override
            public void onError(String errorText) {
                LogUtil.to_Log_debug(Attach, errorText);
            }
        };
    }

    public void UpdatePost_selected() {
        if (Posts_positionSelection != -1) {
            Post selectedPost = findIndex(Posts_positionSelection);
            PostManager.getInstance(activity).retrieve_single_postValue(selectedPost.getId(), createOnPostChangeListener(Posts_positionSelection));
        }
    }
}
