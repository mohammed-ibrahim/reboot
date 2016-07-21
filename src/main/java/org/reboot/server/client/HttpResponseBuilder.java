package org.reboot.server.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.util.*;

public class HttpResponseBuilder {
    public static HttpResponse build(BufferedReader br) {
        try {
            return safeBuild(br);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse safeBuild(BufferedReader br) throws Exception {
        List<String> respHeader = parseFirstLine(br.readLine());
        Integer responseCode = Integer.parseInt(respHeader.get(1));

        Map<String, String> headers = new HashMap<String, String>();
        String line = br.readLine();
        while (!line.isEmpty()) {
            String[] split = line.split(": ");
            headers.put(split[0], split[1]);

            line = br.readLine();
        }

        StringBuilder sb = new StringBuilder();
        line = "";
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\r\n");
        }

        if (sb.length() > 0) {
            return new HttpResponse(responseCode, sb.toString());
        } else {
            return new HttpResponse(responseCode);
        }
    }

    private static List<String> parseFirstLine(String line) {
        String[] items = line.split(" ");

        if (items.length != 3) {
            throw new RuntimeException("Malformed Http Header");
        }

        return Arrays.asList(items);
    }
}
