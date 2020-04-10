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

package com.rozdoum.socialcomponents.adapters;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.activities.MainActivity;
import com.rozdoum.socialcomponents.adapters.holders.LoadViewHolder;
import com.rozdoum.socialcomponents.adapters.holders.PostViewHolder;
import com.rozdoum.socialcomponents.controllers.LikeController;
import com.rozdoum.socialcomponents.enums.ItemType;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnPostListChangedListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.PostListResult;
import com.rozdoum.socialcomponents.utils.PreferencesUtil;

import java.util.List;

/**
 * Created by Kristina on 10/31/16.
 */

public class PostsAdapter extends BasePostsAdapter {
    public static final String Attach = PostsAdapter.class.getSimpleName();

    private Callback callback;
    private boolean isitLoaded = false;
    private boolean AvailableData = true;
    private long lastLoadedItemCreatedDate;
    private SwipeRefreshLayout Swipe_refresher;
    private MainActivity activitymain;

    public PostsAdapter(final MainActivity activity, SwipeRefreshLayout Swipe_refresher) {
        super(activity);
        this.activitymain = activity;
        this.Swipe_refresher = Swipe_refresher;
        Layout_Refresher();
        setHasStableIds(true);
    }

    private void Layout_Refresher() {
        if (Swipe_refresher != null) {
            this.Swipe_refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    doActionRefresh();
                }
            });
        }
    }

    private void doActionRefresh() {
        if (activity.has_connection()) {
            showPage_one();
            deletePostInfo();
        } else {
            Swipe_refresher.setRefreshing(false);
            activitymain.display_flaotbutton(R.string.internet_connection_failed);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ItemType.ITEM.getTypeCode()) {
            return new PostViewHolder(inflater.inflate(R.layout.post_item_list_view, parent, false),
                    createOnClickListener());
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
    }

    private PostViewHolder.OnClickListener createOnClickListener() {
        return new PostViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (callback != null) {
                    Posts_positionSelection = position;
                    callback.onItemClick(findIndex(position), view);
                }
            }

            @Override
            public void onLikeClick(LikeController ControlLikes, int position) {
                Post post = findIndex(position);
                ControlLikes.handleLikeClickAction(activity, post);
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callback != null) {
                    callback.onAuthorClick(findIndex(position).getAuthorId(), view);
                }
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= getItemCount() - 1 && AvailableData && !isitLoaded) {
            android.os.Handler mHandler = activity.getWindow().getDecorView().getHandler();
            mHandler.post(new Runnable() {
                public void run() {
                    //change adapter contents
                    if (activity.has_connection()) {
                        isitLoaded = true;
                        Posts.add(new Post(ItemType.LOAD));
                        notifyItemInserted(Posts.size());
                        show_next(lastLoadedItemCreatedDate - 1);
                    } else {
                        activitymain.display_flaotbutton(R.string.internet_connection_failed);
                    }
                }
            });


        }

        if (getItemViewType(position) != ItemType.LOAD.getTypeCode()) {
            ((PostViewHolder) holder).Data_combining(Posts.get(position));
        }
    }

    private void addList(List<Post> list) {
        this.Posts.addAll(list);
        notifyDataSetChanged();
        isitLoaded = false;
    }

    public void showPage_one() {
        show_next(0);
        PostManager.getInstance(activitymain.getApplicationContext()).to_clearcountfor_post();
    }

    private void show_next(final long nextItemCreatedDate) {

        if (!PreferencesUtil.Check_post_loadedOnce(activitymain) && !activity.has_connection()) {
            activitymain.display_flaotbutton(R.string.internet_connection_failed);
            hide_progress();
            callback.onListLoadingFinished();
            return;
        }

        OnPostListChangedListener<Post> onPostsDataChangedListener = new OnPostListChangedListener<Post>() {
            @Override
            public void ChangedIn_List(PostListResult result) {
                lastLoadedItemCreatedDate = result.getLastItemCreatedDate();
                AvailableData = result.AvailableData();
                List<Post> list = result.receive_postss();

                if (nextItemCreatedDate == 0) {
                    Posts.clear();
                    notifyDataSetChanged();
                    Swipe_refresher.setRefreshing(false);
                }

                hide_progress();

                if (!list.isEmpty()) {
                    addList(list);

                    if (!PreferencesUtil.Check_post_loadedOnce(activitymain)) {
                        PreferencesUtil.to_setOnceLoaded_post(activitymain, true);
                    }
                } else {
                    isitLoaded = false;
                }

                callback.onListLoadingFinished();
            }

            @Override
            public void has_cancelled(String message) {
                callback.has_cancelled(message);
            }
        };

        PostManager.getInstance(activity).receive_postssList(onPostsDataChangedListener, nextItemCreatedDate);
    }

    private void hide_progress() {
        if (!Posts.isEmpty() && getItemViewType(Posts.size() - 1) == ItemType.LOAD.getTypeCode()) {
            Posts.remove(Posts.size() - 1);
            notifyItemRemoved(Posts.size() - 1);
        }
    }

    public void removeSelectedPost() {
        Posts.remove(Posts_positionSelection);
        notifyItemRemoved(Posts_positionSelection);
    }

    @Override
    public long getItemId(int position) {
        return findIndex(position).getId().hashCode();
    }

    public interface Callback {
        void onItemClick(Post post, View view);
        void onListLoadingFinished();
        void onAuthorClick(String authorId, View view);
        void has_cancelled(String message);
    }
}
