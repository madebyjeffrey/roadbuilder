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
    private int width;                                  //width of map
    private int height;                                 //height of map
    private List<Point> cities = new ArrayList<Point>();    //list of cities
    Random rand = new Random();

    //list of RoadNetworkSolutions: a population of "gene-strands" (to be updated with each cycle of genetic algorithm)
    List<RoadNetworkSolution> roadNetworkPopulation = new ArrayList<RoadNetworkSolution>();

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void clearMap()
    {
        roadNetworkPopulation.clear();
    }

    public void generateCities(int count) {
        for (int i = 0; i < count; ++i) {
            cities.add(generateCity());
        }

        //NEW: add clustering algorithm before GA is applied
        //clusterizeRoadNetwork();
    }

    public class PointGroup{
        public List<Point> cities = new ArrayList<Point>();
        public Point center = null;

        public PointGroup()
        {
        }

        public void add(Point c)
        {
            cities.add(c);

            float cx = 0, cy = 0;
            for (int i = 0; i < cities.size(); i++)
            {
                //find center of all cities
                cx += cities.get(i).x;
                cy += cities.get(i).y;
            }
            center = new Point ((int)(cx/cities.size()),(int)(cy/(float)cities.size()));
        }

        public Point returnNearestPoint(Point c)
        {
            Point c2 = null;
            for (int i = 0; i < cities.size(); i++)
            {
                if (c2 == null)
                    c2 = cities.get(i);
                else
                {
                    if (cities.get(i).distance(c) < c2.distance(c))
                        c2 = cities.get(i);
                }
            }
            return c2;
        }

    }

    public void clusterizeRoadNetwork()
    {
        //for all individual cities in a set,
        //group two nearest with road, combine as group, add group back to set
        //repeat until set.size == 1
        RoadNetworkSolution newRoadNetwork = new RoadNetworkSolution(this);

        List<PointGroup> tempCities = new ArrayList<PointGroup>();
        for (int i = 0; i < cities.size(); i++) {
            PointGroup p = new PointGroup();
            p.add(cities.get(i));
            tempCities.add(p);
        }

        while(tempCities.size() > 1)
        {
            //find two nearest groups
            float distance = Math.max(height,width);
            PointGroup group01 = null;
            PointGroup group02 = null;
            for (int i = 0; i < tempCities.size(); i++) {
                if (group01 == null)
                    group01 = tempCities.get(i);
                for (int j = i; j < tempCities.size(); j++)
                {
                    if (i != j) {
                        if (group02 == null) {
                            group02 = tempCities.get(j);
                            distance = (float) group01.center.distance(group02.center);
                        }
                        else {
                            if (distance > tempCities.get(i).center.distance(tempCities.get(j).center))
                            {
                                group01 = tempCities.get(i);
                                group02 = tempCities.get(j);
                                distance = (float) group01.center.distance(group02.center);
                            }
                        }
                    }
                }
            }

            Road newRoad = new Road(group01.returnNearestPoint(group02.center),
                    group02.returnNearestPoint(group01.center));
            tempCities.remove(group01);
            tempCities.remove(group02);
            for (int i = 0; i < group02.cities.size(); i++)
            {
                group01.add(group02.cities.get(i));
            }
            tempCities.add(group01);
            newRoadNetwork.roadNetwork.add(newRoad);
        }

        roadNetworkPopulation.add(0, newRoadNetwork);
        roadNetworkPopulation.get(0).calculateScore(cities);
        System.out.println("The following is the score from the 'clustering' algorithm:");
        printRoadNetworkPopulation();
        System.out.println("next will be the GA");
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
        int citiesLength = Integer.parseInt(meta[0]);

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

    public List<Point> getCities() {
        ArrayList<Point> newList = new ArrayList<Point>();
        for (int i = 0; i < cities.size(); i++)
        {
            newList.add(cities.get(i));
        }
        return newList;
    }

    public void setCities(Point[] cities) {
        this.cities.clear();
        for (int i = 0; i < cities.length; i++) {
            this.cities.add(cities[i]);
        }
    }

    public List<RoadNetworkSolution> getRoadNetworkPopulation() {
        return roadNetworkPopulation;
    }

    public void setRoadNetworkPopulation(List<RoadNetworkSolution> newNetworkPopulation){
        roadNetworkPopulation = newNetworkPopulation;
    }

    private boolean hasCity(Point city) {
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

    //initializeGA: function that initializes population (random "complete" road networks) based on given size)
    public void initializeGA(int sizeOfPopulation) {
        roadNetworkPopulation.clear();
        for (int j = 0; j < sizeOfPopulation; j++) {
            //within loop, generate one complete individual road network and add to population
            RoadNetworkSolution newNetwork = new RoadNetworkSolution(this);
            Random rand = new Random();
            List<Point> remainingCityList = new ArrayList<Point>();
            Point currentCity = cities.get(rand.nextInt(cities.size()));

            //get random city to start with, update newCityList to keep track of which cities have already been connected
            for (int i = 0; i < cities.size(); i++) {
                remainingCityList.add(cities.get(i));
            }
            remainingCityList.remove(currentCity);

            //starting from first city, make a road to non-connected city, until all cities are connected
            while (remainingCityList.size() > 0) {
                Point newCity = remainingCityList.get(rand.nextInt(remainingCityList.size()));
                Road newRoad = new Road(currentCity, newCity);
                newNetwork.roadNetwork.add(newRoad);
                currentCity = newCity;
                remainingCityList.remove(newCity);
            }

            newNetwork.calculateScore(cities);
            roadNetworkPopulation.add(newNetwork);
        }

        printRoadNetworkPopulation();
    }

    //function getRoads: returns the best current road network (to display in ViewerController.java)
    public Road[] getRoads() {
        if (roadNetworkPopulation.size() == 0) {
            Road[] p = new Road[0];
            return p;
        }
        else {
            //(sorting by score, first element will always be the best so far)
            Collections.sort(roadNetworkPopulation);
            Road[] p = new Road[roadNetworkPopulation.get(0).roadNetwork.size()];
            p = roadNetworkPopulation.get(0).roadNetwork.toArray(p);
            return p;
        }
    }

    public void updateGA()
    {
        //make new generation, made up of best from previous, plus new ones
        ArrayList<RoadNetworkSolution> newGeneration
                = new ArrayList<RoadNetworkSolution>();
        int numberFromPrevious = 5;

        //best from previous
        for (int j = 0; j < numberFromPrevious; j++)
        {
            roadNetworkPopulation.set(j, updateIntersectionsAndDeadEnds(roadNetworkPopulation.get(j)));
            roadNetworkPopulation.get(j).calculateScore(cities);
            newGeneration.add(roadNetworkPopulation.get(j));
        }

        //new solutions
        for (int j = numberFromPrevious; j < roadNetworkPopulation.size(); j++) {
            newGeneration.add(generateNewSolution());
        }

        //now add new solutions
        roadNetworkPopulation.clear();
        for (int i = 0; i < newGeneration.size(); i++)
        {
            roadNetworkPopulation.add(newGeneration.get(i));
        }

        //before printing out again, add mutation to everything (ONLY STICKS IF FINDS BETTER SOLUTION, SEE addMutation() )
        for (int j = 0; j < roadNetworkPopulation.size(); j++)
        {
            roadNetworkPopulation.get(j).addMutation();
            roadNetworkPopulation.get(j).calculateScore(cities);
        }

        printRoadNetworkPopulation();
    }

    //function updateIntersectionsAndDeadends: creates intersections where roads overlap, and removes deadend roads that no longer lead anywhere
    public RoadNetworkSolution updateIntersectionsAndDeadEnds(RoadNetworkSolution r)
    {
        r.updateIntersections();
        r.removeDeadEnds();
        return r;
    }

    //function replaceDuplicateRoads: removes duplicate roads, but also adds new random road into map
    //REASONING: if solution's road amount decreases, it is more likely later that incomplete maps will be made
    //(this might use some further testing to see if necessary)
    public RoadNetworkSolution replaceDuplicateRoads(RoadNetworkSolution r)
    {
        //get number of duplicates that were removed
        int dup = r.removeDuplicates();
        //remove duplicate roads, but replace them with a new road
        while (dup != 0)
        {
            //add new road for every road removed
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

                Road newRoad = new Road(city1, city2);
                r.roadNetwork.add(newRoad);
            }
            //check to see that no more duplicates were made
            dup = r.removeDuplicates();
        }
        return r;
    }

    //function sortThreeRandom: randomly choose three solutions, sort them so best two will be at front.
    public ArrayList<RoadNetworkSolution> sortThreeRandom() {
        ArrayList<RoadNetworkSolution> parents = new ArrayList<RoadNetworkSolution>();
        int r1, r2, r3;

        r1 = rand.nextInt(roadNetworkPopulation.size());
        r2 = rand.nextInt(roadNetworkPopulation.size());
        r3 = rand.nextInt(roadNetworkPopulation.size());

        parents.add(roadNetworkPopulation.get(r1));
        parents.add(roadNetworkPopulation.get(r2));
        parents.add(roadNetworkPopulation.get(r3));

        Collections.sort(parents);
        return parents;
    }

    //function generateNewSolution: makes a new road network, based on previous existing generation
    public RoadNetworkSolution generateNewSolution()
    {
        RoadNetworkSolution newNetwork = new RoadNetworkSolution(this);
        ArrayList<RoadNetworkSolution> parents = sortThreeRandom();

        //now only use parents at 0 and 1 to make child, with better of two having greater probability/influence
        double totalProb = (parents.get(0).score + parents.get(1).score);
        double prob01 = totalProb - parents.get(0).score;
        double prob02 = totalProb - parents.get(1).score;

        //(number of roads in new solution = max of number of roads in parents)
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

        newNetwork = updateIntersectionsAndDeadEnds(newNetwork);
        newNetwork = replaceDuplicateRoads(newNetwork);
        newNetwork.calculateScore(cities);
        return newNetwork;
    }


    //function printRoadNetworkPopulation: prints out information from roadNetworkPopulation
    public void printRoadNetworkPopulation()
    {
        Collections.sort(roadNetworkPopulation);
        for (int j = 0; j < roadNetworkPopulation.size(); j++) {
            System.out.println("Road Network \t" + j + "\t: " + roadNetworkPopulation.get(j).score01
                    + "\t+\t" + roadNetworkPopulation.get(j).score02 + "\t=\t"
                    + roadNetworkPopulation.get(j).score);
        }
    }
}