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
        if (this.map.getCities().size() == 0) return;

        init(pop);
    }

    // assumes the last one is the best
    public RoadNetworkSolution getBest() {

        if (this.population.size() == 0) return null;

        RoadNetworkSolution[] rns = population.stream().sorted((x, y) -> Double.compare(x.score(), y.score())).toArray(RoadNetworkSolution[]::new);

        return rns[rns.length-1];
    }

    //initializeGA: function that initializes population (number of random roadSequences) based on given size)
    public void init(int numOfPopulation) {

        for (int j = 0; j < numOfPopulation; j++) {

            RoadNetworkSolution newNetwork = new RoadNetworkSolution(map);

            //generate roads randomly (straight lines per city, do not connect to city already with connection)
            Random rand = new Random();
            List<Point> newCityList = new ArrayList<>(map.getCities());


            Point currentCity = map.getCities().get(rand.nextInt(map.getCities().size()));
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

            newNetwork.averageLength();

            population.add(newNetwork);
        }
    }

}
