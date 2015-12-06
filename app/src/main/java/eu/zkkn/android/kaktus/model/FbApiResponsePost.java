package eu.zkkn.android.kaktus.model;

import com.google.api.client.util.Key;


public class FbApiResponsePost {
    @Key
    public String message;
    @Key("created_time")
    public String createdTime;
}
