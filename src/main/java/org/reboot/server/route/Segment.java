package org.reboot.server.route;

public class Segment {

    public enum SegmentType {
        PATH,
        PATH_VARIABLE
    }

    private String path;

    private SegmentType segmentType;

    public Segment(String keyPath) {
        if (keyPath.length() < 1) {
            throw new RuntimeException(keyPath + " is invalid path");
        }

        if (keyPath.length() > 2) {

            String startChar = keyPath.substring(0,1);
            String endChar = keyPath.substring(keyPath.length()-2, keyPath.length()-1);

            if (startChar.equals("<") && endChar.equals(">")) {
                this.path = stripEdges(keyPath);
                this.segmentType = SegmentType.PATH_VARIABLE;
            } else {
                this.path = keyPath;
                this.segmentType = SegmentType.PATH;
            }

        } else {
            this.path = keyPath;
            this.segmentType = SegmentType.PATH;
        }
    }

    private String stripEdges(String text) {
        return text.substring(1, text.length()-1);
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public SegmentType getSegmentType() {
        return this.segmentType;
    }

    public void setSegmentType(SegmentType segmentType) {
        this.segmentType = segmentType;
    }

    public String toString() {
        return this.segmentType.toString() + " " + this.path;
    }
}
