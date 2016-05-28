package org.reboot.server.route;

import org.reboot.server.client.*;
import java.util.*;

public class Route {

    private List<Segment> segments;

    private Method method;

    public Route(String route, Method method) {

        List<String> segmentList = Arrays.asList(route.split("/"));
        List<Segment> formattedSegments = new ArrayList<Segment>();

        for (String segmentPath: segmentList) {
            if (segmentPath.length() > 0) {
                formattedSegments.add(new Segment(segmentPath));
            }
        }

        this.segments = formattedSegments;
        this.method = method;
    }

    public List<Segment> getSegments() {
        return this.segments;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String toString() {
        return this.method.toString() + ":" + this.segments.toString();
    }
}
