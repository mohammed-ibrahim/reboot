package org.reboot.server.entity.response;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import org.reboot.server.util.*;

public class Ok {

    public String generatePacket() {
        return Util.textOk;
    }
}
