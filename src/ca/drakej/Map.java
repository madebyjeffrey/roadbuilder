
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
    }


    //class RoadNetworkSolution: a class that stores a list of roads, plus score calculation for comparison (an "agent" or "gene sequence" in population)
    public class RoadNetworkSolution {
        public List<Road> roadNetwork = new ArrayList<Road>();

        public float score01 = 0f;
        public float score02 = 0f;
        public float score = 0f;

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
                            totalAvgDistance += width*height;
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
            System.out.println("Road Network " + j + ": " + newNetwork.score01 + " + " + newNetwork.score02 + " = " + newNetwork.score);

            roadNetworkPopulation.add(newNetwork);
        }
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
        //TODO: EVERYTHING!
        for (int j = 0; j < roadNetworkPopulation.size(); j++) {

            System.out.println("Road Network " + j + ": "
                    + roadNetworkPopulation.get(j).score01 + " + "
                    + roadNetworkPopulation.get(j).score02 + " = "
                    + roadNetworkPopulation.get(j).score);
        }
    }

}