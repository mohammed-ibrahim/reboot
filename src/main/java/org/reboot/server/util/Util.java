package org.reboot.server.util;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Util {

    public static String R201 = "HTTP/1.1 201 OK\r\n";

    public static String R200 = "HTTP/1.1 200 OK\r\nContent-Length: %s\r\n\r\n%s";
}
