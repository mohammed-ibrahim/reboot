package org.reboot.server.util;

import java.util.*;
import java.io.*;

import org.codehaus.jackson.map.ObjectMapper;

public class Serializer {

    private static ObjectMapper mapper = new ObjectMapper(); 

    public static String getString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "Failed to Serialize: " + e.getMessage();
        }
    }
}
