package org.reboot.server.route;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteMatcher {
    private static Logger log = LoggerFactory.getLogger(RouteMatcher.class);

    public static Route getRoute(List<Route> availableRoutes, Route requestRoute) {

        for (Route availableRoute: availableRoutes) {
            Map<String, String> result = match(requestRoute, availableRoute);
            if (result != null) {
                return availableRoute;
            } else {
                log.info("Didn't match: " + availableRoute.toString() + " vs " + requestRoute.toString());
            }
        }

        return null;
    }

    public static Map<String, String> match(Route requestRoute, Route savedRoute) {

        if (!requestRoute.getMethod().equals(savedRoute.getMethod())) {
            log.info("Request method don't match.");
            return null;
        }

        List<Segment> availableRouteSegments = requestRoute.getSegments();
        List<Segment> requestRouteSegments = savedRoute.getSegments();

        if (availableRouteSegments.size() != requestRouteSegments.size()) {
            log.info("Segment size donesn't match.");
            return null;
        }


        int size = availableRouteSegments.size();
        Map<String, String> map = new HashMap<String, String>();

        for (int i=0; i<size; i++) {
            if (availableRouteSegments.get(i).getSegmentType() == Segment.SegmentType.PATH) {
                if (!availableRouteSegments.get(i).getPath().equals(requestRouteSegments.get(i).getPath())) {
                    log.info("Route path donesnt' match.");
                    return null;
                }
            } else {
                String variableName = availableRouteSegments.get(i).getPath();
                String pathValue = requestRouteSegments.get(i).getPath();

                map.put(variableName, pathValue);
            }
        }

        return map;
    }
}
