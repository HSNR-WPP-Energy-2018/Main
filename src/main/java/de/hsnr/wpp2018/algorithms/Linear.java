package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Helper;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class Linear implements Algorithm<Algorithm.Configuration> {
    public static final String NAME = "linear";

    private double interpolateValue(double x, double x1, double x2, double y1, double y2) {
        return y1 + (x - x1) / (x2 - x1) * (y2 - y1);
    }

    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        int decimals = 5;
        TreeMap<LocalDateTime, Consumption> values = new TreeMap<>();
        int counter = 1;
        double yLinear;
        Map.Entry<LocalDateTime, Consumption> entry = data.firstEntry();
        while (data.higherEntry(entry.getKey()) != null) {
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());
            counter++;
            if (Helper.getDistance(one, two) > configuration.getInterval()) {
                //x1<=x<=x2s
                yLinear = interpolateValue(counter, counter - 1, counter + 1, entry.getValue().getValue(), data.higherEntry(entry.getKey()).getValue().getValue());
                yLinear = Helper.roundDouble(yLinear, decimals);
                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15)) {
                    values.put(newDate, new Consumption(yLinear, true));
                }
            } else {
                values.put(one, entry.getValue());
            }
            entry = data.higherEntry(entry.getKey());
        }

        return values;
    }

    @Override
    public String getConfigurationExplanation() {
        return "interval=<int>";
    }

    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Map<String, String> configuration) throws ParserException {
        int interval = ParserHelper.getInteger(configuration, "interval", 0);
        return interpolate(data, new Configuration(interval));
    }
}