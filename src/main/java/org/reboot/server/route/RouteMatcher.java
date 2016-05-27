package org.reboot.server.route;

import java.util.*;

public class RouteMatcher {
    public static Map<String, String> match(Route srcRoute, Route destRoute) {

        List<Segment> src = srcRoute.getSegments();
        List<Segment> dest = destRoute.getSegments();

        if (src.size() != dest.size()) {
            return null;
        }

        int size = src.size();
        Map<String, String> map = new HashMap<String, String>();

        for (int i=0; i<size; i++) {
            if (src.get(i).getSegmentType() == Segment.SegmentType.PATH) {
                if (src.get(i).getPath() != dest.get(i).getPath()) {
                    return null;
                }
            } else {
                String variableName = src.get(i).getPath();
                String pathValue = dest.get(i).getPath();

                map.put(variableName, pathValue);
            }
        }

        return map;
    }
}
