package ca.drakej;

import java.awt.*;

/**
* Created by drakej on 14-12-07.
*/ //class Road: a class that stores (x1,x2)->(x3,x4) (which represents a straight line)
public class Road {
    private Point start, end;

    public Road(Point s, Point f) {
        start = s;
        end = f;
    }

    public double length() {
        return start.distance(end);
    }

    public boolean starts(Point p) {
        return start.equals(p);
    }

    public boolean ends(Point p) {
        return end.equals(p);
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }
}
