package org.reboot.server.route;

import org.reboot.server.client.*;
import java.util.*;

public class Route {

    private List<Segment> segments;

    private Method method;

    private Class klass;

    private Object extendedData;

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

    public Class getKlass() {
        return this.klass;
    }

    public void setKlass(Class klass) {
        this.klass = klass;
    }

    public Object getExtendedData() {
        return this.extendedData;
    }

    public void setExtendedData(Object extendedData) {
        this.extendedData = extendedData;
    }

    public Route(String route, Method method, Class klass) {

        String cleanedRoute = route;
        if (route.indexOf("?") != -1) {
            cleanedRoute = route.substring(0, route.indexOf("?"));
        }
        List<String> segmentList = Arrays.asList(cleanedRoute.split("/"));
        List<Segment> formattedSegments = new ArrayList<Segment>();

        for (String segmentPath: segmentList) {
            if (segmentPath.length() > 0) {
                formattedSegments.add(new Segment(segmentPath));
            }
        }

        this.segments = formattedSegments;
        this.method = method;
        this.klass = klass;
    }

    public Route(String route, Method method, Class klass, Object extendedData) {
        this(route, method, klass);
        this.extendedData = extendedData;
    }
}
