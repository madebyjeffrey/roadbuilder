
package ca.drakej;

import java.awt.*;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Random;

public class Map {
    private int width;
    private int height;
    private Point[] cities;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        cities = new Point[0];
    }

    public void generateCities(int count) {
        cities = new Point[count];


        for (int i = 0; i < cities.length; ++i) {
            cities[i] = generateCity();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%d %d %d\n", cities.length, this.width, this.height));

        Arrays.stream(cities).forEach(x -> sb.append(String.format("%d %d\n", x.x, x.y)));
        return sb.toString();
    }

    public static Map fromReader(Reader reader) {
        BufferedReader br = new BufferedReader(reader);
        String []lines = br.lines().toArray(String[]::new);
        String []meta = lines[0].split(" ");

        Map map = new Map(Integer.parseInt(meta[1]), Integer.parseInt(meta[2]));
        map.cities = new Point[Integer.parseInt(meta[0])];

        for (int i = 0; i < map.cities.length; ++i) {
            meta = lines[i+1].split(" ");
            map.cities[i] = new Point(Integer.parseInt(meta[0]), Integer.parseInt(meta[1]));
        }

        return map;
    }

    private boolean hasCity(Point city) {
        return Arrays.stream(cities).filter(x -> x != null && x.equals(city)).count() != 0;
    }

    private Point generateCity() {
        Random rnd = new Random();
        while (true) {
            Point x = new Point(rnd.nextInt(width), rnd.nextInt(height));
            if (hasCity(x)) continue;
            else return x;
        }
    }
}