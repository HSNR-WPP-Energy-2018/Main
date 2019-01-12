package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Helper;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toMap;


public class CubicSplines implements Algorithm<CubicSplines.Configuration> {
    public static final String NAME = "cubic-splines";

    //TODO: support configured time range
    public double equationSys(ArrayList<Double> xArray, ArrayList<Double> yArray) {
        double[] x = xArray.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = yArray.stream().mapToDouble(Double::doubleValue).toArray();

        LinearInterpolator interpolation = new LinearInterpolator();
        PolynomialSplineFunction polyFunction = interpolation.interpolate(x, y);

        /* Hier kommen die polynomiellen Funktionen raus, die in Frage kommen
        / Arrays.stream(polyFunction.getPolynomials()).forEach(System.out::println);
        */
        return polyFunction.value(x.length);
    }

    public TreeMap<LocalDateTime, Double> removingValues(Map<LocalDateTime, Double> neighborsDesc) {
        TreeMap<LocalDateTime, Double> neighborsAsc = new TreeMap<>();
        int local = 0;
        for (Iterator<Map.Entry<LocalDateTime, Double>> it = neighborsDesc.entrySet().iterator(); it.hasNext() && local < 4; ) {
            Map.Entry<LocalDateTime, Double> entry2 = it.next();
            neighborsAsc.put(entry2.getKey(), entry2.getValue());
            local++;
        }
        return neighborsAsc;
    }

    //TODO: support configured time range
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        int decimals = 5;
        TreeMap<LocalDateTime, Consumption> values = new TreeMap<>();
        TreeMap<LocalDateTime, Double> neighborsMap = new TreeMap<>();

        Map.Entry<LocalDateTime, Consumption> entry = data.firstEntry();
        while (data.higherEntry(entry.getKey()) != null) {
            neighborsMap.put(entry.getKey(), entry.getValue().getValue());
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());

            Map<LocalDateTime, Double> neighborsDesc = neighborsMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.<LocalDateTime, Double>comparingByKey().reversed())
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            if (Helper.getDistance(one, two) > configuration.getInterval()) {
                synchronized (neighborsDesc) {
                    int counter = 0;
                    for (Iterator<Map.Entry<LocalDateTime, Double>> it = neighborsDesc.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<LocalDateTime, Double> entry2 = it.next();
                        if (counter >= 4) {
                            it.remove();
                        }
                        counter++;
                    }
                }

                double nextVal = data.get(two).getValue();
                TreeMap<LocalDateTime, Double> neighborsAsc = removingValues(neighborsDesc);

                ArrayList<Double> xArray = new ArrayList<>();
                ArrayList<Double> yArray = new ArrayList<>();

                for (int i = 0; i < neighborsAsc.size() + 1; i++) {
                    if (i != neighborsAsc.size()) {
                        xArray.add(i + 1d);
                    } else {
                        xArray.add(i + 2d);
                    }
                }

                neighborsAsc.forEach((key, value) -> yArray.add(value));
                yArray.add(nextVal);

                double result = equationSys(xArray, yArray);
                result = Helper.roundDouble(result, decimals);
                values.put(one, entry.getValue().copyAsOriginal());
                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15)) {
                    values.put(newDate, new Consumption(result, true));
                }

                neighborsDesc.clear();
                neighborsAsc.clear();
                neighborsMap.clear();
            } else {
                values.put(one, entry.getValue().copyAsOriginal());
            }
            entry = data.higherEntry(entry.getKey());
        }
        //values.forEach((time, value) -> System.out.println("Time: " + time + ". Value: " + value.getValue() + ". Interpolated? " + value.isInterpolated()));
        return values;

    }

    @Override
    public String getConfigurationExplanation() {
        return "interval=<int>;{start=<date>;end=<date>;}neighbors=<int>";
    }

    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Map<String, String> configuration) throws ParserException {
        int neighbors = ParserHelper.getInteger(configuration, "neighbors", 0);
        return interpolate(data, new Configuration(Algorithm.Configuration.parse(configuration), neighbors));
    }

    public static class Configuration extends Algorithm.Configuration {
        private int neighbors;

        public Configuration(int interval, int neighbors) {
            super(interval);
            this.neighbors = neighbors;
        }

        public Configuration(Algorithm.Configuration base, int neighbors) {
            super(base.getInterval(), base.getStart(), base.getEnd());
            this.neighbors = neighbors;
        }

        public Configuration(int interval, LocalDateTime start, LocalDateTime end, int neighbors) {
            super(interval, start, end);
            this.neighbors = neighbors;
        }

        public int getNeighbors() {
            return neighbors;
        }
    }
}