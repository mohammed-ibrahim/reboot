package org.reboot.server.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.reboot.server.client.*;
import java.util.*;

import org.reboot.server.route.*;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.reboot.server.util.*;

import java.net.SocketTimeoutException;
import static org.reboot.server.util.Trace.*;

class Worker implements Runnable {

    private Socket socket = null;

    private static Logger log = LoggerFactory.getLogger(Worker.class);

    private Integer requestTimeOut = null;

    private Integer responseTimeOut = null;

    private List<Route> routes = new ArrayList<Route>();

    public Worker(Socket socket, Integer requestTimeOut, Integer responseTimeOut, List<Route> routes) {
        this.socket = socket;
        this.requestTimeOut = requestTimeOut;
        try { this.socket.setSoTimeout(this.requestTimeOut); } catch (Exception e) { traceError(e); }
        this.responseTimeOut = responseTimeOut;
        this.routes = routes;
    }

    public void run() {
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            log.info("Processing the request...");
            HttpRequest request = new HttpRequest(in);

            log.info("Obtaining controller...");
            Controller controller = getController(request);
            log.info("Controller fetched successfully...");

            log.info(Serializer.getString(request));

            Future <HttpResponse> resp = RequestProcessor.submit(controller, request);
            HttpResponse result = getResponse(resp);
            //log.info("Writing ouput: " + result.getText());
            out.write(result.getText());
        } catch (SocketTimeoutException sto) {
            try { out.write(HttpResponse.REQUEST_TIMEOUT.getText()); } catch (Exception ie) { traceError(ie); }
            log.info("sto");
            log.error(sto.getMessage());
        } catch (MethodNotAllowedException sto) {
            try { out.write(HttpResponse.METHOD_NOT_ALLOWED.getText()); } catch (Exception ie) { traceError(ie); }
            log.info("mto");
            log.error(sto.getMessage());
        } catch (Exception e) {
            try { out.write(HttpResponse.INTERNAL_SERVER_ERROR.getText()); } catch (Exception ie) { traceError(ie); }
            log.info("e");
            traceError(e);
        } finally {
            try { out.close(); } catch (Exception e) { traceError(e); }
            try { in.close(); } catch (Exception e) { traceError(e); }
            try { this.socket.close(); } catch (Exception e) { traceError(e); }
        }
    }

    private Controller getController(HttpRequest request) {
        Route route = new Route(request.getResource(), request.getMethod(), null);
        MatchedRoute matchedRoute = null;

        if (route != null) {
            log.info("Searching for route...");
            matchedRoute = RouteMatcher.getRoute(this.routes, route);

            if (matchedRoute == null) {
                log.info("Route not found!");
                throw new MethodNotAllowedException();
            }
        }

        try { 
            request.setPathVariables(matchedRoute.getPathVariables());
            request.setRouteExtendedData(matchedRoute.getAvailableRoute().getExtendedData());
            return (Controller) matchedRoute.getAvailableRoute().getKlass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Controller not found");
        }
    }

    private HttpResponse getResponse(Future <HttpResponse> future) {
        try {
            return future.get(this.responseTimeOut, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return HttpResponse.SERVER_TIME_OUT;
        } catch (Exception e) {
            return HttpResponse.INTERNAL_SERVER_ERROR;
        }
    }
}
