package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Helper;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;


import static de.hsnr.wpp2018.Helper.isBusinessDay;

public class Yesterday implements Algorithm<Algorithm.Configuration> {
    public static final String NAME = "yesterday";


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
                values.put(one, entry.getValue().copyAsOriginal());
                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15)) {
                    LocalDateTime yesterday = newDate.minusDays(1);


                    if (isBusinessDay(newDate) && !isBusinessDay(yesterday)) {
                        while (!isBusinessDay(yesterday)) {
                            yesterday = yesterday.minusDays(1);
                        }
                    } else if (!isBusinessDay(newDate) && isBusinessDay(yesterday)) {
                        while (isBusinessDay(yesterday)) {
                            yesterday = yesterday.minusDays(1);
                        }

                    }


                    if (values.containsKey(yesterday)) {
                        values.put(newDate, new Consumption(values.get(yesterday).getValue(), true));
                    } else { //values missing already on the first day in the list -> taking linear instead
                        yLinear = Linear.interpolateValue(counter, counter - 1, counter + 1, entry.getValue().getValue(), data.higherEntry(entry.getKey()).getValue().getValue());
                        yLinear = Helper.roundDouble(yLinear, decimals);
                        values.put(newDate, new Consumption(yLinear, true));
                    }

                }

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
        int interval = ParserHelper.getInteger(configuration, "interval", 0);
        return interpolate(data, new Configuration(interval));
    }
}