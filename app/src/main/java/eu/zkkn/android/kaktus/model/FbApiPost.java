package eu.zkkn.android.kaktus.model;

import com.google.api.client.util.Key;


public class FbApiPost {
    @Key
    public String message;
    @Key("created_time")
    public String createdTime;
    @Key
    public FbApiAttachments attachments;
}
