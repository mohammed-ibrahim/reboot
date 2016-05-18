package org.reboot.server;

import java.net.ServerSocket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

class Server {

    private int port = 0;

    private boolean keepRunning = true;

    private ExecutorService executorService = null;
    
    public Server(int port, int numOfThreads) {
        this.port = port;
        this.keepRunning = true;
        this.executorService = Executors.newFixedThreadPool(numOfThreads)
    }

    public void startServer() {
        try {
            listen();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void listen() {
        ServerSocket server = new ServerSocket(port);
        while (this.keepRunning) {
            Socket socket = server.accept();
        }
    }

    public void stop() {
        this.keepRunning = false;
    }
}
