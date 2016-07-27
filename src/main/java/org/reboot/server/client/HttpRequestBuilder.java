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

public class HttpRequestBuilder {
    public static HttpRequest build(BufferedReader br) throws Exception {
        HttpRequest request = new HttpRequest();

        String baseLine = br.readLine();
        parseBaseLine(request, baseLine);
        setHeaders(request, br);
        request.setPathVariables(new HashMap<String, String>());

        if (request.getHeaders().keySet().contains(Util.contentLength) &&
            Util.isDigit(request.getHeaders().get(Util.contentLength))) {
            parseContent(request, br);
        } else {
            request.setBody(null);
        }

        return request;
    }

    /*Content*/
    public static void parseContent(HttpRequest request, BufferedReader br) throws Exception {
        Integer contentLength = Integer.parseInt(request.getHeaders().get(Util.contentLength));
        char[] content = new char[contentLength];

        br.read(content, 0, contentLength);
        request.setBody(new String(content));
    }

    /*Headers*/
    public static void setHeaders(HttpRequest request, BufferedReader br) throws Exception {
        Map<String, String> headers = new HashMap<String, String>();

        String line = br.readLine();
        while (true) {
            if (line == null || line.isEmpty()) {
                break;
            }

            String[] split = line.split(": ");
            if (split.length < 2) {
                headers.put(split[0], null);
            } else {
                headers.put(split[0], split[1]);
            }

            line = br.readLine();
        }

        request.setHeaders(headers);
    }

    /*Baseline*/
    private static void parseBaseLine(HttpRequest request, String line) {
        String[] parts = line.split(" ");

        if (parts.length != 3) {
            throw new RuntimeException("Invalid first line");
        }

        request.setMethod(Method.valueOf(parts[0]));
        setResourceAndRequestParameters(request, parts[1]);
        String version = parts[2];
    }

    private static void setResourceAndRequestParameters(HttpRequest request, String fullResource) {
        if (fullResource.indexOf("?") == -1) {
            request.setResource(fullResource);
            return;
        }

        request.setResource(fullResource.substring(0, fullResource.indexOf("?")));
        String params = fullResource.substring(fullResource.indexOf("?")+1);

        if (params.length() > 0) {
            request.setRequestParams(parseRequestParams(params));
        }
    }

    private static Map<String, String> parseRequestParams(String params) {
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
