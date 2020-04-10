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

package com.rozdoum.socialcomponents;

/**
 * Created by alexey on 08.12.16.
 */

public class Constants {

    public static class Profile {
        public static final int SizeOfMaxAvatar = 1280; //px, side of square
        public static final int SizeOfMinAvatar = 100; //px, side of square
        public static final int LengthOfName = 120;
    }

    public static class Post {
        public static final int TextLengthMax = 300; //characters
        public static final int TitleLengthMax = 255; //characters
        public static final int LimitToPost = 10;
    }

    public static class Database {
        public static final int UploadRetry_InMilli = 60000; //1 minute
    }

    public static class PushNotification {
        public static final int SizeOfLargestIcon = 256; //px
    }
}
