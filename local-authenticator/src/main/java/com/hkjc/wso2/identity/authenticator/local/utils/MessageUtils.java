package com.hkjc.wso2.identity.authenticator.local.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Collection of utils for working with messages at REST services
 */
public class MessageUtils {


    public static Map<String, List<String>> getQueryMap(String url) throws UnsupportedEncodingException {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        if (StringUtils.isNotEmpty(url)) {
            int queryPos = url.indexOf("?");
            String query = queryPos > 0 ? url.substring(queryPos + 1) : null;
            if (StringUtils.isEmpty(query)) {
                return map;
            }
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                if (map.containsKey(key)) {
                    map.get(key).add(value);
                } else {
                    map.put(key, Collections.singletonList(value));
                }
            }
        }
        return map;
    }

    public static Map<String, String> getStrongQueryMap(String url) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(url)) {
            int queryPos = url.indexOf("?");
            String query = queryPos > 0 ? url.substring(queryPos + 1) : null;
            if (StringUtils.isEmpty(query)) {
                return map;
            }
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                map.put(key, value);
            }
        }
        return map;
    }

    public static String mapToQuery(Map<String, String> map) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            String value = map.get(key);
            stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
            stringBuilder.append("=");
            stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
        }

        return stringBuilder.toString();
    }

    public static String getClearURL(String url){
        int queryPos = url.indexOf("?");
        return queryPos>0 ? url.substring(0,queryPos) : url;
    }

    public static Object parseJSON(String json, Class model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, model);
    }

}
