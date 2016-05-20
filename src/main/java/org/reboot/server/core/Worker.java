package org.reboot.server.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.reboot.server.entity.*;
import org.reboot.server.entity.response.*;
import org.reboot.server.client.*;

import java.util.concurrent.TimeUnit;

class Worker implements Runnable {

    private Socket socket = null;

    private static Logger log = LoggerFactory.getLogger(Worker.class);

    private Integer requestTimeOut = null;

    private Integer responseTimeOut = null;

    public Worker(Socket socket, Integer requestTimeOut, Integer responseTimeOut) {
        this.socket = socket;
        this.requestTimeOut = requestTimeOut;
        this.responseTimeOut = responseTimeOut;
    }

    public void run() {
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            log.info("Processing the request...");
            HttpRequest request = new HttpRequest(in);

            log.info(request.getMethod().toString());
            log.info(request.getResource());
            log.info(request.getHeaders().toString());

            Future <HttpResponse> resp = RequestProcessor.submit(new TestController(), request);
            /*
            String packet = Response._200("hello world");
            packet = Response._500();
            log.info("Sending: " + packet);
            out.write(packet);
            */
        } catch (Exception e) {
            try { out.write(Response._500()); } catch (Exception ie) { log.error(ie.getMessage()); }
            log.error(e.getMessage());
        } finally {
            try { out.close(); } catch (Exception e) { log.error(e.getMessage()); }
            try { in.close(); } catch (Exception e) { log.error(e.getMessage()); }
            try { this.socket.close(); } catch (Exception e) { log.error(e.getMessage()); }
        }
    }

    private HttpResponse getResponse(Future <HttpResponse> future) {
        try {
            return future.get(this.responseTimeOut, TimeUnit.MILLISECONDS);
        //TODO: implement timeout exception
        } catch (Exception e) {

            //TODO: Left at this part

            return null;
        } 
    }
}
