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

import com.rozdoum.socialcomponents.enums.ItemType;
import com.rozdoum.socialcomponents.utils.FormatterUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kristina on 10/28/16.
 */

public class Post implements Serializable, LazyLoading {

    private String id;
    private String title;
    private String description;
    private long createdDate;
    private String imagePath;
    private String Header_Img;
    private String authorId;
    private long Count_comment;
    private long likesCount;
    private long watchersCount;
    private boolean Complained;
    private ItemType itemType;

    public Post() {
        this.createdDate = new Date().getTime();
        itemType = ItemType.ITEM;
    }

    public Post(ItemType itemType) {
        this.itemType = itemType;
        setId(itemType.toString());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getImageTitle() {
        return Header_Img;
    }

    public void setImageTitle(String Header_Img) {
        this.Header_Img = Header_Img;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getCommentsCount() {
        return Count_comment;
    }

    public void setCommentsCount(long Count_comment) {
        this.Count_comment = Count_comment;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getWatchersCount() {
        return watchersCount;
    }

    public void setWatchersCount(long watchersCount) {
        this.watchersCount = watchersCount;
    }

    public boolean isHasComplain() {
        return Complained;
    }

    public void setHasComplain(boolean Complained) {
        this.Complained = Complained;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("title", title);
        result.put("description", description);
        result.put("createdDate", createdDate);
        result.put("imagePath", imagePath);
        result.put("Header_Img", Header_Img);
        result.put("authorId", authorId);
        result.put("Count_comment", Count_comment);
        result.put("likesCount", likesCount);
        result.put("watchersCount", watchersCount);
        result.put("Complained", Complained);
        result.put("createdDateText", FormatterUtil.to_retrieveFirebase_Date_Format().format(new Date(createdDate)));

        return result;
    }

    @Override
    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public void to_settypeof_item(ItemType itemType) {

    }
}
