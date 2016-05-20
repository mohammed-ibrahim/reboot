package org.reboot.server.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.*;

import java.util.*;

import org.reboot.server.client.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RequestProcessor {

    private static ExecutorService executorService = Executors.newFixedThreadPool(20);

    private static Logger log = LoggerFactory.getLogger(RequestProcessor.class);

    public static Future<HttpResponse> submit(final Controller controller, final HttpRequest request) {
        Future<HttpResponse> response = executorService.submit(new Callable<HttpResponse>() {
            public HttpResponse call() {
                return controller.process(request);
            }
        });

        return response;
    }
}
