package ca.drakej;

public class Generator {



    public static void main(String []arguments) {
        if (arguments.length != 3) {
            System.err.println("Requires the width and height of the map, and number of cities.");
            return;
        }

        int width = Integer.parseInt(arguments[0]);
        int height = Integer.parseInt(arguments[1]);
        int cities = Integer.parseInt(arguments[2]);

        Map map = new Map(width, height);
        map.generateCities(cities);

        System.err.println(map);

    }
}