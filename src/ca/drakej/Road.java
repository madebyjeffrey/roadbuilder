package ca.drakej;

import java.awt.*;

/**
 * Created by Andrew on 12/12/2014.
 */
//class Road: a class that stores (x1,x2)->(x3,x4) (which represents a straight line)
public class Road {
    //(end points)
    Point p1, p2;

    public Road(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public boolean contains(Point p)
    {
        if (p1.equals(p) || p2.equals(p))
            return true;
        else
            return false;
    }
}