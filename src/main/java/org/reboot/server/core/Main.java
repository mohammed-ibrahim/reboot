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
        /*
        routes.add(new Route("/v1", Method.GET, TestController.class));
        routes.add(new Route("/v2", Method.GET, TimeConsumingController.class));
        routes.add(new Route("/", Method.GET, ErroredController.class));
        */
        routes.add(new Route("/v1/<term_id>/v2/<location_id>/v3", Method.GET, TestController.class));

        log.info(routes.toString());

        Server server = new Server(9899, 5, 2000, 2000, routes);
        server.startServer();
    }
}

