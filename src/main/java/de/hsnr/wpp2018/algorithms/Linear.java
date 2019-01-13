package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Helper;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class Linear implements Algorithm<Algorithm.Configuration> {
    public static final String NAME = "linear";

    public static double interpolateValue(double x, double x1, double x2, double y1, double y2) {
        return y1 + (x - x1) / (x2 - x1) * (y2 - y1);
    }

    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        int decimals = 5;
        TreeMap<LocalDateTime, Consumption> values = new TreeMap<>();
        int counter = 1;
        double yLinear;

        LocalDateTime startDate = configuration.hasStart() ? configuration.getStart() : data.firstKey();
        LocalDateTime endDate = configuration.hasEnd() ? configuration.getEnd() : data.lastKey();

        Map.Entry<LocalDateTime, Consumption> entry = null;
        for (Map.Entry<LocalDateTime, Consumption> localEntry : data.entrySet()) {
            if (localEntry.getKey().equals(startDate))
            {
                entry = localEntry;
            }
        }

        //under constuction
        for (LocalDateTime newDate = data.lastKey(); newDate.isBefore(endDate.plusMinutes(15)); newDate = newDate.plusMinutes(15)) {
            data.put(endDate.plusMinutes(15), new Consumption(0.0, true));
        }

        while (!entry.getKey().isAfter(endDate) && data.higherEntry(entry.getKey()) != null) {
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());
            counter++;
            if (Helper.getDistance(one, two) > configuration.getInterval()) {
                //x1<=x<=x2s
                yLinear = interpolateValue(counter, counter - 1, counter + 1, entry.getValue().getValue(), data.higherEntry(entry.getKey()).getValue().getValue());
                yLinear = Helper.roundDouble(yLinear, decimals);

                //values.put(one, entry.getValue().copyAsOriginal());

                if(!values.containsKey(one)) {
                    values.put(one, entry.getValue().copyAsOriginal());
                }
                values.put(one.plusMinutes(15), new Consumption(yLinear,true));

                if(!data.containsKey(one.plusMinutes(15)))
                {
                    data.put(one.plusMinutes(15), new Consumption(yLinear,true));
                }

                /*
                for (LocalDateTime newDate = one; newDate.isBefore(two); newDate = newDate.plusMinutes(15)) {
                    values.put(newDate, new Consumption(yLinear, true));
                }
                */
            } else {
                values.put(one, entry.getValue());
            }
            entry = data.higherEntry(entry.getKey());
        }
        //values.forEach((time, value) -> System.out.println("Time: " + time + ". Value: " + value.getValue() + ". Interpolated? " + value.isInterpolated()));
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