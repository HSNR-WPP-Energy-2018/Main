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
        Map.Entry<LocalDateTime, Consumption> entry = data.firstEntry();
        while (data.higherEntry(entry.getKey()) != null) {
            double neighborVal = entry.getValue().getValue();
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());

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
                    } else { //taking value from previous entry (which is current time minus 15 minutes) -> we guess that consumption does not change a lot during this time
                        neighborVal = Helper.roundDouble(neighborVal, decimals);
                        values.put(newDate, new Consumption(neighborVal, true));
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