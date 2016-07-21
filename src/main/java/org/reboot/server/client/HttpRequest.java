package org.reboot.server.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.reboot.server.util.*;

import java.util.*;

public class HttpRequest {
    private static Logger log = LoggerFactory.getLogger(HttpRequest.class);

    public static Set<String> methodsWithBody = new HashSet<String>();

    static {
        methodsWithBody.add("POST");
        methodsWithBody.add("PUT");
        methodsWithBody.add("PATCH");
        methodsWithBody.add("DELETE");
    }

    private Method method;

    private String resource;

    private Map<String,String> headers;

    private Map<String,String> requestParams;

    private Map<String,String> pathVariables;

    private String body;

    private Object routeExtendedData;

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getResource() {
        return this.resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Map<String,String> getHeaders() {
        return this.headers;
    }

    public void setHeaders(Map<String,String> headers) {
        this.headers = headers;
    }

    public Map<String,String> getRequestParams() {
        return this.requestParams;
    }

    public void setRequestParams(Map<String,String> requestParams) {
        this.requestParams = requestParams;
    }

    public Map<String,String> getPathVariables() {
        return this.pathVariables;
    }

    public void setPathVariables(Map<String,String> pathVariables) {
        this.pathVariables = pathVariables;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Object getRouteExtendedData() {
        return this.routeExtendedData;
    }

    public void setRouteExtendedData(Object routeExtendedData) {
        this.routeExtendedData = routeExtendedData;
    }

    public String toString() {
        return (this.method + ":" +  this.resource + ":" + this.headers + ":"
            + this.requestParams + ":" + this.pathVariables + ":" + this.body + ":" + safeString(this.routeExtendedData));
    }

    public HttpRequest(BufferedReader in) throws Exception {
        this.headers = new HashMap<String, String>();
        this.requestParams = new HashMap<String, String>();
        this.pathVariables = new HashMap<String, String>();

        String firstLine = in.readLine();
        parseHeader(firstLine);

        String line = in.readLine();
        while (true) {
            if (line == null || line.isEmpty()) {
                break;
            }

            String[] split = line.split(": ");
            headers.put(split[0], split[1]);

            line = in.readLine();
        }

        if (methodsWithBody.contains(this.method.toString())) {
            if (headers.keySet().contains(Util.contentLength)) {
                Integer contentLength = Integer.parseInt(headers.get(Util.contentLength));
                char[] content = new char[contentLength];

                in.read(content, 0, contentLength);
                this.body = new String(content);
            }
        }
    }

    private void parseHeader(String line) {
        String[] headers = line.split(" ");

        if (headers.length != 3) {
            throw new RuntimeException("Invalid first line");
        }

        this.method = Method.valueOf(headers[0]);
        setResourceAndRequestParameters(headers[1]);
        String version = headers[2];
    }

    private String safeString(Object obj) {
        if (obj == null) {
            return "NULL";
        }

        return obj.toString();
    }

    private void setResourceAndRequestParameters(String fullResource) {
        if (fullResource.indexOf("?") == -1) {
            this.resource = fullResource;
            return;
        }

        this.resource = fullResource.substring(0, fullResource.indexOf("?"));
        String params = fullResource.substring(fullResource.indexOf("?")+1);

        if (params.length() > 0) {
            this.requestParams = parseRequestParams(params);
        }
    }

    private Map<String, String> parseRequestParams(String params) {
        Map<String,String> fmtd = new HashMap<String, String>();

        for (String keySet: params.split("&")) {
            String[] kvp = keySet.split("=");

            if (kvp.length == 1) {
                fmtd.put(kvp[0], "");
            } else {
                fmtd.put(kvp[0], kvp[1]);
            }
        }

        return fmtd;
    }
}
