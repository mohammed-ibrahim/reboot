package org.reboot.server.secure.util;

import org.reboot.server.secure.model.StreamType;
import org.reboot.server.secure.model.TraceContext;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

@Component
public class StreamTraceImpl implements IStreamTrace {

  private static byte[] START_BYTES = "=============================START=================================\r\n".getBytes();
//  private static byte[] PRE_MODIFIED = "\r\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\r\n".getBytes();
//  private static byte[] MID_MODIFIED = "\r\n===================================================================\r\n".getBytes();
//  private static byte[] POST_MODIFIED = "\r\n<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\r\n".getBytes();
  private static byte[] END_BYTES = "=============================END=================================\r\n".getBytes();
  @Override
  public void start(TraceContext traceContext) throws Exception {
    if (isEnabled(traceContext)) {
      traceContext.getOutputStream().write(START_BYTES);
    }
  }

  @Override
  public void addTrace(TraceContext traceContext, byte[] data) throws Exception {
    if (isEnabled(traceContext)) {
      traceContext.getOutputStream().write(data);
    }
  }

  @Override
  public void addTrace(TraceContext traceContext, byte[] data, int start, int limit) throws Exception {
    if (isEnabled(traceContext)) {
      traceContext.getOutputStream().write(data, start, limit);
    }
  }

  @Override
  public void addModifiedTrace(TraceContext traceContext, byte[] actual, int start, int limit, boolean isModified, byte[] modified) throws Exception {
    if (isEnabled(traceContext)) {
      if (isModified) {

        StringBuilder sb = new StringBuilder();
        sb.append(new String(actual, start, limit))
            .append(" >>>> ")
            .append(new String(modified));

        traceContext.getOutputStream().write(sb.toString().getBytes());

      } else {
        traceContext.getOutputStream().write(actual, start, limit);
      }
    }
  }

  @Override
  public void end(TraceContext traceContext) throws Exception {
    if (isEnabled(traceContext)) {
      traceContext.getOutputStream().write(END_BYTES);
    }
  }

  private boolean isEnabled(TraceContext traceContext) throws Exception {
    return traceContext.getOutputStream() != null;
  }

//  private OutputStream getStream(TraceContext traceContext) {
//
//    switch (traceContext.getStreamType()) {
//      case REQUEST:
//        return traceContext.getOutputStream();
//
//      case RESPONSE:
//        return traceContext.getResponseStream();
//    }
//
//    throw new RuntimeException("Unknown stream type: " + traceContext.getStreamType());
//  }
}
