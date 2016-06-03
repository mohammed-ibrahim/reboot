package org.reboot.server.util;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trace {
    private static Logger log = LoggerFactory.getLogger(Trace.class);

    public static void traceError(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log.error(sw.toString());
    }
}
