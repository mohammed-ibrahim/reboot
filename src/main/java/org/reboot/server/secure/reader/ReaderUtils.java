package org.reboot.server.secure.reader;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReaderUtils {

  private static Logger log = LoggerFactory.getLogger(ReaderUtils.class);
  private static TimeLimiter timeLimiter = SimpleTimeLimiter.create(Executors.newFixedThreadPool(500));
  public static String readLine(BufferedReader bufferedReader) {
    try {

      return timeLimiter.callWithTimeout(() -> {
        try {
          return bufferedReader.readLine();
        } catch (IOException e) {
          log.error("Error while readLine", e);
          throw new RuntimeException(e);
        }
      }, 2, TimeUnit.SECONDS);

    } catch (TimeoutException | UncheckedTimeoutException tex) {
      log.error("Timeout during reading");
      throw new UncheckedTimeoutException(tex);
    } catch (Exception e) {
      log.error("Error while read with timeout: ", e);
      throw new RuntimeException(e);
    }
  }

  public static int read(BufferedReader bufferedReader, char[] buffer, int offset, int pendingBytes) {

    try {

      return timeLimiter.callWithTimeout(() -> {
        try {
          return bufferedReader.read(buffer, offset, pendingBytes);
        } catch (IOException e) {
          log.error("Error while readLine", e);
          throw new RuntimeException(e);
        }
      }, 2, TimeUnit.SECONDS);

    } catch (TimeoutException | UncheckedTimeoutException tex) {
      log.error("Timeout during reading");
      throw new UncheckedTimeoutException(tex);
    } catch (Exception e) {
      log.error("Error while read with timeout: ", e);
      throw new RuntimeException(e);
    }
  }
}
