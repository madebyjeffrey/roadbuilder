
package ca.drakej;

import javafx.util.Pair;

import java.awt.*;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.*; //added by AH

public class Map {
    private int width;
    private int height;
    private List<Point> cities = new ArrayList<Point>();    //modified by AH    //private Point[] cities;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void generateCities(int count) {
        for (int i = 0; i < count; ++i) {
            cities.add(generateCity());//modified by AH    //cities[i] = generateCity();
        }
    }




    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%d %d %d\n", cities.size(), this.width, this.height));
        Point[] cities = new Point[this.cities.size()];
        this.cities.toArray(cities);
        Arrays.stream(cities).forEach(x -> sb.append(String.format("%d %d\n", x.x, x.y)));
        return sb.toString();
    }

    public static Map fromReader(Reader reader) {
        BufferedReader br = new BufferedReader(reader);
        String[] lines = br.lines().toArray(String[]::new);
        String[] meta = lines[0].split(" ");

        Map map = new Map(Integer.parseInt(meta[1]), Integer.parseInt(meta[2]));
        int citiesLength = Integer.parseInt(meta[0]);//map.cities = new Point[Integer.parseInt(meta[0])];

        for (int i = 0; i < citiesLength; ++i) {
            meta = lines[i + 1].split(" ");
            map.cities.add(new Point(Integer.parseInt(meta[0]), Integer.parseInt(meta[1])));
        }

        return map;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Point[] getCities() {
        Point[] returnMe = new Point[cities.size()];
        cities.toArray(returnMe);
        return returnMe;
    }

    public void setCities(Point[] cities) {

        this.cities.clear();
        for (int i = 0; i < cities.length; i++) {
            this.cities.add(cities[i]);
        }
    }

    private boolean hasCity(Point city) {
//        return Arrays.stream(cities).filter(x -> x != null && x.equals(city)).count() != 0;
        return Arrays.stream(cities.toArray()).filter(Objects::nonNull).anyMatch(x -> x.equals(city));

    }

    private Point generateCity() {
        Random rnd = new Random();
        while (true) {
            Point x = new Point(rnd.nextInt(width), rnd.nextInt(height));
            if (hasCity(x)) continue;
            else return x;
        }
    }

    //class Road: a class that stores (x1,x2)->(x3,x4) (which represents a straight line)
    public class Road {
        public int x1, x2, x3, x4;

        public Road(int x1, int x2, int x3, int x4) {
            this.x1 = x1;
            this.x2 = x2;
            this.x3 = x3;
            this.x4 = x4;
        }

        public boolean contains(int x, int y)
        {
            if (x1 == x && x2 == y)
                return true;
            else if (x3 == x && x4 == y)
                return true;
            else
                return false;
        }
    }


    //class RoadNetworkSolution: a class that stores a list of roads, plus score calculation for comparison (an "agent" or "gene sequence" in population)
    public class RoadNetworkSolution implements Comparable<RoadNetworkSolution> {
        public List<Road> roadNetwork = new ArrayList<Road>();

        public float score01 = 0f;
        public float score02 = 0f;
        public float score = 0f;

        public int removeDuplicates() {
            int sizeOfList = roadNetwork.size();
            HashSet hs = new HashSet();
            hs.addAll(roadNetwork);
            roadNetwork.clear();
            roadNetwork.addAll(hs);
            return sizeOfList - roadNetwork.size();
        }

        public void removeDeadEnds()
        {
            ArrayList<Integer> indexToRemove = new ArrayList<Integer>();
            for (int i = 0; i < roadNetwork.size(); i++) {
                boolean isDeadEnd = true;
                Point p1 = new Point(roadNetwork.get(i).x1,roadNetwork.get(i).x2);
                for (int j = 0; j < roadNetwork.size(); j++)
                {
                    if (i != j){
                        Point p2 = new Point(roadNetwork.get(j).x1,roadNetwork.get(j).x2);
                        if (p1 == p2)
                            isDeadEnd = false;
                        p2 = new Point(roadNetwork.get(j).x3,roadNetwork.get(j).x4);
                        if (p1 == p2)
                            isDeadEnd = false;
                    }
                }
                for (int j = 0; j < cities.size(); j++)
                {
                    if (p1 == cities.get(j))
                        isDeadEnd = false;
                }
                if (isDeadEnd)
                    indexToRemove.add(i);
                isDeadEnd = true;
                p1 = new Point(roadNetwork.get(i).x3,roadNetwork.get(i).x4);
                for (int j = 0; j < roadNetwork.size(); j++)
                {
                    if (i != j){
                        Point p2 = new Point(roadNetwork.get(j).x1,roadNetwork.get(j).x2);
                        if (p1 == p2)
                            isDeadEnd = false;
                        p2 = new Point(roadNetwork.get(j).x3,roadNetwork.get(j).x4);
                        if (p1 == p2)
                            isDeadEnd = false;
                    }
                }
                for (int j = 0; j < cities.size(); j++)
                {
                    if (p1 == cities.get(j))
                        isDeadEnd = false;
                }
                if (isDeadEnd)
                    indexToRemove.add(i);
            }
            for (int i = 0; i < indexToRemove.size(); i++)
                roadNetwork.remove(indexToRemove.get(i));
        }

        //public Set<Point> myIntersections = new HashSet<Point>();
        public Set<Pair<Float,Float>> myIntersections = new HashSet<Pair<Float,Float>>();
        public void updateIntersections()
        {
            System.out.println("updating intersections");
            int totalInt = 0;
            Road r1 = null, r2 = null;
            float interX = -1, interY = -1;

            myIntersections.clear();
            do {
                interX = -1;
                interY = -1;
                r1 = null;
                r2 = null;
                for (int i = 0; i < roadNetwork.size(); i++) {
                    float x1 = roadNetwork.get(i).x1;
                    float y1 = roadNetwork.get(i).x2;
                    float x2 = roadNetwork.get(i).x3;
                    float y2 = roadNetwork.get(i).x4;
                    for (int j = 0; j < roadNetwork.size(); j++) {
                        if (i != j) {
                            //does this road intersect with current road?
                            float x3 = roadNetwork.get(j).x1;
                            float y3 = roadNetwork.get(j).x2;
                            float x4 = roadNetwork.get(j).x3;
                            float y4 = roadNetwork.get(j).x4;
                            if ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4) == 0)
                                continue;
                            //float intersectx = (((((x1 * y2) - (y1 * x2)) * (x3 - x4))
                             //       - ((x1 - x2) * ((x3 * y4) - (y3 * x4))) /
                              //      (((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4)))));
                            float intersectx = (((x1*y2 - y1*x2)*(x3-x4))-((x1-x2)*(x3*y4-y3*x4)))
                                    / (((x1-x2)*(y3-y4))-((y1-y2)*(x3-x4)));
                            float intersecty = (((((x1 * y2) - (y1 * x2)) * (y3 - y4)) -
                                    ((y1 - y2) * ((x3 * y4) - (y3 * x4)))) /
                                    (((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4))));
                            //System.out.println("from " + x1 + "," + y1 + " and " + x2 + ","+ y2 +
                            //        " to " + x3 + "," + y3 + " and " + x4 + ","+ y4
                            //        +" intersect would be at " + (int)intersectx + " " + (int)intersecty);
                            if (((intersectx > x1 && intersectx < x2)
                                    || (intersectx < x1 && intersectx > x2)) &&
                                ((intersectx > x3 && intersectx < x4)
                                        || (intersectx < x3 && intersectx > x4)) &&
                                    ((intersecty > y1 && intersecty < y2)
                                    || (intersecty < y1 && intersecty > y2)) &&
                                    ((intersecty > y3 && intersecty < y4)
                                            || (intersecty < y3 && intersecty > y4))
                                    && (!roadNetwork.get(i).contains((int)intersectx,(int)intersecty)
                                    || !roadNetwork.get(j).contains((int)intersectx,(int)intersecty)))
                            {
                                    //THERE IS INTERSECTION! SPLIT THE ROAD! (later)
                                    interX = intersectx;
                                    interY = intersecty;
                                    r1 = roadNetwork.get(i);
                                    r2 = roadNetwork.get(j);
                                    //System.out.println("intersect at " + interX + " " + interY);
                                    break;
                                    //totalInt++;
                                    //myIntersections.add(new Pair(interX,interY));
                            }
                        }
                    }
                    if (interX != -1)
                        break;
                }

                //THERE IS INTERSECTION! SPLIT THE ROAD!
                if (r1 != null && r2 != null) {
                    roadNetwork.remove(r1);
                    roadNetwork.remove(r2);
                    Road newRoad1 = new Road(r1.x1, r1.x2, (int)interX, (int)interY);
                    Road newRoad2 = new Road(r1.x3, r1.x4, (int)interX, (int)interY);
                    Road newRoad3 = new Road(r2.x1, r2.x2, (int)interX, (int)interY);
                    Road newRoad4 = new Road(r2.x3, r2.x4, (int)interX, (int)interY);
                    roadNetwork.add(newRoad1);
                    roadNetwork.add(newRoad2);
                    roadNetwork.add(newRoad3);
                    roadNetwork.add(newRoad4);
                   //totalInt++;
                    totalInt = 0;
                }
            }while(interX != -1);
            //System.out.println("I found " + myIntersections.size() + " intersections.");
            //System.out.println("it is " + myIntersections);
        }


        public int compareTo(RoadNetworkSolution r2)
        {
            if (this.score < r2.score)
                return -1;
            else if (this.score > r2.score)
                return 1;
            else
                return 0;
        }

        public void addMutation()
        {
            RoadNetworkSolution newSolution = new RoadNetworkSolution();
            for (int i = 0; i < roadNetwork.size(); i++)
            {
                newSolution.roadNetwork.add(new Road(roadNetwork.get(i).x1, roadNetwork.get(i).x2,
                        roadNetwork.get(i).x3, roadNetwork.get(i).x4));
            }
            Random rand = new Random();
            int whatToDo = rand.nextInt(5);

            //random from 0 - 4,
            //0 = don't mutate,
            //1 = randomly choose a road's end point, find nearest end point elsewhere and make new road
            //2 = randomly choose city, find nearest city elsewhere without a direct connection and make new road
            //3 = find a road end point that is not a city, and shift one cell left/right/up or down (or don't mutate if no intersection exists)
            //4 = randomly remove a road
            if (whatToDo == 0)
            {

            }
            else if (whatToDo == 1)
            {
                int getEndPoint = rand.nextInt(roadNetwork.size());
                Point endPoint1 = new Point (roadNetwork.get(getEndPoint).x1,
                        roadNetwork.get(getEndPoint).x2);
                Point endPoint2 = null;
                for (int i = 0; i < roadNetwork.size(); i++)
                {
                    if (endPoint2 == null)
                    {
                        if (endPoint1.distance(roadNetwork.get(i).x1, roadNetwork.get(i).x2) <=
                                endPoint1.distance(roadNetwork.get(i).x3, roadNetwork.get(i).x4)
                                && !roadNetwork.contains(new Road(endPoint1.x, endPoint1.y, roadNetwork.get(i).x1, roadNetwork.get(i).x2)))
                        {
                            endPoint2 = new Point(roadNetwork.get(i).x1, roadNetwork.get(i).x2);
                        }
                        else if (endPoint1.distance(roadNetwork.get(i).x3, roadNetwork.get(i).x4) <=
                                endPoint1.distance(roadNetwork.get(i).x1, roadNetwork.get(i).x2)
                                && !roadNetwork.contains(new Road(endPoint1.x, endPoint1.y, roadNetwork.get(i).x3, roadNetwork.get(i).x4)))
                        {
                            endPoint2 = new Point(roadNetwork.get(i).x3, roadNetwork.get(i).x4);
                        }
                    }
                    else
                    {
                        if (endPoint1.distance(roadNetwork.get(i).x1, roadNetwork.get(i).x2) <=
                                endPoint1.distance(endPoint2)
                                && !roadNetwork.contains(new Road(endPoint1.x, endPoint1.y, roadNetwork.get(i).x1, roadNetwork.get(i).x2)))
                        {
                            endPoint2 = new Point(roadNetwork.get(i).x1, roadNetwork.get(i).x2);
                        }
                        else if (endPoint1.distance(roadNetwork.get(i).x3, roadNetwork.get(i).x4) <=
                                endPoint1.distance(endPoint2)
                                && !roadNetwork.contains(new Road(endPoint1.x, endPoint1.y, roadNetwork.get(i).x3, roadNetwork.get(i).x4)))
                        {
                            endPoint2 = new Point(roadNetwork.get(i).x3, roadNetwork.get(i).x4);
                        }
                    }
                }
                Road addRoad = new Road(endPoint1.x, endPoint1.y, endPoint2.x, endPoint2.y);
                newSolution.roadNetwork.add(addRoad);
            }
            else if (whatToDo == 2)
            {
                Point getCity1 = cities.get(rand.nextInt(cities.size()));
                Point getCity2 = null;
                for (int i = 0; i < cities.size(); i++)
                {
                    if (getCity2 == null)
                    {
                        if (!roadNetwork.contains(new Road(getCity1.x, getCity1.y,
                                cities.get(i).x, cities.get(i).y)))
                        {
                            getCity2 = cities.get(i);
                        }
                    }
                    else
                    {
                        if (getCity1.distance(cities.get(i).x, cities.get(i).y) <=
                                getCity1.distance(getCity2)
                                && !roadNetwork.contains(new Road(getCity1.x, getCity1.y, cities.get(i).x, cities.get(i).y)))
                        {
                            getCity2 = new Point(cities.get(i).x, cities.get(i).y);
                        }
                    }
                }
                Road addRoad = new Road(getCity1.x, getCity1.y, getCity2.x, getCity2.y);
                newSolution.roadNetwork.add(addRoad);
            }
            else if (whatToDo == 3)
            {
                //roadNetwork.
                //newSolution.roadNetwork.remove(rand.nextInt(roadNetwork.size()));
                ArrayList<Point> listOfIntersections = new ArrayList<Point>();
                for (int i = 0; i < roadNetwork.size(); i++)
                {
                    if (!cities.contains(new Point(roadNetwork.get(i).x1, roadNetwork.get(i).x2)))
                    {
                        if (!listOfIntersections.contains(new Point(roadNetwork.get(i).x1, roadNetwork.get(i).x2)))
                        {
                            listOfIntersections.add(new Point(roadNetwork.get(i).x1, roadNetwork.get(i).x2));
                        }
                    }
                    if (!cities.contains(new Point(roadNetwork.get(i).x3, roadNetwork.get(i).x4))) {
                        if (!listOfIntersections.contains(new Point(roadNetwork.get(i).x3, roadNetwork.get(i).x4))) {
                            listOfIntersections.add(new Point(roadNetwork.get(i).x3, roadNetwork.get(i).x4));
                        }
                    }
                }
                if (listOfIntersections.size() > 0) {
                    Point originalPoint = listOfIntersections.get(rand.nextInt(listOfIntersections.size()));
                    Point newPoint = null;
                    int changeTo = rand.nextInt(4);
                    if (changeTo == 0 && originalPoint.x + 1 < width)
                        newPoint = new Point(originalPoint.x + 1, originalPoint.y);
                    else if (changeTo == 1 && originalPoint.x - 1 >= 0)
                        newPoint = new Point(originalPoint.x - 1, originalPoint.y);
                    else if (changeTo == 2 && originalPoint.y + 1 < height)
                        newPoint = new Point(originalPoint.x, originalPoint.y + 1);
                    else if (changeTo == 2 && originalPoint.y - 1 >= 0)
                        newPoint = new Point(originalPoint.x, originalPoint.y - 1);
                    if (newPoint != null) {
                        for (int i = 0; i < newSolution.roadNetwork.size(); i++) {
                            if (newSolution.roadNetwork.get(i).x1 == originalPoint.x
                                    && newSolution.roadNetwork.get(i).x2 == originalPoint.y) {
                                newSolution.roadNetwork.get(i).x1 = newPoint.x;
                                newSolution.roadNetwork.get(i).x2 = newPoint.y;
                            }
                            if (newSolution.roadNetwork.get(i).x3 == originalPoint.x
                                    && newSolution.roadNetwork.get(i).x4 == originalPoint.y) {
                                newSolution.roadNetwork.get(i).x3 = newPoint.x;
                                newSolution.roadNetwork.get(i).x4 = newPoint.y;
                            }
                        }
                    }
                }

            }
            else if (whatToDo == 4) {
                newSolution.roadNetwork.remove(rand.nextInt(roadNetwork.size()));
            }
            newSolution.calculateScore(cities);
            if (newSolution.score < score){
                roadNetwork = newSolution.roadNetwork;
            }
        }

        public void calculateScore(List<Point> locations) {
            //calculate score based on locations (cities) provided:
            //score = (total distance of roads) + (average distance from any location a to any location b)
            //if not a complete graph (no existing connection from a to b), cost == infinity (or else very very high)
            //lower score is better
            //question: should we normalize each criteria afterwards, so we can compare them and apply weights later?

            float totalDistance = 0f;
            float avgDistance = 0f;

            //---parameter/criteria 1: total distance of all roads---
            for (int i = 0; i < roadNetwork.size(); i++) {
                Point p = new Point(roadNetwork.get(i).x1, roadNetwork.get(i).x2);
                totalDistance += p.distance(roadNetwork.get(i).x3, roadNetwork.get(i).x4);
            }

            //---parameter/criteria 2: average distance between any two points---
            float totalAvgDistance = 0;
            float totalAvgNumber = 0;
            for (int i = 0; i < locations.size(); i++) {
                //list of all roads connected to i
                List<Point> distanceList = new ArrayList<Point>();
                List<Float> distanceFloatList = new ArrayList<Float>();
                List<Point> traveledPoints = new ArrayList<Point>();
                List<Float> traveledFloatPoints = new ArrayList<Float>();

                distanceList.add(new Point(locations.get(i).x, locations.get(i).y));
                distanceFloatList.add(0f);
                traveledPoints.add(new Point(locations.get(i).x, locations.get(i).y));
                traveledFloatPoints.add(0f);

                while (!distanceList.isEmpty()) {
                    //take out first element of list, add all roads connected to that element, keep track of distance
                    //keep track of all points previously explored
                    //if find an element with endpoint j, return distance (must explore ALL possibilities for shortest distance)
                    //if list of all roads == null, no connection found
                    Point currentNode = distanceList.remove(0);
                    Float currentFloatNode = distanceFloatList.remove(0);
                    for (int k = 0; k < roadNetwork.size(); k++) {
                        if (currentNode.x == roadNetwork.get(k).x1 && currentNode.y == roadNetwork.get(k).x2) {
                            //road connects to this point,
                            //if second point exists in traveledPoints, check before adding
                            if (traveledPoints.contains(new Point(roadNetwork.get(k).x3, roadNetwork.get(k).x4))) {
                                int oldIndex = traveledPoints.indexOf(new Point(roadNetwork.get(k).x3, roadNetwork.get(k).x4));
                                Point newNode = new Point(roadNetwork.get(k).x3, roadNetwork.get(k).x4);
                                float newDistance = currentFloatNode
                                        + (float) newNode.distance(roadNetwork.get(k).x1, roadNetwork.get(k).x2);
                                if (newDistance < traveledFloatPoints.get(oldIndex)) {
                                    traveledFloatPoints.set(oldIndex, newDistance);
                                    distanceList.add(newNode);
                                    distanceFloatList.add(newDistance);
                                }
                            } else {
                                Point newNode = new Point(roadNetwork.get(k).x3, roadNetwork.get(k).x4);
                                float newDistance = currentFloatNode
                                        + (float) newNode.distance(roadNetwork.get(k).x1, roadNetwork.get(k).x2);
                                distanceList.add(newNode);
                                distanceFloatList.add(newDistance);
                                traveledPoints.add(newNode);
                                traveledFloatPoints.add(newDistance);
                            }
                        } else if (currentNode.x == roadNetwork.get(k).x3 && currentNode.y == roadNetwork.get(k).x4) {
                            //road connects to this point,
                            //if second point exists in traveledPoints, check before adding
                            if (traveledPoints.contains(new Point(roadNetwork.get(k).x1, roadNetwork.get(k).x2))) {
                                int oldIndex = traveledPoints.indexOf(new Point(roadNetwork.get(k).x1, roadNetwork.get(k).x2));
                                Point newNode = new Point(roadNetwork.get(k).x1, roadNetwork.get(k).x2);
                                float newDistance = currentFloatNode
                                        + (float) newNode.distance(roadNetwork.get(k).x3, roadNetwork.get(k).x4);
                                if (newDistance < traveledFloatPoints.get(oldIndex)) {
                                    traveledFloatPoints.set(oldIndex, newDistance);
                                    distanceList.add(newNode);
                                    distanceFloatList.add(newDistance);
                                }
                            } else {
                                Point newNode = new Point(roadNetwork.get(k).x1, roadNetwork.get(k).x2);
                                float newDistance = currentFloatNode
                                        + (float) newNode.distance(roadNetwork.get(k).x3, roadNetwork.get(k).x4);
                                distanceList.add(newNode);
                                distanceFloatList.add(newDistance);
                                traveledPoints.add(newNode);
                                traveledFloatPoints.add(newDistance);
                            }
                        }
                    }
                }
                for (int j = 0; j < locations.size(); j++) {
                    if (i != j) {
                        if (traveledPoints.indexOf(locations.get(j)) == -1)
                        {
                            //case where graph is not complete
                            totalAvgDistance += Math.pow((double) width * height, 2);
                        }
                        else {
                            totalAvgDistance += traveledFloatPoints.get(traveledPoints.indexOf(locations.get(j)));
                        }
                        totalAvgNumber++;
                    }

                }
            }
            avgDistance = totalAvgDistance / totalAvgNumber;
            score01 = totalDistance;
            score02 = avgDistance;

            score = totalDistance + avgDistance;
        }
    }

    //list of RoadNetworkSolutions: a population (to be updated with each cycle of GA)
    List<RoadNetworkSolution> roadNetworkPopulation = new ArrayList<RoadNetworkSolution>();

    //initializeGA: function that initializes population (number of random roadSequences) based on given size)
    public void initializeGA(int numOfPopulation) {
        for (int j = 0; j < numOfPopulation; j++) {

            RoadNetworkSolution newNetwork = new RoadNetworkSolution();

            //generate roads randomly (straight lines per city, do not connect to city already with connection)
            Random rand = new Random();
            List<Point> newCityList = new ArrayList<Point>();
            for (int i = 0; i < cities.size(); i++) {
                newCityList.add(cities.get(i));
            }
            Point currentCity = cities.get(rand.nextInt(cities.size()));
            newCityList.remove(currentCity);

            while (newCityList.size() > 0) {
                Point newCity = newCityList.get(rand.nextInt(newCityList.size()));
                Road newRoad = new Road(currentCity.x, currentCity.y, newCity.x, newCity.y);
                newNetwork.roadNetwork.add(newRoad);
                currentCity = newCity;
                newCityList.remove(newCity);
            }

            newNetwork.calculateScore(cities);

            roadNetworkPopulation.add(newNetwork);
        }
        Collections.sort(roadNetworkPopulation);
        for (int j = 0; j < numOfPopulation; j++) {
            System.out.println("Road Network " + j + ": " + roadNetworkPopulation.get(j).score01
                    + " + " + roadNetworkPopulation.get(j).score02 + " = "
                    + roadNetworkPopulation.get(j).score);
        }

    }

    public Pair[] getIntersections()
    {
        if (roadNetworkPopulation.size() == 0) {
            Pair[] p = new Pair[0];
            return p;
        }
        Pair [] p = new Pair[roadNetworkPopulation.get(0).myIntersections.size()];
        p = roadNetworkPopulation.get(0).myIntersections.toArray(p);

        return p;
    }


    //function getRoads: returns the best current road network (to display in ViewerController.java)
    public Road[] getRoads() {
        if (roadNetworkPopulation.size() == 0) {
            Road[] p = new Road[0];
            return p;
        } else {
            int bestIndex = 0;
            for (int i = 0; i < roadNetworkPopulation.size(); i++) {
                if (roadNetworkPopulation.get(bestIndex).score > roadNetworkPopulation.get(i).score) {
                    bestIndex = i;
                }
            }

            Road[] p = new Road[roadNetworkPopulation.get(bestIndex).roadNetwork.size()];
            p = roadNetworkPopulation.get(bestIndex).roadNetwork.toArray(p);
            return p;
        }
    }

    public List<RoadNetworkSolution> getRoadNetworkPopulation() {
        return roadNetworkPopulation;
    }

    public void setRoadNetworkPopulation(List<RoadNetworkSolution> newNetworkPopulation)
    {
        roadNetworkPopulation = newNetworkPopulation;
    }

    public void updateGA()
    {

        Collections.sort(roadNetworkPopulation);
        //roadNetworkPopulation.get(0).updateIntersections();

        ArrayList<RoadNetworkSolution> newGeneration
                = new ArrayList<RoadNetworkSolution>();

        for (int j = 0; j < 5; j++)
        {
            roadNetworkPopulation.get(j).updateIntersections();
            roadNetworkPopulation.get(j).removeDeadEnds();
            roadNetworkPopulation.get(j).calculateScore(cities);

            newGeneration.add(roadNetworkPopulation.get(j));
        }
        Random rand = new Random();
        for (int j = 5; j < roadNetworkPopulation.size(); j++) {
            ArrayList<RoadNetworkSolution> parents
                    = new ArrayList<RoadNetworkSolution>();
            int r1, r2, r3;
            r1 = rand.nextInt(roadNetworkPopulation.size());
            r2 = rand.nextInt(roadNetworkPopulation.size());
            r3 = rand.nextInt(roadNetworkPopulation.size());

            parents.add(roadNetworkPopulation.get(r1));
            parents.add(roadNetworkPopulation.get(r2));
            parents.add(roadNetworkPopulation.get(r3));
            Collections.sort(parents);

            //now only use parents at 0 and 1 to make child
            double totalProb = (parents.get(0).score + parents.get(1).score);
            double prob01 = totalProb - parents.get(0).score;
            double prob02 = totalProb - parents.get(1).score;
            RoadNetworkSolution newNetwork = new RoadNetworkSolution();
            for (int k = 0; k < Math.max(parents.get(0).roadNetwork.size(), parents.get(1).roadNetwork.size()); k++)
            {
                //random number between 0 and 1, scaled to fit our probability
                double decidingNumber = rand.nextFloat()*totalProb;
                if (decidingNumber < prob01)
                {
                    //add a random road from solution 01
                    newNetwork.roadNetwork.add(parents.get(0).roadNetwork.get(rand.nextInt(parents.get(0).roadNetwork.size())));
                }
                else
                {
                    //add a random road from solution 02
                    newNetwork.roadNetwork.add(parents.get(1).roadNetwork.get(rand.nextInt(parents.get(1).roadNetwork.size())));
                }
            }
            newNetwork.updateIntersections();
            newNetwork.removeDeadEnds();
            int dup = newNetwork.removeDuplicates();
            //remove duplicate roads, but replace them with a new road
            while (dup != 0)
            {
                for (int i = 0; i < dup; i++)
                {
                    Point city1, city2;
                    int city1Index = rand.nextInt(cities.size());
                    int city2Index = rand.nextInt(cities.size());
                    if (city1Index == city2Index)
                    {
                        city2Index++;
                        if (city2Index >= cities.size())
                            city2Index = 0;
                    }
                    city1 = cities.get(city1Index);
                    city2 = cities.get(city2Index);

                    Road newRoad = new Road(city1.x, city1.y, city2.x, city2.y);
                    newNetwork.roadNetwork.add(newRoad);
                }

                dup = newNetwork.removeDuplicates();

            }

            //NOW... check for intersections and modify them
            //also check for dead end roads

            newNetwork.calculateScore(cities);
            newGeneration.add(newNetwork);
        }
        roadNetworkPopulation.clear();
        for (int i = 0; i < newGeneration.size(); i++)
        {
            roadNetworkPopulation.add(newGeneration.get(i));
        }

        Collections.sort(roadNetworkPopulation);

        //before printing out again, add mutation
        for (int j = 0; j < roadNetworkPopulation.size(); j++)
        {
            roadNetworkPopulation.get(j).addMutation();
            roadNetworkPopulation.get(j).calculateScore(cities);
        }

        Collections.sort(roadNetworkPopulation);

        for (int j = 0; j < roadNetworkPopulation.size(); j++) {

            System.out.println("Road Network " + j + ": "
                    + roadNetworkPopulation.get(j).score01 + " + "
                    + roadNetworkPopulation.get(j).score02 + " = "
                    + roadNetworkPopulation.get(j).score);
        }

    }

}