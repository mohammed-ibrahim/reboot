package org.reboot.server.route;

import java.util.*;

public class Route {

    private List<Segment> segments;

    public Route(String route) {

        List<String> segmentList = Arrays.asList(route.split("/"));
        List<Segment> formattedSegments = new ArrayList<Segment>();

        for (String segmentPath: segmentList) {
            if (segmentPath.length() > 0) {
                formattedSegments.add(new Segment(segmentPath));
            }
        }

        this.segments = formattedSegments;
    }

    public List<Segment> getSegments() {
        return this.segments;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
}
