
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
        //modified by AH  //cities = new Point[0];
    }

    public void generateCities(int count) {
        //cities = new Point[count];


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
        String []lines = br.lines().toArray(String[]::new);
        String []meta = lines[0].split(" ");

        Map map = new Map(Integer.parseInt(meta[1]), Integer.parseInt(meta[2]));
        int citiesLength = Integer.parseInt(meta[0]);//map.cities = new Point[Integer.parseInt(meta[0])];

        for (int i = 0; i < citiesLength; ++i) {
            meta = lines[i+1].split(" ");
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
        for (int i = 0; i < cities.length; i++)
        {
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

    public class Road
    {
        public int x1, x2, x3, x4;

        public Road(int x1, int x2, int x3, int x4)
        {
            this.x1 = x1;
            this.x2 = x2;
            this.x3 = x3;
            this.x4 = x4;
        }

    }


    List<Road> roadNetwork = new ArrayList<Road>();

    public void initializeGA(int numOfPopulation)
    {
        //generate roads randomly (straight lines per city, do not connect to city already with connection)
        roadNetwork.clear();
        Random rand = new Random();

       // List<Point> newCityList = cities;
        List<Point> newCityList = new ArrayList<Point>();
        for (int i = 0; i < cities.size(); i++)
        {
            newCityList.add(cities.get(i));
        }
        Point currentCity = cities.get(rand.nextInt(cities.size()));
        newCityList.remove(currentCity);

        while (newCityList.size() > 0)//numOfPopulation)
        {
            Point newCity = newCityList.get(rand.nextInt(newCityList.size()));
            Road newRoad = new Road(currentCity.x, currentCity.y, newCity.x, newCity.y);
            roadNetwork.add(newRoad);
            currentCity = newCity;
            newCityList.remove(newCity);
        }
    }

    public Road[] getRoads()
    {
        Road[] p = new Road[roadNetwork.size()];
        p = roadNetwork.toArray(p);
        return p;
    }

}