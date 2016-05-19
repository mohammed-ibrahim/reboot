package org.reboot.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Main {
    public static void main(String []args) {
        Logger log = LoggerFactory.getLogger(Main.class);
        log.info("hello world");
        
        Server server = new Server(9899, 5);
        server.startServer();
    }
}

