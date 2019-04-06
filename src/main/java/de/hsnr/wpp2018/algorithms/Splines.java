package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Helper;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Splines algorithm
 */
public class Splines implements Algorithm<Splines.Configuration> {
    public static final String NAME = "splines";

    /**
     *
     * @param xArray supporting points (x) for the calculation of the polynomial function
     * @param yArray supporting points (y)
     * @return interpolated value
     */
    public double equationSys(ArrayList<Double> xArray, ArrayList<Double> yArray) {
        double[] x = xArray.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = yArray.stream().mapToDouble(Double::doubleValue).toArray();

        LinearInterpolator interpolation = new LinearInterpolator();
        /* Some charakteristics of the function, e.g. the polynomials, can be verified by printing polyFunction.getPolynomials() */
        PolynomialSplineFunction polyFunction = interpolation.interpolate(x, y);

        return polyFunction.value(x.length);
    }

    /**
     *
     * @param neighborsDesc list of the neighbor values in descending order
     * @return modified list of neighbor values (4 neighbors in ascending order)
     */
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

    /**
     *
     * @param data          data to be interpolated
     * @param configuration algorithm configuration
     * @return
     */
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        int decimals = 5;
        TreeMap<LocalDateTime, Consumption> values = new TreeMap<>();
        TreeMap<LocalDateTime, Double> neighborsMap = new TreeMap<>();

        LocalDateTime startDate = configuration.hasStart() ? configuration.getStart() : data.firstKey();
        LocalDateTime endDate = configuration.hasEnd() ? configuration.getEnd() : data.lastKey();

        Map.Entry<LocalDateTime, Consumption> entry = null;
        for (Map.Entry<LocalDateTime, Consumption> localEntry : data.entrySet()) {
            if (localEntry.getKey().equals(startDate)) {
                entry = localEntry;
            }
        }


        if (endDate.isAfter(data.lastKey())) {
            data.put(endDate.plusMinutes(15), new Consumption(-100.0, true));
        }

        while (!entry.getKey().isAfter(endDate) && data.higherEntry(entry.getKey()) != null) {
            neighborsMap.put(entry.getKey(), entry.getValue().getValue());
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());

            Map<LocalDateTime, Double> neighborsDesc = neighborsMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.<LocalDateTime, Double>comparingByKey().reversed())
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            if ((Helper.getDistance(one, two) / 60) > configuration.getInterval()) {
                /**
                 * This spline algorithm also can only estimate the energy consumption between existing values
                 * Termination if date is "in the future"
                 */
                if (data.get(two).getValue() != (-100.0)) {
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


                    if (!values.containsKey(one)) {
                        values.put(one, entry.getValue().copyAsOriginal());
                    }

                    values.put(one.plusMinutes(configuration.getInterval()), new Consumption(result, true));
                    data.put(one.plusMinutes(configuration.getInterval()), new Consumption(result, true));
                    entry = data.higherEntry(one);
                    neighborsDesc.clear();
                    neighborsAsc.clear();
                    neighborsMap.clear();
                } else {
                    System.out.println("Cubic Splines cannot be used for predictions because their goal is to find supporting points between given values. Please use another interpolation algorithm.");
                    break;
                }
            } else {
                if(!values.containsKey(one))
                {
                    values.put(one, entry.getValue().copyAsOriginal());
                }
                entry = data.higherEntry(entry.getKey());
            }
        }
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