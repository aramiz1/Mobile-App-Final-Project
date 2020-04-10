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

package com.rozdoum.socialcomponents.model;


import java.util.ArrayList;
import java.util.List;

public class PostListResult {
    boolean AvailableData;
    List<Post> posts = new ArrayList<>();
    long Item_date_lastcreated;

    public boolean AvailableData() {
        return AvailableData;
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        AvailableData = moreDataAvailable;
    }

    public List<Post> receive_postss() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public long getLastItemCreatedDate() {
        return Item_date_lastcreated;
    }

    public void setLastItemCreatedDate(long Item_date_lastcreated) {
        this.Item_date_lastcreated = Item_date_lastcreated;
    }
}
