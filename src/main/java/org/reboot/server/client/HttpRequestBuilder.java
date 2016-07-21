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
    public static HttpResponse build(BufferedReader br) {
        try {
            return safeBuild(br);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse safeBuild(BufferedReader br) throws Exception {
        HttpResponse response = new HttpResponse();

        return response;
    }
}
