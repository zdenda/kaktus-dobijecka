package eu.zkkn.android.kaktus.model;

import com.google.api.client.util.Key;


public class FbApiResponse {
    @Key("data")
    public FbApiResponsePost[] posts;
}
