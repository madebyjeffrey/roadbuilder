
package ca.drakej;

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

    public boolean hasCity(Point city) {
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


    //list of RoadNetworkSolutions: a population (to be updated with each cycle of GA)
    List<RoadNetworkSolution> roadNetworkPopulation = new ArrayList<RoadNetworkSolution>();


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