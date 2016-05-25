package org.reboot.server.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.util.*;

public class HttpResponse {
    public static final Integer HTTP_200 = 200;

    public static final HttpResponse INTERNAL_SERVER_ERROR = new HttpResponse(500, "Internal Server Error");

    private static Map<Integer, String> headerMapping = new HashMap<Integer, String>();

    private static Set<Integer> requestsWithBody = new HashSet<Integer>();

    private static String CR = "\r\n";

    static {
        headerMapping.put(new Integer(200), "OK");
        headerMapping.put(new Integer(201), "OK");
        headerMapping.put(new Integer(400), "BadRequest");
        headerMapping.put(new Integer(401), "Unauthorized");
        headerMapping.put(new Integer(404), "Not Found");
        headerMapping.put(new Integer(500), "Internal Server Error");

        requestsWithBody.add(new Integer(200));
        requestsWithBody.add(new Integer(201));
        requestsWithBody.add(new Integer(400));
        requestsWithBody.add(new Integer(401));
        requestsWithBody.add(new Integer(404));
        requestsWithBody.add(new Integer(500));
    }

    private Integer status = null;

    private String body = null;

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public HttpResponse() {

    }

    public HttpResponse(Integer status) {
        this.status = status;
    }

    public HttpResponse(Integer status, String body) {
        this.status = status;
        this.body = body;
    }

    public String getText() {

        if (requestsWithBody.contains(this.status)) {
            return getPacketedBody();
        }

        return getEmptyPacketBody();
    }

    public String getPacketedBody() {

        return getHeaderLine() + CR + formatHeaders(getDefaultHeaders(), this.body.length()) + CR + this.body.toString();
    }

    public String getEmptyPacketBody() {

        return getHeaderLine() + CR + formatHeaders(getDefaultHeaders(), null) + CR;
    }

    public String formatHeaders(Map<String, String> map, Integer contentLength) {
        StringBuilder sb = new StringBuilder();

        if (contentLength != null) {
            sb.append("Content-Length");
            sb.append(": ");
            sb.append(String.valueOf(contentLength));
            sb.append(CR);
        }

        for (String key: map.keySet()) {
            sb.append(key);
            sb.append(": ");
            sb.append(map.get(key));
            sb.append(CR);
        }

        return sb.toString();
    }

    public String getHeaderLine() {
        if (this.status == null) {
            this.status = new Integer(201);
        }

        String textStat = headerMapping.get(this.status);
        String httpVer = "HTTP/1.1";

        return httpVer + " " + this.status.toString() + " " + textStat ;
    }

    public Map<String, String> getDefaultHeaders() {
        Map<String, String> map = new HashMap<String, String>();

        map.put("Server", "reboot-1.0");

        return map;
    }
}
