package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Helper;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Newton algorithm
 */
public class Newton implements Algorithm<Newton.Configuration> {
    public static final String NAME = "newton";

    /**
     *
     * @param neighborsAsc neighbor values in ascending order
     * @return initialized table with divided differences (with zeros)
     */
    private Map<LocalDateTime, ArrayList<Consumption>> fValuesCreation(Map<LocalDateTime, Consumption> neighborsAsc) {
        Map<LocalDateTime, ArrayList<Consumption>> fValues = new LinkedHashMap<>();

        neighborsAsc.forEach((key, value) -> {
            ArrayList<Consumption> temp = new ArrayList<>();
            temp.add(value);
            for (int j = 1; j < neighborsAsc.size(); j++) {
                temp.add(new Consumption(0));
            }
            fValues.put(key, temp);
        });
        return fValues;
    }

    /**
     *
     * @param values fValues
     * @param x size of fValues matrix -> identifies the index of the current variable x
     * @return interpolation value
     */
    private double createNewtonPolynoms(Map<LocalDateTime, ArrayList<Consumption>> values, int x) {
        int decimals = 6;
        int i = 1;

        /**
         *  Step 1: Calculate polynomials
         */
        LocalDateTime previousKey = null;
        ArrayList<Consumption> previousVal = null;
        for (Map.Entry<LocalDateTime, ArrayList<Consumption>> entry_i : values.entrySet()) {
            if (previousKey != null && previousVal != null) {
                for (int j = 1; j <= i; j++) {
                    double numerator = entry_i.getValue().get(j - 1).getValue() - previousVal.get(j - 1).getValue();
                    double denumerator = (i) - (i - j);
                    if (denumerator == 0) {
                        denumerator = denumerator + 0.001;
                    }
                    double temp = numerator / denumerator;
                    entry_i.getValue().add(j, new Consumption(temp));
                }
                i++;
            }
            previousKey = entry_i.getKey();
            previousVal = entry_i.getValue();

        }


        /**
         *  Step 2: Insert polynomials into the following formula:
           P(x) = f[x0]+f[x0,x1](x-x0)+f[x0,x1,x2](x-x0)(x-x1)(x-x2)...
         */
        double p = values.entrySet().iterator().next().getValue().get(0).getValue();
        double a = 1.0;
        int iCounter = 0;
        for (Map.Entry<LocalDateTime, ArrayList<Consumption>> entry : values.entrySet()) {
            if (iCounter > 0 && iCounter < values.size()) {
                a = a * (x - (iCounter - 1));
                p = p + entry.getValue().get(iCounter).getValue() * a;
            }
            iCounter++;
        }
        p = Helper.roundDouble(p, decimals);

        return p;
    }

    /**
     *
     * @param data          data to be interpolated
     * @param configuration algorithm configuration
     * @return
     */
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        double p;
        TreeMap<LocalDateTime, Double> resultMap = new TreeMap<>();
        TreeMap<LocalDateTime, Consumption> neighborsMap = new TreeMap<>();
        Map<LocalDateTime, Consumption> neighborsAsc = new LinkedHashMap<>();
        TreeMap<LocalDateTime, Consumption> values = new TreeMap<>();

        LocalDateTime startDate = configuration.hasStart() ? configuration.getStart() : data.firstKey();
        LocalDateTime endDate = configuration.hasEnd() ? configuration.getEnd() : data.lastKey();

        Map.Entry<LocalDateTime, Consumption> entry = null;
        for (Map.Entry<LocalDateTime, Consumption> localEntry : data.entrySet()) {
            if (localEntry.getKey().equals(startDate))
            {
                entry = localEntry;
            }
        }

        if(endDate.isAfter(data.lastKey()))
        {
            data.put(endDate.plusMinutes(15), new Consumption(0.0, true));
        }

        while (entry.getKey().isBefore(endDate) && data.higherEntry(entry.getKey()) != null) { //uses isBefore instead of !isAfter
            neighborsMap.put(entry.getKey(), entry.getValue());
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());


            Map<LocalDateTime, Consumption> neighborsDesc = neighborsMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.<LocalDateTime, Consumption>comparingByKey().reversed())
                    .collect(toMap(Map.Entry::getKey,
                            Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            /**
             *  so that the k nearest neighbours are not later cut off from the wrong side
             */


            if ((Helper.getDistance(one, two)/60) > configuration.getInterval()) {
                int counter = 0;
                for (Iterator<Map.Entry<LocalDateTime, Consumption>> it = neighborsDesc.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<LocalDateTime, Consumption> entry2 = it.next();
                    if (counter >= configuration.getNeighbors()) {
                        it.remove();
                    }
                    counter++;
                }

                neighborsDesc.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEachOrdered(x -> neighborsAsc.put(x.getKey(), x.getValue()));


                int xAmount = 0;
                for (LocalDateTime newDate = one.plusMinutes(configuration.getInterval()); newDate.isBefore(two); newDate = newDate.plusMinutes(configuration.getInterval())) {
                    xAmount++;
                }


                Map<LocalDateTime, ArrayList<Consumption>> fValues = fValuesCreation(neighborsAsc);


                int x = fValues.size(); /* next x that has to be calculated */

                List<Map.Entry<LocalDateTime, ArrayList<Consumption>>> entryList = new ArrayList<>(fValues.entrySet());
                LocalDateTime lastDate = entryList.get(entryList.size() - 1).getKey();

                LocalDateTime newDate = lastDate.plusMinutes(configuration.getInterval());
                p = createNewtonPolynoms(fValues, x);
                values.put(newDate, new Consumption(p, true));
                resultMap.put(newDate, p);

                /**
                 *
                If several x-values are needed:
                Do not completely reinitialize fValues -> only add a new lower oblique line to the existing difference scheme in the triangle matrix.
                -> Saves computing time
                */
                if (xAmount >= 2) {

                    for (int i = 1; i <= xAmount; i++) {
                        ArrayList<Consumption> temp = new ArrayList<>();
                        temp.add(new Consumption(p));
                        for (int j = 1; j < fValues.size(); j++) {
                            temp.add(new Consumption(0));
                        }

                        fValues.put(newDate, temp);

                        x = fValues.size();
                        p = createNewtonPolynoms(fValues, x);
                        resultMap.put(newDate, p);
                        values.put(newDate, new Consumption(p, true));
                        newDate = newDate.plusMinutes(configuration.getInterval());
                    }
                }
                values.put(one, entry.getValue().copyAsOriginal());
                entry = data.higherEntry(one);
                neighborsDesc.clear();
                neighborsAsc.clear();
                neighborsMap.clear();
            } else {
                if(!values.containsKey(one))
                {
                    values.put(entry.getKey(), entry.getValue().copyAsOriginal());
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