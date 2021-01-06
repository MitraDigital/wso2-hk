package com.hkjc.wso2.identity.authenticator.local.utils;


import java.util.List;
import java.util.Map;

public class CommonUtils {
    public static String getParamFromString(String src, String prefix, String separator){
        String result = null;
        if (src!=null && src.length() > 0){
            int startIndex;
            int endIndex;
            if (src.startsWith(prefix)){
                startIndex = prefix.length();
            } else {
                startIndex = src.indexOf(separator + prefix);
                if (startIndex > -1){
                    startIndex+=separator.length() + prefix.length();
                }
            }
            if (startIndex > -1 && startIndex < src.length()){
                endIndex = src.indexOf(separator,  startIndex);
                if (endIndex != -1){
                    if (endIndex > startIndex) {
                        result = src.substring(startIndex, endIndex);
                    }
                } else {
                    result = src.substring(startIndex);
                }
            }
        }
        return result;
    }

    public static String mapToString(Map<String, String> map, String equalSign, String separator) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(separator);
            }
            String value = map.get(key);
            stringBuilder.append((key != null ? key : ""));
            stringBuilder.append(equalSign);
            stringBuilder.append(value != null ? value : "");
        }

        return stringBuilder.toString();
    }

    public static String listToString(List<String> list, String separator) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String entry : list) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(separator);
            }
            stringBuilder.append(entry);
        }

        return stringBuilder.toString();
    }

}




