package org.reboot.server.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class HttpResponse {

    public enum Status {
        HTTP_200,
        HTTP_201,
        HTTP_400,
        HTTP_401,
        HTTP_404,
        HTTP_500
    }

    private Status status;

    private String body;

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public HttpResponse() {

    }

    public HttpResponse(Status status) {
        this.status = status;
    }

    public HttpResponse(Status status, String body) {
        this.status = status;
        this.body = body;
    }
}
