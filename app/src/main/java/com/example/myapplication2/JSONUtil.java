package com.example.myapplication2;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class JSONUtil {

    public static String getUrlFromJson(String json) {
        String url = "";

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            JSONObject dataObject = jsonArray.getJSONObject(0);
            url = dataObject.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return url;
    }
}