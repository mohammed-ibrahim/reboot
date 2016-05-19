package org.reboot.server.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HttpRequest {
    private static Logger log = LoggerFactory.getLogger(HttpRequest.class);

    public enum Method {
        GET,
        POST,
        PUT,
        PATCH,
        DELETE
    }

    private Method method;

    private String resource = "";

    private Map<String, String> headers = new HashMap<String, String>();

    public Method getMethod() {
        return this.method;
    }

    public String getResource() {
        return this.resource;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public HttpRequest(BufferedReader in) throws Exception {
        String firstLine = in.readLine();
        parseHeader(firstLine);

        String line = in.readLine();
        while (!line.isEmpty()) {
            String[] split = line.split(":");
            headers.put(split[0], split[1]);

            line = in.readLine();
        }
    }

    private void parseHeader(String line) {
        String[] headers = line.split(" ");

        if (headers.length != 3) {
            throw new RuntimeException("Invalid first line");
        }

        this.method = Method.valueOf(headers[0]);
        this.resource = headers[1];
        String version = headers[2];
    }
}
