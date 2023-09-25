package org.reboot.server.secure.core.stream;

import org.reboot.server.secure.model.HttpHeaderContext;

import java.io.OutputStream;

public interface IHeaderProcessor {

  void writeHeader(byte[] data, OutputStream outputStream, HttpHeaderContext httpHeaderContext) throws Exception ;

}
