package ca.drakej;

import com.sun.tools.javac.util.ArrayUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by drakej on 14-12-07.
 */ //class RoadNetworkSolution: a class that stores a list of roads, plus score calculation for comparison (an "agent" or "gene sequence" in population)
public class RoadNetworkSolution {
    private Map map;
    public List<Road> roadNetwork = new ArrayList<>();

    public float score01 = 0f;
    public float score02 = 0f;
    public float score = 0f;

    public RoadNetworkSolution(Map map) {
        this.map = map;
    }

    public List<Point> connections(Point p) {
        return roadNetwork.stream().map(road -> {
            if (road.starts(p)) return road.getEnd();
            else if (road.ends(p)) return road.getStart();
            else return null;
        }).filter(x -> x != null).distinct().collect(Collectors.toCollection(ArrayList<Point>::new));
    }

    // this is a faulty method, because it returns how many were not connected on the graph of the first
    // unvisited point, not overall
    public double complete() {
        // unvisited only contains cities
        List<Point> unvisited = Arrays.stream(map.getCities()).collect(Collectors.toCollection(ArrayList<Point>::new));
        // visited will contain points
        List<Point> visited = new ArrayList<>();

        Queue<Point> frontier = new LinkedList<>();
        frontier.add(unvisited.remove(0));

        while (frontier.size() != 0) {
            // obtain an element from the frontier
            Point current = frontier.remove();
            visited.add(current);

            // connecting points, from current by single road that have not been visited yet
            List<Point> nextPoints = connections(current).stream()
                    .filter(e -> !visited.contains(e))
                    .filter(e -> !frontier.contains(e))
                    .collect(Collectors.toList());

            // remove cities from unvisited, and add them to the frontier
            nextPoints.forEach(e -> {
                if (unvisited.contains(e)) {
                    unvisited.remove(e);
                }
                frontier.add(e);
            });
        }

        return unvisited.size() / visited.size();
    }

    // does not duplicate road sections that would be used on multiple paths
    public double totalLength() {
        return roadNetwork.stream().map(Road::length).reduce((x,y)->x+y).get();
    }


    public double score() {
        return complete() * totalLength();
    }

//    public void calculateScore(List<Point> locations) {
//        //calculate score based on locations (cities) provided:
//        //score = (total distance of roads) + (average distance from any location a to any location b)
//        //if not a complete graph (no existing connection from a to b), cost == infinity (or else very very high)
//        //lower score is better
//        //question: should we normalize each criteria afterwards, so we can compare them and apply weights later?
//
//        float totalDistance = 0f;
//        float avgDistance = 0f;
//
//        //---parameter/criteria 1: total distance of all roads---
//        for (int i = 0; i < roadNetwork.size(); i++) {
//            Point p = new Point(roadNetwork.get(i).x1, roadNetwork.get(i).x2);
//            totalDistance += p.distance(roadNetwork.get(i).x3, roadNetwork.get(i).x4);
//        }
//
//        //---parameter/criteria 2: average distance between any two points---
//        float totalAvgDistance = 0;
//        float totalAvgNumber = 0;
//        for (int i = 0; i < locations.size(); i++) {
//            //list of all roads connected to i
//            List<Point> distanceList = new ArrayList<>();
//            List<Float> distanceFloatList = new ArrayList<>();
//            List<Point> traveledPoints = new ArrayList<>();
//            List<Float> traveledFloatPoints = new ArrayList<>();
//
//            distanceList.add(new Point(locations.get(i).x, locations.get(i).y));
//            distanceFloatList.add(0f);
//            traveledPoints.add(new Point(locations.get(i).x, locations.get(i).y));
//            traveledFloatPoints.add(0f);
//
//            while (!distanceList.isEmpty()) {
//                //take out first element of list, add all roads connected to that element, keep track of distance
//                //keep track of all points previously explored
//                //if find an element with endpoint j, return distance (must explore ALL possibilities for shortest distance)
//                //if list of all roads == null, no connection found
//                Point currentNode = distanceList.remove(0);
//                Float currentFloatNode = distanceFloatList.remove(0);
//                for (int k = 0; k < roadNetwork.size(); k++) {
//                    if (currentNode.x == roadNetwork.get(k).x1 && currentNode.y == roadNetwork.get(k).x2) {
//                        //road connects to this point,
//                        //if second point exists in traveledPoints, check before adding
//                        if (traveledPoints.contains(new Point(roadNetwork.get(k).x3, roadNetwork.get(k).x4))) {
//                            int oldIndex = traveledPoints.indexOf(new Point(roadNetwork.get(k).x3, roadNetwork.get(k).x4));
//                            Point newNode = new Point(roadNetwork.get(k).x3, roadNetwork.get(k).x4);
//                            float newDistance = currentFloatNode
//                                    + (float) newNode.distance(roadNetwork.get(k).x1, roadNetwork.get(k).x2);
//                            if (newDistance < traveledFloatPoints.get(oldIndex)) {
//                                traveledFloatPoints.set(oldIndex, newDistance);
//                                distanceList.add(newNode);
//                                distanceFloatList.add(newDistance);
//                            }
//                        } else {
//                            Point newNode = new Point(roadNetwork.get(k).x3, roadNetwork.get(k).x4);
//                            float newDistance = currentFloatNode
//                                    + (float) newNode.distance(roadNetwork.get(k).x1, roadNetwork.get(k).x2);
//                            distanceList.add(newNode);
//                            distanceFloatList.add(newDistance);
//                            traveledPoints.add(newNode);
//                            traveledFloatPoints.add(newDistance);
//                        }
//                    } else if (currentNode.x == roadNetwork.get(k).x3 && currentNode.y == roadNetwork.get(k).x4) {
//                        //road connects to this point,
//                        //if second point exists in traveledPoints, check before adding
//                        if (traveledPoints.contains(new Point(roadNetwork.get(k).x1, roadNetwork.get(k).x2))) {
//                            int oldIndex = traveledPoints.indexOf(new Point(roadNetwork.get(k).x1, roadNetwork.get(k).x2));
//                            Point newNode = new Point(roadNetwork.get(k).x1, roadNetwork.get(k).x2);
//                            float newDistance = currentFloatNode
//                                    + (float) newNode.distance(roadNetwork.get(k).x3, roadNetwork.get(k).x4);
//                            if (newDistance < traveledFloatPoints.get(oldIndex)) {
//                                traveledFloatPoints.set(oldIndex, newDistance);
//                                distanceList.add(newNode);
//                                distanceFloatList.add(newDistance);
//                            }
//                        } else {
//                            Point newNode = new Point(roadNetwork.get(k).x1, roadNetwork.get(k).x2);
//                            float newDistance = currentFloatNode
//                                    + (float) newNode.distance(roadNetwork.get(k).x3, roadNetwork.get(k).x4);
//                            distanceList.add(newNode);
//                            distanceFloatList.add(newDistance);
//                            traveledPoints.add(newNode);
//                            traveledFloatPoints.add(newDistance);
//                        }
//                    }
//                }
//            }
//            for (int j = 0; j < locations.size(); j++) {
//                if (i != j) {
//                    if (traveledPoints.indexOf(locations.get(j)) == -1) {
//                        //case where graph is not complete
//                        totalAvgDistance += map.getWidth() * map.getHeight();
//                    } else {
//                        totalAvgDistance += traveledFloatPoints.get(traveledPoints.indexOf(locations.get(j)));
//                    }
//                    totalAvgNumber++;
//                }
//
//            }
//        }
//        avgDistance = totalAvgDistance / totalAvgNumber;
//        score01 = totalDistance;
//        score02 = avgDistance;
//
//        score = totalDistance + avgDistance;
//    }
}
