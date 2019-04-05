package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Helper;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

/**
 * Linear algorithm
 */
public class Linear implements Algorithm<Algorithm.Configuration> {
    public static final String NAME = "linear";


    /**
     *
     * @param x current date without consumption information
     * @return candidate value between neighbors x1 and x2
     */
    public static double interpolateValue(double x, double x1, double x2, double y1, double y2) {
        return y1 + (x - x1) / (x2 - x1) * (y2 - y1);
    }


    /**
     *
     * @param input source data
     * @param configuration algorithm configuration
     * @return
     */


    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> input, Configuration configuration) {

        TreeMap<LocalDateTime, Consumption> data = new TreeMap<>(input);

        int decimals = 5;
        TreeMap<LocalDateTime, Consumption> values = new TreeMap<>();
        int counter = 1;
        double yLinear;

        LocalDateTime startDate = configuration.hasStart() ? configuration.getStart() : data.firstKey();
        LocalDateTime endDate = configuration.hasEnd() ? configuration.getEnd() : data.lastKey();

        Map.Entry<LocalDateTime, Consumption> entry = null;
        for (Map.Entry<LocalDateTime, Consumption> localEntry : data.entrySet()) {
            if (localEntry.getKey().equals(startDate)) {
                entry = localEntry;
            }
        }

        /**
         * sets an unrealistic value for consumption data -> termination condition, because linear interpolation cannot
         * calculate an energy consumption which is in the future
         */
        if (endDate.isAfter(data.lastKey())) {
            data.put(endDate.plusMinutes(15), new Consumption(-100.0, true));
        }

        while (!entry.getKey().isAfter(endDate) && data.higherEntry(entry.getKey()) != null) {
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());
            counter++;
            if ((Helper.getDistance(one, two) / 60) > configuration.getInterval()) {
                if (data.get(two).getValue() != (-100.0)) {

                    /* x1<=x<=x2 */
                    yLinear = interpolateValue(counter, counter - 1, counter + 1, entry.getValue().getValue(), data.higherEntry(entry.getKey()).getValue().getValue());
                    yLinear = Helper.roundDouble(yLinear, decimals);

                    if (!values.containsKey(one)) {
                        values.put(one, entry.getValue().copyAsOriginal());
                    }

                    values.put(one.plusMinutes(configuration.getInterval()), new Consumption(yLinear, true));
                    data.put(one.plusMinutes(configuration.getInterval()), new Consumption(yLinear, true));
                    entry = data.higherEntry(one);
                } else {
                    System.out.println("Linear Interpolation cannot be used for predictions because if tries to find a curve within two known data points. Please use another interpolation algorithm.");
                    break;
                }
            } else {
                values.put(one, entry.getValue());
                entry = data.higherEntry(entry.getKey());
            }
        }
        return values;
    }

    @Override
    public String getConfigurationExplanation() {
        return "interval=<int>";
    }

    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Map<String, String> configuration) throws ParserException {
        return interpolate(data, Algorithm.Configuration.parse(configuration));
    }
}