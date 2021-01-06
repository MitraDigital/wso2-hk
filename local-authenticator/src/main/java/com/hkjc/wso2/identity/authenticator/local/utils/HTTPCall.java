package com.hkjc.wso2.identity.authenticator.local.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

//TODO - extend all features when it's needed
public class HTTPCall {

    private String targetURL;

    ;
    private Map<String, List<String>> query;
    private String body;
    private TraceHeaders trace;
    private Map<String, List<String>> requestHeaders;
    private Map<String, List<String>> responseHeaders;
    private int status = 0;
    private String response;

    public HTTPCall(String targetURL, String body, Map<String, List<String>> headers, Map<String, List<String>> query, TraceHeaders trace) {
        this.targetURL = targetURL;
        this.body = body;
        this.trace = trace;
        this.query = query;
        this.requestHeaders = headers;
    }

    //private Map<String, List<String>> responseHeaders;

    public int getStatus() {
        return status;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public String getResponse() {
        return response;
    }

    /**
     * Method to call REST, tested only with POST and GET
     *
     * @param operation GET, POST
     * @throws IOException
     * @throws HTTPCallException
     */
    public void executeOperation(String operation) throws IOException, HTTPCallException {
        HttpURLConnection connection = null;
        try {
            //prepare
            byte[] rawRequest;
            if (StringUtils.isNotEmpty(body)) {
                rawRequest = body.getBytes("UTF-8");
            } else {
                rawRequest = new byte[0];
            }

            //Create connection
            URL url = new URL(targetURL + serializeQuery(query));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(operation);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setInstanceFollowRedirects(false);
            setupHeaders(connection,requestHeaders);
            trace.enrichRequest(connection);
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            if ("POST".equals(operation)) {
                connection.setRequestProperty("Content-Type",
                        "application/json");
                connection.setRequestProperty("Content-Length",
                        Integer.toString(rawRequest.length));
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(rawRequest);
                }
            }

            //Send body
            connection.connect();

            //Get Response
            responseHeaders = connection.getHeaderFields();
            InputStream is;
            String err = null;
            try {
                is = connection.getInputStream();
            } catch (IOException ioe) {
                err = ioe.toString();
                is = connection.getErrorStream();
            }
            if (is != null) {
                response = readFromStream(is);
                is.close();
            }
            status = connection.getResponseCode();
            if (status < 200 || status >= 400) {
                if (StringUtils.isEmpty(err)){
                    err = "Unexpectable status";
                }
                throw new HTTPCallException(err, this);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void setupHeaders(HttpURLConnection connection, Map<String, List<String>> headers) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    for (String value : entry.getValue()) {
                        connection.setRequestProperty(entry.getKey(), value);
                    }
                }
            }
        }
    }

    private String serializeQuery(Map<String, List<String>> query) {
        StringBuilder result = new StringBuilder();
        if (query != null) {
            for (Map.Entry<String, List<String>> entry : query.entrySet()) {
                if (entry.getValue() != null) {
                    for (String value : entry.getValue()) {
                        result.append("&").append(entry.getKey()).append("=").append(value);
                    }
                }
            }
            if (result.length() > 0) {
                result.replace(0, 1, "?");
            }
        }
        return result.toString();
    }

    private String readFromStream(InputStream is) throws IOException {
        StringBuilder resp = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = rd.readLine()) != null) {
            resp.append(line);
            resp.append('\r');
        }
        return resp.toString();
    }

    public static class HTTPCallException extends Exception {
        HTTPCall result;

        HTTPCallException(String message, HTTPCall result) {
            super(message);
            this.result = result;
        }

        public HTTPCall getResult() {
            return result;
        }
    }


}
