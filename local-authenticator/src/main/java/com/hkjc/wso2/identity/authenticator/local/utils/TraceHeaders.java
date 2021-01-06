package com.hkjc.wso2.identity.authenticator.local.utils;

import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletRequest;

public class TraceHeaders {
    private String xRequestId;
    private String xGlobalRequestId;
    private String xB3Parentspanid;
    private String xB3Traceid;
    private String marker;

    /**
     * Processing of trace headers (extracting from request).
     *
     * @param request is used for extracting
     */
    public TraceHeaders(HttpServletRequest request){
        StringBuilder traceLog = new StringBuilder();
        traceLog.append(" [");
        xRequestId = checkTracingHeader(request,"x-request-id", traceLog);
        xGlobalRequestId = checkTracingHeader(request,"x-global-request-id", traceLog);
        xB3Parentspanid = checkTracingHeader(request,"x-b3-parentspanid", traceLog);
        xB3Traceid = checkTracingHeader(request,"x-b3-traceid", traceLog);
        traceLog.append("] ");
        marker = traceLog.toString();
    }

    /**
     * Processing of trace headers (initializing from string).
     *
     * @param initialTrace is used for extracting
     */
    public TraceHeaders(String initialTrace){
        StringBuilder traceLog = new StringBuilder();
        traceLog.append(" [");
        xRequestId = initialTrace;
        xGlobalRequestId = initialTrace;
        xB3Parentspanid = initialTrace;
        xB3Traceid = initialTrace;
        traceLog.append("] ");
        marker = traceLog.toString();
    }

    public String toString(){
        return marker;
    }

    public void enrichRequest(HttpURLConnection con){
        if (xRequestId!= null) {
            con.setRequestProperty("x-request-id", xRequestId);
        }
        if (xGlobalRequestId!= null) {
            con.setRequestProperty("x-global-request-id", xGlobalRequestId);
        }
        if (xB3Parentspanid!= null) {
            con.setRequestProperty("x-b3-parentspanid", xB3Parentspanid);
        }
        if (xB3Traceid!= null) {
            con.setRequestProperty("x-b3-traceid", xB3Traceid);
        }
    }

    /**
     * Copying of header value to response header and tracelog
     * @param request is used for extracting
     * @param name is name of header
     * @param collector is extracted value collector
     */
    private static String checkTracingHeader(HttpServletRequest request,
                                           String name, StringBuilder collector){
        String header = request.getHeader(name);
        if (header != null){
            collector.append(name).append(":").append(header).append(" ");
        }
        return header;
    }

}
