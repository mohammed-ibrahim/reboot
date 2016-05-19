package org.reboot.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import org.reboot.server.client.*;

class Main {
    public static void main(String []args) {
        Logger log = LoggerFactory.getLogger(Main.class);
        log.info("hello world");

        List<Route> routes = new ArrayList<Route>();
        routes.add(new Route("/health", new TestController())); 

        Server server = new Server(9899, 5, routes);
        server.startServer();
    }
}

