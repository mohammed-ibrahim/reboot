package org.reboot.server.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


public class ErroredController implements Controller {
    public HttpResponse process(HttpRequest request) {

        throw new RuntimeException("Dummy error");
    }
}
