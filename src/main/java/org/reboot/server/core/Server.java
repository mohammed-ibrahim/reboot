package org.reboot.server.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.util.*;

import org.reboot.server.client.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Server {

    private int port = 0;

    private boolean keepRunning = true;

    private ExecutorService executorService = null;

    private static Logger log = LoggerFactory.getLogger(Server.class);

    private List<Route> routes = new ArrayList<Route>();
    
    public Server(int port, int numOfThreads, List<Route> routes) {
        this.port = port;
        this.keepRunning = true;
        this.executorService = Executors.newFixedThreadPool(numOfThreads);
        this.routes = routes;
    }

    public void startServer() {
        try {
            log.info("Starting the server on port: " + port);
            listen();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void listen() throws Exception {
        ServerSocket server = new ServerSocket(port);
        while (this.keepRunning) {
            log.info("Waiting for client...");
            Socket socket = server.accept();
            log.info("Got a new request...");
            this.executorService.submit(new Worker(socket));
        }
    }

    public void stop() {
        this.keepRunning = false;
    }
}
