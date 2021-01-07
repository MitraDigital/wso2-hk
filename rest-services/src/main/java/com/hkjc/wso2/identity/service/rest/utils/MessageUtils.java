package com.hkjc.wso2.identity.service.rest.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Collection of utils for working with messages at REST services
 */
public class MessageUtils {

    public static class HTTPCallException extends Exception {
        int code = 0;

        HTTPCallException(String message, int code) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }

    }

    ;


    /**
     * Setup error response (with logging).
     *
     * @param request  is used for getting path of resource
     * @param response is used for answer building and delivery
     * @param status   is used for defining of HTTP code
     * @param msg      is used for providing error description
     * @param log      is used for log writing
     * @throws IOException if there are problems with response creation
     */
    public static void setError(HttpServletRequest request,
                                HttpServletResponse response,
                                int status, String msg, Log log) throws IOException {

        //logging
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }

        //body creation in accordance with approved error pattern
        JSONObject body = new JSONObject();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", status);
        body.put("error", Integer.toString(status));
        body.put("message", msg);
        body.put("path", request.getRequestURI());

        //response updating
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(body.toString());
        response.setStatus(status);

    }


    /**
     * Setup success response (with logging).
     *
     * @param response is used for answer building and delivery
     * @param body     can contain json for returning
     * @param log      is used for log writing
     * @throws IOException if there are problems with response creation
     */
    public static void setSuccess(HttpServletResponse response,
                                  JSONObject body, Log log) throws IOException {
        setSuccess(response, body.toString(), log);
    }


    /**
     * Setup success response (with logging).
     *
     * @param response is used for answer building and delivery
     * @param body     can contain json for returning
     * @param log      is used for log writing
     * @throws IOException if there are problems with response creation
     */
    public static void setSuccess(HttpServletResponse response,
                                  String body, Log log) throws IOException {

        //logging
        if (log.isDebugEnabled()) {
            log.debug("Success");
        }

        //response updating
        if (body != null) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(body);
        }
        response.setStatus(HttpServletResponse.SC_OK);

    }

    public static void setRedirect(HttpServletResponse response, Log log,
                                   String location, String lang, String app) {

        //logging
        if (log.isDebugEnabled()) {
            log.debug("Success");
        }

        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.setHeader("Location", location);
        if (StringUtils.isNotEmpty(lang)) {
            response.setHeader("lang", lang);
        }
        if (StringUtils.isNotEmpty(app)) {
            response.setHeader("IDN-App", app);
        }

    }

    /**
     * Deserialization of json request
     *
     * @param request is used for body extraction
     * @return a deserialized object
     * @throws IOException    if there are problems with body reading
     * @throws ParseException if there are problems with request content
     */
    public static JSONObject parseJSONRequest(HttpServletRequest request) throws ParseException, IOException {
        JSONParser parser = new JSONParser();
        StringWriter writer = new StringWriter();
        IOUtils.copy(request.getInputStream(), writer, "UTF-8");
        return (JSONObject) parser.parse(writer.toString());

    }


    /**
     * Deserialization of string to json
     *
     * @param plainText contain the string with json
     * @return a deserialized object
     * @throws ParseException if there are problems with string content
     */
    public static JSONObject parseJSON(String plainText) throws ParseException {
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(plainText);

    }

    public static Map<String, String> mapToClaimsToStringMap(JSONObject input, Map<String, String> claimMap) {
        Map<String, String> claimsResult = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : claimMap.entrySet()) {
            Object inputClaim = (Object) input.get(entry.getValue());
            if (inputClaim != null) {
                claimsResult.put(entry.getKey(), inputClaim.toString());
            }
        }
        return claimsResult;
    }

    public static JSONObject mapFromClaimsToJSONStringsObj(Map<String, String> claims, Map<String, String> claimMap) {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, String> entry : claims.entrySet()) {
            String item = claimMap.get(entry.getKey());
            if (item != null) {
                result.put(item, entry.getValue());
            }
        }
        return result;
    }

//    public static JSONObject sendGetRequest(String url, Map<String,String> params) {
//        RestTemplate restTemplate = new RestTemplate();
//        String response = restTemplate.getForObject(url, );
//        return parseJSON(response);
//    }
//
//    public static JsonNode sendPostRequest(String url, Map<String,String> params, JSONObject body) {
//        RestTemplate restTemplate = new RestTemplate();
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.readValue()
//        String response = restTemplate.getForObject(url, Quote.class);
//        return parseJSON(response);
//    }


    public static Object parseJSONreq(HttpServletRequest request, Class model) throws IOException {
        JSONParser parser = new JSONParser();
        StringWriter writer = new StringWriter();
        IOUtils.copy(request.getInputStream(), writer, "UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(writer.toString(), model);
    }

    public static Object parseJSON(String json, Class model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, model);
    }

    public static String generateJSON(Object data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(data);
    }
}
