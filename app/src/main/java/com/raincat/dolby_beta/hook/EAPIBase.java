package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.google.gson.Gson;
import com.raincat.dolby_beta.db.ExtraDao;
import com.raincat.dolby_beta.model.UserInfo;
import com.raincat.dolby_beta.net.Http;
import com.raincat.dolby_beta.utils.NeteaseAES2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;

class EAPIBase {
    private static final Pattern REX_TYPE = Pattern.compile("\"type\":\\d+");
    private static final Pattern REX_PL = Pattern.compile("\"pl\":(?!999000)\\d+");
    private static final Pattern REX_DL = Pattern.compile("\"dl\":(?!999000)\\d+");
    private static final Pattern REX_SUBP = Pattern.compile("\"subp\":\\d+");

    /**
     * 收藏
     */
    String modifyPlaylistManipulateApi(Context context, HashMap<String, String> data, String original) throws Exception {
        if (original.contains("\"code\":200") && original.contains("\"offlineIds\":[]") && !original.contains("\"trackIds\":\"[]\""))
            return original;

        HashMap<String, Object> header = new HashMap<>();
        header.put("Cookie", ExtraDao.getInstance(context).getExtra("cookie"));

        HashMap<String, Object> param = new HashMap<>();
        String trackIds = (String) data.get("trackIds");
        param.put("op", data.get("op"));
        param.put("pid", data.get("pid"));

        String paramString = NeteaseAES2.Decrypt((String) data.get("params"));
        if (paramString != null && paramString.length() != 0) {
            if (paramString.contains("-36cd479b6b5-")) {
                paramString = paramString.substring(paramString.indexOf("-36cd479b6b5-") + 13);
                paramString = paramString.substring(0, paramString.indexOf("-36cd479b6b5-"));
            }
            JSONObject jsonObject = new JSONObject(paramString);
            trackIds = jsonObject.getString("trackIds");
            param.put("op", jsonObject.getString("op"));
            param.put("pid", jsonObject.getString("pid"));
        }

        String newTrackIds = trackIds.replace("]", "") + trackIds.replace("[", ",");
        param.put("trackIds", newTrackIds);
        String result = new Http("POST", "http://music.163.com/api/playlist/manipulate/tracks", param, header).getResult();
        if (result.contains("502") || result.contains("200"))
            result = "{\"trackIds\":" + trackIds + ",\"code\":200,\"privateCloudStored\":false}";
        return result;
    }

    /**
     * 喜欢
     */
    String modifyLike(Context context, HashMap<String, String> data, String original) throws Exception {
        if (original.contains("\"code\":200"))
            return original;
        String trackId = null;

        HashMap<String, Object> header = new HashMap<>();
        header.put("Cookie", ExtraDao.getInstance(context).getExtra("cookie"));

        //获取我喜欢的音乐列表
        Gson gson = new Gson();
        String paramString = NeteaseAES2.Decrypt((String) data.get("params"));
        if (paramString != null && paramString.length() != 0) {
            if (paramString.contains("-36cd479b6b5-")) {
                paramString = paramString.substring(paramString.indexOf("-36cd479b6b5-") + 13);
                paramString = paramString.substring(0, paramString.indexOf("-36cd479b6b5-"));
            }
            JSONObject jsonObject = new JSONObject(paramString);
            trackId = jsonObject.getString("trackId");
        }

        String userInfoString = new Http("GET", "http://music.163.com/api/v1/user/info", null, header).getResult();
        UserInfo userInfo = gson.fromJson(userInfoString, UserInfo.class);
        String playlistString = new Http("GET", "http://music.163.com/api/user/playlist?limit=1&uid=" + userInfo.getUserPoint().getUserId(), null, header).getResult();
        JSONObject playlistJson = new JSONObject(playlistString);
        JSONArray playlistArray = playlistJson.getJSONArray("playlist");
        String pid = playlistArray.getJSONObject(0).getString("id");

        HashMap<String, Object> param = new HashMap<>();
        param.put("trackIds", "[\"" + trackId + "\",\"" + trackId + "\"]");
        param.put("op", "add");
        param.put("pid", pid);

        String result = new Http("POST", "http://music.163.com/api/playlist/manipulate/tracks", param, header).getResult();
        if (result.contains("502") || result.contains("200"))
            result = "{\"playlistId\":" + pid + ",\"code\":200}";
        return result;
    }

    /**
     * 音效
     */
    String modifyEffect(String originalContent) {
        originalContent = REX_TYPE.matcher(originalContent).replaceAll("\"type\":1");
        return originalContent;
    }

    String modifyByRegex(String originalContent) {
        originalContent = REX_PL.matcher(originalContent).replaceAll("\"pl\":320000");
        originalContent = REX_DL.matcher(originalContent).replaceAll("\"dl\":320000");
        originalContent = REX_SUBP.matcher(originalContent).replaceAll("\"subp\":1");
        return originalContent;
    }
}
