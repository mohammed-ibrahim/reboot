package org.reboot.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import org.reboot.server.route.*;
import org.reboot.server.client.*;

class Main {
    public static void main(String []args) {
        Logger log = LoggerFactory.getLogger(Main.class);
        log.info("Booting up....");

        List<Route> routes = new ArrayList<Route>();
        routes.add(new Route("/health", Method.GET, TestController.class));
        routes.add(new Route("/", Method.GET, TestController.class));
        routes.add(new Route("/health/<entity_id>/<server_id>/token", Method.GET, TestController.class));

        log.info(routes.toString());

        Server server = new Server(9899, 5, 2000, 2000, routes);
        server.startServer();
    }
}

