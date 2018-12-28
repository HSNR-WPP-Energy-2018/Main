package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Helper;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class CubicSplines implements Algorithm<Algorithm.Configuration> {


    public double equationSys(ArrayList<Double> xArray, ArrayList<Double> yArray) {

        double[] x = xArray.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = yArray.stream().mapToDouble(Double::doubleValue).toArray();

        LinearInterpolator interpolation = new LinearInterpolator();
        PolynomialSplineFunction polyFunction = interpolation.interpolate(x, y);

        /* Hier kommen die polynomiellen Funktionen raus, die in Frage kommen
        / Arrays.stream(polyFunction.getPolynomials()).forEach(System.out::println);
        */

        double value = polyFunction.value(x.length); //interpolierter Wert
        return value;
    }

    public TreeMap<LocalDateTime, Double> removingValues(Map<LocalDateTime, Double> neighbors_desc) {
        TreeMap<LocalDateTime, Double> neighbors_asc = new TreeMap<>();
        int local = 0;
        for (Iterator<Map.Entry<LocalDateTime, Double>> it = neighbors_desc.entrySet().iterator(); it.hasNext() && local < 4; ) {
            Map.Entry<LocalDateTime, Double> entry2 = it.next();
            neighbors_asc.put(entry2.getKey(), entry2.getValue());
            local++;
        }
        return neighbors_asc;
    }


    public ArrayList<Consumption> interpolate(TreeMap<LocalDateTime, Double> data, Configuration configuration) {
        int decimals = 5;
        ArrayList<Algorithm.Consumption> values = new ArrayList<>();  //Neue ArrayList mit den interpolierten Ergebnissen
        TreeMap<LocalDateTime, Double> neighbors_map = new TreeMap<>();

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

                synchronized (neighbors_desc) {
                    int counter = 0;
                    for (Iterator<Map.Entry<LocalDateTime, Double>> it = neighbors_desc.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<LocalDateTime, Double> entry2 = it.next();
                        if (counter >= 4) {
                            it.remove();
                        }
                        counter++;
                    }
                }


                double nextVal = data.get(two);
                TreeMap<LocalDateTime, Double> neighbors_asc = removingValues(neighbors_desc);

                ArrayList<Double> xArray = new ArrayList<>();
                ArrayList<Double> yArray = new ArrayList<>();

                for (int i = 0; i < neighbors_asc.size() + 1; i++) {
                    if (i != neighbors_asc.size()) {
                        xArray.add(Double.valueOf(i + 1));
                    } else {
                        xArray.add(Double.valueOf(i + 2));
                    }
                }

                neighbors_asc.forEach((key, value) -> yArray.add(value));
                yArray.add(nextVal);

                double result = equationSys(xArray, yArray);
                result = Helper.roundDouble(result,decimals);

                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15)) {
                    values.add(new Algorithm.Consumption(newDate, result, true));
                }

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