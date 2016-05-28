package org.reboot.server.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


public class TimeConsumingController implements Controller {
    public HttpResponse process(HttpRequest request) {
        
        try { Thread.sleep(2500); } catch (Exception e) {  }
        return new HttpResponse(HttpResponse.HTTP_200, "Hello world");
    }
}
