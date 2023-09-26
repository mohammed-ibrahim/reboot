package org.reboot.server.secure.core.stream;

import org.reboot.server.secure.model.HttpHeaderContext;
import org.reboot.server.secure.model.StreamContext;

import java.io.OutputStream;

public interface IHeaderProcessor {

  void writeHeader(byte[] data, OutputStream outputStream, HttpHeaderContext httpHeaderContext, StreamContext streamContext) throws Exception ;

}
