package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Helper;
import de.hsnr.wpp2018.LinearEquationSystem;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class CubicSplines implements Algorithm<Algorithm.Configuration> {


    public void equate() {


    }


    public ArrayList<Consumption> interpolate(TreeMap<LocalDateTime, Double> data, Configuration configuration) {
        int decimals = 5;
        ArrayList<Algorithm.Consumption> values = new ArrayList<>();  //Neue ArrayList mit den interpolierten Ergebnissen
        TreeMap<LocalDateTime, Double> neighbors_map = new TreeMap<>();
        Map<LocalDateTime, Double> neighbors_asc = new LinkedHashMap<>();
        double y_spline = 0;

        Map.Entry<LocalDateTime, Double> entry = data.firstEntry();
        while (data.higherEntry(entry.getKey()) != null) {
            neighbors_map.put(entry.getKey(), entry.getValue());
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());

            Map<LocalDateTime, Double> neighbors_desc = neighbors_map.entrySet()
                    .stream()
                    .sorted(Map.Entry.<LocalDateTime, Double>comparingByKey().reversed())
                    .collect(toMap(Map.Entry::getKey,
                            Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


            if (Helper.getDistance(one, two) > configuration.getInterval()) {

                int counter = 0;
                for (Iterator<Map.Entry<LocalDateTime, Double>> it = neighbors_desc.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<LocalDateTime, Double> entry2 = it.next();
                    if (counter >= 4) {
                        it.remove();
                    }
                    counter++;
                }


                neighbors_desc.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEachOrdered(x -> neighbors_asc.put(x.getKey(), x.getValue()));

                double nextVal = data.get(two);

                neighbors_asc.forEach((key, value) -> System.out.println("Key: " + key + ". Value: " + value));

                System.out.println("Aktueller Wert ist: " + data.higherEntry(one) + " & " + nextVal);
                ArrayList <Double> xArray = new ArrayList<>();
                ArrayList <Double> yArray = new ArrayList<>();

                for (int i=0; i<5;i++)
                {
                    xArray.add(Double.valueOf(i+1));
                }

                neighbors_asc.forEach((key, value) -> yArray.add(value));
                yArray.add(nextVal);
                double[] x = xArray.stream().mapToDouble(Double::doubleValue).toArray();
                double[] y = yArray.stream().mapToDouble(Double::doubleValue).toArray();
                //LinearEquationSystem sys = new LinearEquationSystem(); //ab hier gibt es den Fehler
                //sys.equate();

                //x1<=x<=x2s

                //ylinear berechnen
                /*
                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15)) {
                    values.add(new Algorithm.Consumption(newDate,y_linear,true));
                }
                */

                neighbors_desc.clear();
                neighbors_asc.clear();
                neighbors_map.clear();
            } else {
                values.add(new Algorithm.Consumption(one, entry.getValue(), false));
            }
            entry = data.higherEntry(entry.getKey());
        }

        return values;
    }


    public static class Configuration extends Algorithm.Configuration {
        private int neighbors;

        public Configuration(int interval, int neighbors) {
            super(interval);
            this.neighbors = neighbors;
        }

        public int getNeighbors() {
            return neighbors;
        }
    }


}