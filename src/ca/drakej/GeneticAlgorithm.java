package ca.drakej;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by drakej on 14-12-07.
 */
public class GeneticAlgorithm {
    private Map map;
    private int pop;
    private List<RoadNetworkSolution> population;

    public GeneticAlgorithm(Map map, int population) {
        this.map = map;
        this.pop = population;

        this.population = new ArrayList<>();

        init(pop);
    }

    //initializeGA: function that initializes population (number of random roadSequences) based on given size)
    public void init(int numOfPopulation) {

        for (int j = 0; j < numOfPopulation; j++) {

            RoadNetworkSolution newNetwork = new RoadNetworkSolution(map);

            //generate roads randomly (straight lines per city, do not connect to city already with connection)
            Random rand = new Random();
            List<Point> newCityList = Arrays.stream(map.getCities()).collect(Collectors.toList());


            Point currentCity = map.getCities()[rand.nextInt(map.getCities().length)];
            newCityList.remove(currentCity);

            while (newCityList.size() > 0) {
                Point newCity = newCityList.get(rand.nextInt(newCityList.size()));
                Road newRoad = new Road(currentCity, newCity);
                newNetwork.roadNetwork.add(newRoad);
                currentCity = newCity;
                newCityList.remove(newCity);
            }

            newNetwork.score();
            System.out.println("Road Network " + j + ": " + newNetwork.complete() + " + " + newNetwork.totalLength() + " = " + newNetwork.score());

            population.add(newNetwork);
        }
    }

}
