package org.reboot.server.secure.util;


import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class IDGen {

  private static final Logger log = LoggerFactory.getLogger(IDGen.class);

  public static String newId(String ctxPath) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    long nextId = nextId();
    String path = String.format("%06d-%s-%s.log", nextId, cleanPath(ctxPath), getDateStamp());
    log.info("Next id completed in: {} ms path: {}", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS), path);
    return path;
  }


  private static final DateFormat df = new SimpleDateFormat("HH-mm-ss-yyyy-MM-dd"); // Quoted "Z" to indicate UTC, no timezone offset
  private static String getDateStamp() {
    String format = df.format(new Date());
    return format;
  }

  private static String cleanPath(String path) {
    return path.replaceAll("[^a-zA-Z0-9]", "-");
  }

  private static long counter = 0;
  private static synchronized long nextId() {
    counter++;
    return counter;
  }
}
