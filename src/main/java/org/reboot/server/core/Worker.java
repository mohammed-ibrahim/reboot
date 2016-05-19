package org.reboot.server.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.reboot.server.entity.*;
import org.reboot.server.entity.response.*;

class Worker implements Runnable {

    private Socket socket = null;

    private static Logger log = LoggerFactory.getLogger(Worker.class);

    public Worker(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            log.info("Processing the request...");
            Packet pr = new Packet(in);

            log.info(pr.getMethod().toString());
            log.info(pr.getResource());
            log.info(pr.getHeaders().toString());
            out.write((new Ok()).generatePacket());
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            try { out.close(); } catch (Exception e) { }
            try { in.close(); } catch (Exception e) { }
        }
    }
}
