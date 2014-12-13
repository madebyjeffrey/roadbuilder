package ca.drakej;

import javafx.util.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by Andrew on 12/12/2014.
 */
//class RoadNetworkSolution: a class that stores a list of roads, plus score calculation for comparison (an "agent" or "gene sequence" in population)
public class RoadNetworkSolution implements Comparable<RoadNetworkSolution> {
    //(references Map to get width, height, cities, etc.)
    public Map thisMap;

    //list of roads in this solution
    public List<Road> roadNetwork = new ArrayList<Road>();

    //score01 = total length/amount of "road" on map
    public float score01 = 0f;
    //score02 = average distance between any two cities (really high if not a complete graph)
    public float score02 = 0f;
    //score = sum of above scores, used for comparison
    public float score = 0f;

    Random rand = new Random();

    public RoadNetworkSolution(Map thisMap)
    {
        this.thisMap = thisMap;
    }

    //function compareTo: used to easily sort solutions based on score
    public int compareTo(RoadNetworkSolution r2)
    {
        if (this.score < r2.score)
            return -1;
        else if (this.score > r2.score)
            return 1;
        else
            return 0;
    }

    //function removeDuplicates: removes duplicate roads (puts roads into a hashset, then back into arraylist)
    public int removeDuplicates() {
        int sizeOfList = roadNetwork.size();
        HashSet hs = new HashSet();
        hs.addAll(roadNetwork);
        roadNetwork.clear();
        roadNetwork.addAll(hs);
        return sizeOfList - roadNetwork.size();
    }

    //function removeDeadEnds: when generating new RoadNetworkSolutions, it's possible one leftover
    //road will connect to an intersection point where other roads no longer connect. If road leads
    //nowhere, remove it.
    //COULD BE IMPROVED: when finding shortest paths for all cities, you could remove all roads not
    //used in any path.
    public void removeDeadEnds()
    {
        ArrayList<Integer> indexToRemove = new ArrayList<Integer>();
        for (int i = 0; i < roadNetwork.size(); i++) {
            //does point on this road not connect to any other road or city?
            boolean isDeadEnd = pointConnectsToRoadOrCity(roadNetwork.get(i).p1, i) || pointConnectsToRoadOrCity(roadNetwork.get(i).p2, i);
            if (isDeadEnd)
                indexToRemove.add(i);
        }
        for (int i = 0; i < indexToRemove.size(); i++)
            roadNetwork.remove(indexToRemove.get(i));
        //WARNING: other dead ends may exist after this, but future "GA-generations" would solve this in a few cycles anyway
    }

    //function pointConnectsToRoadOrCity: returns true or false to say if point is shared with cities or other roads
    public boolean pointConnectsToRoadOrCity(Point p, int originalRoad)
    {
        boolean isDeadEnd = true;
        for (int j = 0; j < roadNetwork.size(); j++)
        {
            if (originalRoad != j){
                Point p2 = new Point(roadNetwork.get(j).p1);
                if (p.equals(p2))
                    isDeadEnd = false;
                p2 = new Point(roadNetwork.get(j).p2);
                if (p.equals(p2))
                    isDeadEnd = false;
            }
        }
        for (int j = 0; j < thisMap.getCities().size(); j++)
        {
            if (p.equals(thisMap.getCities().get(j)))
                isDeadEnd = false;
        }
        return isDeadEnd;
    }

    //function updateIntersections: checks for overlapping roads, automatically makes intersection at that spot to allow them to branch paths
    //(intersection is placed at integer values on grid, updates one intersection at a time until no unresolved intersections remain)
    //DO NOT TRY TO UNDERSTAND: math taken from http://en.wikipedia.org/wiki/Line%E2%80%93line_intersection , "Given two points on each line"
    public void updateIntersections()
    {
        Road r1 = null, r2 = null;
        float interX = -1, interY = -1;

        do {
            interX = -1;
            interY = -1;
            r1 = null;
            r2 = null;
            for (int i = 0; i < roadNetwork.size(); i++) {
                float x1 = roadNetwork.get(i).p1.x;
                float y1 = roadNetwork.get(i).p1.y;
                float x2 = roadNetwork.get(i).p2.x;
                float y2 = roadNetwork.get(i).p2.y;
                for (int j = 0; j < roadNetwork.size(); j++) {
                    if (i != j) {
                        //does this road intersect with current road?
                        float x3 = roadNetwork.get(j).p1.x;
                        float y3 = roadNetwork.get(j).p1.y;
                        float x4 = roadNetwork.get(j).p2.x;
                        float y4 = roadNetwork.get(j).p2.y;
                        if ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4) == 0)
                            continue;
                        float intersectx = (((x1*y2 - y1*x2)*(x3-x4))-((x1-x2)*(x3*y4-y3*x4)))
                                / (((x1-x2)*(y3-y4))-((y1-y2)*(x3-x4)));
                        float intersecty = (((x1*y2 - y1*x2)*(y3-y4))-((y1-y2)*(x3*y4-y3*x4)))
                                / (((x1-x2)*(y3-y4))-((y1-y2)*(x3-x4)));
                        if (((intersectx > x1 && intersectx < x2) || (intersectx < x1 && intersectx > x2))
                                && ((intersectx > x3 && intersectx < x4) || (intersectx < x3 && intersectx > x4))
                                && ((intersecty > y1 && intersecty < y2) || (intersecty < y1 && intersecty > y2))
                                && ((intersecty > y3 && intersecty < y4) || (intersecty < y3 && intersecty > y4))
                                && (!roadNetwork.get(i).contains(new Point((int)intersectx,(int)intersecty))
                                || !roadNetwork.get(j).contains(new Point((int)intersectx,(int)intersecty))))
                        {
                            //THERE IS INTERSECTION! SPLIT THE ROAD! (later)
                            interX = intersectx;
                            interY = intersecty;
                            r1 = roadNetwork.get(i);
                            r2 = roadNetwork.get(j);
                            break;
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
                Road newRoad1 = new Road(r1.p1, new Point((int)interX, (int)interY));
                Road newRoad2 = new Road(r1.p2, new Point((int)interX, (int)interY));
                Road newRoad3 = new Road(r2.p1, new Point((int)interX, (int)interY));
                Road newRoad4 = new Road(r2.p2, new Point((int)interX, (int)interY));
                roadNetwork.add(newRoad1);
                roadNetwork.add(newRoad2);
                roadNetwork.add(newRoad3);
                roadNetwork.add(newRoad4);
            }
        }while(interX != -1);
    }

    //function addMutation: adds random mutation, ONLY KEEPS MUTATION IF IT MADE IMPROVEMENT
    public void addMutation()
    {
        RoadNetworkSolution newSolution = new RoadNetworkSolution(thisMap);
        newSolution.thisMap = thisMap;
        for (int i = 0; i < roadNetwork.size(); i++)
        {
            newSolution.roadNetwork.add(new Road(roadNetwork.get(i).p1, roadNetwork.get(i).p2));
        }

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
            newSolution = mutation_randomNewRoad(newSolution);
        }
        else if (whatToDo == 2)
        {
            newSolution = mutation_randomCityConnection(newSolution);
        }
        else if (whatToDo == 3)
        {
            newSolution = mutation_randomIntersectionMovement(newSolution);
        }
        else if (whatToDo == 4) {
            newSolution = mutation_randomRemoveRoad(newSolution);
        }

        newSolution.calculateScore(thisMap.getCities());
        if (newSolution.score < score){
            roadNetwork = newSolution.roadNetwork;
        }
    }

    //1 = randomly choose a road's end point, find nearest end point elsewhere and make new road
    public RoadNetworkSolution mutation_randomNewRoad(RoadNetworkSolution originalSolution)
    {
        int getEndPoint = rand.nextInt(roadNetwork.size());
        Point endPoint1 = new Point (roadNetwork.get(getEndPoint).p1);
        Point endPoint2 = null;
        for (int i = 0; i < roadNetwork.size(); i++)
        {
            if (endPoint2 == null)
            {
                if (endPoint1.distance(roadNetwork.get(i).p1) <= endPoint1.distance(roadNetwork.get(i).p2)
                        && !roadNetwork.contains(new Road(endPoint1, roadNetwork.get(i).p1)))
                {
                    endPoint2 = roadNetwork.get(i).p1;
                }
                else if (endPoint1.distance(roadNetwork.get(i).p2) <= endPoint1.distance(roadNetwork.get(i).p1)
                        && !roadNetwork.contains(new Road(endPoint1, roadNetwork.get(i).p2)))
                {
                    endPoint2 = roadNetwork.get(i).p2;
                }
            }
            else
            {
                if (endPoint1.distance(roadNetwork.get(i).p1) <= endPoint1.distance(endPoint2)
                        && !roadNetwork.contains(new Road(endPoint1, roadNetwork.get(i).p1)))
                {
                    endPoint2 = roadNetwork.get(i).p1;
                }
                else if (endPoint1.distance(roadNetwork.get(i).p2) <= endPoint1.distance(endPoint2)
                        && !roadNetwork.contains(new Road(new Point(endPoint1.x, endPoint1.y), roadNetwork.get(i).p2)))
                {
                    endPoint2 = roadNetwork.get(i).p2;
                }
            }
        }
        Road addRoad = new Road(endPoint1, endPoint2);
        originalSolution.roadNetwork.add(addRoad);
        return originalSolution;
    }

    //2 = randomly choose city, find nearest city elsewhere without a direct connection and make new road
    public RoadNetworkSolution mutation_randomCityConnection(RoadNetworkSolution originalSolution)
    {
        Point getCity1 = thisMap.getCities().get(rand.nextInt(thisMap.getCities().size()));
        Point getCity2 = null;
        for (int i = 0; i < thisMap.getCities().size(); i++)
        {
            if (getCity2 == null)
            {
                if (!roadNetwork.contains(new Road(getCity1, thisMap.getCities().get(i))))
                {
                    getCity2 = thisMap.getCities().get(i);
                }
            }
            else
            {
                if (getCity1.distance(thisMap.getCities().get(i)) <= getCity1.distance(getCity2)
                        && !roadNetwork.contains(new Road(getCity1, thisMap.getCities().get(i))))
                {
                    getCity2 = thisMap.getCities().get(i);
                }
            }
        }
        Road addRoad = new Road(getCity1, getCity2);
        originalSolution.roadNetwork.add(addRoad);
        return originalSolution;
    }

    //3 = find a road end point that is not a city, and shift one cell left/right/up or down (or don't mutate if no intersection exists)
    public RoadNetworkSolution mutation_randomIntersectionMovement(RoadNetworkSolution originalSolution)
    {
        ArrayList<Point> listOfIntersections = new ArrayList<Point>();
        for (int i = 0; i < roadNetwork.size(); i++)
        {
            if (!thisMap.getCities().contains(roadNetwork.get(i)) && !listOfIntersections.contains(roadNetwork.get(i))) {
                listOfIntersections.add(roadNetwork.get(i).p1);
            }
            if (!thisMap.getCities().contains(roadNetwork.get(i).p2) && !listOfIntersections.contains(roadNetwork.get(i).p2)) {
                listOfIntersections.add(roadNetwork.get(i).p2);
            }
        }
        if (listOfIntersections.size() > 0) {
            Point originalPoint = listOfIntersections.get(rand.nextInt(listOfIntersections.size()));
            Point newPoint = null;
            int changeTo = rand.nextInt(4);
            if (changeTo == 0 && originalPoint.x + 1 < thisMap.getWidth())
                newPoint = new Point(originalPoint.x + 1, originalPoint.y);
            else if (changeTo == 1 && originalPoint.x - 1 >= 0)
                newPoint = new Point(originalPoint.x - 1, originalPoint.y);
            else if (changeTo == 2 && originalPoint.y + 1 < thisMap.getHeight())
                newPoint = new Point(originalPoint.x, originalPoint.y + 1);
            else if (changeTo == 2 && originalPoint.y - 1 >= 0)
                newPoint = new Point(originalPoint.x, originalPoint.y - 1);
            if (newPoint != null) {
                for (int i = 0; i < originalSolution.roadNetwork.size(); i++) {
                    if (originalSolution.roadNetwork.get(i).p1.equals(originalPoint)) {
                        originalSolution.roadNetwork.get(i).p1 = newPoint;
                    }
                    if (originalSolution.roadNetwork.get(i).p2.equals(originalPoint)) {
                        originalSolution.roadNetwork.get(i).p2 = newPoint;
                    }
                }
            }
        }
        return originalSolution;
    }

    //4 = randomly remove a road
    public RoadNetworkSolution mutation_randomRemoveRoad(RoadNetworkSolution originalSolution)
    {
        originalSolution.roadNetwork.remove(rand.nextInt(roadNetwork.size()));
        return originalSolution;
    }


    //function calculateScore: calculates float value based on criteria, score used to compare against other solutions
    public void calculateScore(List<Point> locations) {
        //calculate score based on locations (cities) provided:
        //score = (total distance of roads) + (average distance from any location a to any location b)
        //if not a complete graph (no existing connection from a to b), cost == infinity (or else very very high)
        //lower score is better
        //question: should we normalize each criteria afterwards, so we can compare them and apply weights later?

        //---parameter/criteria 1: total distance of all roads---
        score01 = calculateTotalRoadLength(locations);

        //---parameter/criteria 2: average distance between any two points---
        score02 = calculateAverageDistanceBetweenTwoPoints(locations);

        score = score01 + score02;
    }

    //function calculateTotalRoadLength: sum of all lengths of all roads
    public float calculateTotalRoadLength(List<Point> locations)
    {
        float totalLength = 0;
        for (int i = 0; i < roadNetwork.size(); i++) {
            Point p = roadNetwork.get(i).p1;
            totalLength += p.distance(roadNetwork.get(i).p2);
        }
        return totalLength;
    }

    //function calculateAverageDistanceBetweenTwoPoints: average distance between all possible pairs of cities
    //(I didn't want to refactor this further due to complexity and time. Don't bother understanding it,
    // just know it finds the shortest distance between all pairs of cities and averages them)
    public float calculateAverageDistanceBetweenTwoPoints(List<Point> locations)
    {
        float avgDistanceAtoB = 0;
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

                    if (currentNode.equals(roadNetwork.get(k).p1)) {
                        //road connects to this point,
                        //if second point exists in traveledPoints, check before adding
                        if (traveledPoints.contains(roadNetwork.get(k).p2)) {
                            int oldIndex = traveledPoints.indexOf(roadNetwork.get(k).p2);
                            Point newNode = (roadNetwork.get(k).p2);
                            float newDistance = currentFloatNode+(float)newNode.distance(roadNetwork.get(k).p1);
                            if (newDistance < traveledFloatPoints.get(oldIndex)) {
                                traveledFloatPoints.set(oldIndex, newDistance);
                                distanceList.add(newNode);
                                distanceFloatList.add(newDistance);
                            }
                        } else {
                            Point newNode = new Point(roadNetwork.get(k).p2);
                            float newDistance = currentFloatNode + (float) newNode.distance(roadNetwork.get(k).p1);
                            distanceList.add(newNode);
                            distanceFloatList.add(newDistance);
                            traveledPoints.add(newNode);
                            traveledFloatPoints.add(newDistance);
                        }
                    } else if (currentNode.equals(roadNetwork.get(k).p2)) {
                        //road connects to this point,
                        //if second point exists in traveledPoints, check before adding
                        if (traveledPoints.contains(roadNetwork.get(k).p1)) {
                            int oldIndex = traveledPoints.indexOf(roadNetwork.get(k).p1);
                            Point newNode = roadNetwork.get(k).p1;
                            float newDistance = currentFloatNode + (float) newNode.distance(roadNetwork.get(k).p2);
                            if (newDistance < traveledFloatPoints.get(oldIndex)) {
                                traveledFloatPoints.set(oldIndex, newDistance);
                                distanceList.add(newNode);
                                distanceFloatList.add(newDistance);
                            }
                        } else {
                            Point newNode = new Point(roadNetwork.get(k).p1);
                            float newDistance = currentFloatNode + (float) newNode.distance(roadNetwork.get(k).p2);
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
                        //case where graph is not complete, add large value
                        totalAvgDistance += Math.pow((double) thisMap.getWidth() * thisMap.getHeight(), 2);
                    }
                    else {
                        totalAvgDistance += traveledFloatPoints.get(traveledPoints.indexOf(locations.get(j)));
                    }
                    totalAvgNumber++;
                }

            }
        }
        avgDistanceAtoB = totalAvgDistance / totalAvgNumber;
        return avgDistanceAtoB;
    }
}