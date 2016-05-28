package org.reboot.server.route;

import org.reboot.server.client.*;
import java.util.*;

public class MatchedRoute {

    private Route availableRoute;

    private Map<String,String> pathVariables;

    public Route getAvailableRoute() {
        return this.availableRoute;
    }

    public void setAvailableRoute(Route availableRoute) {
        this.availableRoute = availableRoute;
    }

    public Map<String,String> getPathVariables() {
        return this.pathVariables;
    }
 
    public void setPathVariables(Map<String,String> pathVariables) {
        this.pathVariables = pathVariables;
    }

    public MatchedRoute(Route availableRoute, Map<String,String> pathVariables) {
        this.availableRoute = availableRoute;
        this.pathVariables = pathVariables;
    }
}
