package com.murphy.pokotalk.activity.event;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.murphy.pokotalk.PokoTalkApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NaverLocationSearchService {
    private static NaverLocationSearchService instance = null;
    private RequestQueue requestQueue;
    private Listener userListener;

    // Rest API url format
    private static final String URL_FORAMT = "https://openapi.naver.com/v1/search/local.json" +
            "?query=%s&display=10&start=1&sort=random";

    private static final String ENCODING = "UTF-8";
    private static final String TAG = "NaverLocationSearch";
    private static final String CLIENT_ID = "pgNQjcP5iIeoIfHL5qN8";
    private static final String CLIENT_SECRET = "PGZKcXseLl";

    public static NaverLocationSearchService getInstance() {
        if (instance == null) {
            instance = new NaverLocationSearchService();
        }

        return instance;
    }

    public NaverLocationSearchService() {
        requestQueue = PokoTalkApp.getInstance().getVolleyRequestQueue();
    }

    public NaverLocationSearchService request(String keyword) {
        try {
            // Encode keyword
            String urlEncode = URLEncoder.encode(keyword, ENCODING);

            // Formatting url
            String url = String.format(URL_FORAMT, urlEncode);

            // Create request
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Log.v("SEARCH", jsonObject.toString());

                            // Parse result json object
                            ArrayList<LocationSearchResult> results = parseResult(jsonObject);

                            // Call user callback
                            if (userListener != null) {
                                userListener.onLocationSearchResult(results);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "server response error " + error.toString());
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    // Create header hash map
                    HashMap<String, String> headers = new HashMap<>();

                    // Put headers for service authorization
                    headers.put("X-Naver-Client-Id", CLIENT_ID);
                    headers.put("X-Naver-Client-Secret", CLIENT_SECRET);

                    return headers;
                }
            };

            // Add to request queue
            requestQueue.add(request);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding");
        }

        return this;
    }

    public NaverLocationSearchService setResultListener(Listener listener) {
        userListener = listener;

        return this;
    }

    public interface Listener {
        void onLocationSearchResult(ArrayList<LocationSearchResult> results);
    }

    public ArrayList<LocationSearchResult> parseResult(JSONObject jsonObject) {
        // Array list for search results
        ArrayList<LocationSearchResult> results = new ArrayList<>();

        try {
            // Get json array that contains all result items
            JSONArray items = jsonObject.getJSONArray("items");

            // Loop for all items
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                // Replace <b> or </b> in title
                String title = item.getString("title");
                title = title.replace("<b>", "")
                        .replace("</b>","");

                // Parse an item
                LocationSearchResult result = new LocationSearchResult()
                        .setTitle(title)
                        .setCategory(item.getString("category"))
                        .setAddress(item.getString("address"))
                        .setRoadAddress(item.getString("roadAddress"))
                        .setDescription(item.getString("description"))
                        .setMapX(Integer.parseInt(item.getString("mapx")))
                        .setMapY(Integer.parseInt(item.getString("mapy")));

                // Add to result list
                results.add(result);
            }
        } catch (JSONException e) {
            Log.e(TAG, "parse error " + e.toString());
        } finally {
            // Finally return results
            return results;
        }
    }
}
