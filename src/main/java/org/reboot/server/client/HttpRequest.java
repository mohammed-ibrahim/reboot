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
}
