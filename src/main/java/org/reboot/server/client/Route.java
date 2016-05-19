package org.reboot.server.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Route {
    private String route;

    private Controller controller;

    public Route(String route, Controller controller) {
        this.route = route;
        this.controller = controller;
    }

    public String getRoute() {
        return this.route;
    }

    public Controller getController() {
        return this.controller;
    }
}
