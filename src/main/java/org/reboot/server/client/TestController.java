package org.reboot.server.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


public class TestController implements Controller {
    public HttpResponse process(HttpRequest request) {

       return new HttpResponse(HttpResponse.HTTP_200, "Hello world");
    }
}
