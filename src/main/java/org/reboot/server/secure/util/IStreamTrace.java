package org.reboot.server.secure.util;

import org.reboot.server.secure.model.TraceContext;

public interface IStreamTrace {

  void start(TraceContext traceContext) throws Exception;
  void addTrace(TraceContext traceContext, byte[] data) throws Exception;

  void addTrace(TraceContext traceContext, byte[] data, int start, int limit) throws Exception;

  void addModifiedTrace(TraceContext traceContext, byte[] actual, int start, int limit, boolean isModified, byte[] modified) throws Exception;

  void end(TraceContext traceContext) throws Exception;
}
