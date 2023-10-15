package org.reboot.server.secure.util;

import org.reboot.server.secure.model.StreamHandle;
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
      getStream(traceContext).write(START_BYTES);
    }
  }

  @Override
  public void addTrace(TraceContext traceContext, byte[] data) throws Exception {
    if (isEnabled(traceContext)) {
      getStream(traceContext).write(data);
    }
  }

  @Override
  public void addTrace(TraceContext traceContext, byte[] data, int start, int limit) throws Exception {
    if (isEnabled(traceContext)) {
      getStream(traceContext).write(data, start, limit);
    }
  }

  @Override
  public void addModifiedTrace(TraceContext traceContext, byte[] actual, int start, int limit, boolean isModified, byte[] modified) throws Exception {
    if (isEnabled(traceContext)) {
      if (isModified) {

        StringBuilder sb = new StringBuilder();
        sb.append("<<<< ")
            .append(new String(actual, start, limit))
            .append(" ==== ")
            .append(new String(modified))
            .append(" >>>>");

        getStream(traceContext).write(sb.toString().getBytes());

      } else {
        getStream(traceContext).write(actual, start, limit);
      }
    }
  }

  @Override
  public void end(TraceContext traceContext) throws Exception {
    if (isEnabled(traceContext)) {
      getStream(traceContext).write(END_BYTES);
    }
  }

  private boolean isEnabled(TraceContext traceContext) throws Exception {
    if (traceContext.getStreamType().equals(StreamType.REQUEST)) {
      return traceContext.getRequestStream() != null;
    }

    if (traceContext.getStreamType().equals(StreamType.RESPONSE)) {
      return traceContext.getResponseStream() != null;
    }

    throw new RuntimeException("Unknown stream type: " + traceContext.getStreamType());
  }

  private OutputStream getStream(TraceContext traceContext) {

    switch (traceContext.getStreamType()) {
      case REQUEST:
        return traceContext.getRequestStream();

      case RESPONSE:
        return traceContext.getResponseStream();
    }

    throw new RuntimeException("Unknown stream type: " + traceContext.getStreamType());
  }
}
