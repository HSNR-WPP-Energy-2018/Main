package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Helper;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

import static de.hsnr.wpp2018.base.Helper.isBusinessDay;

/**
 * Yesterday algorithm
 */
public class Yesterday implements Algorithm<Algorithm.Configuration> {
    public static final String NAME = "yesterday";

    /**
     *
     * @param data          data to be interpolated
     * @param configuration algorithm configuration
     * @return
     */
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        int decimals = 5;
        TreeMap<LocalDateTime, Consumption> values = new TreeMap<>();


        LocalDateTime startDate = configuration.hasStart() ? configuration.getStart() : data.firstKey();
        LocalDateTime endDate = configuration.hasEnd() ? configuration.getEnd() : data.lastKey();

        Map.Entry<LocalDateTime, Consumption> entry = null;
        for (Map.Entry<LocalDateTime, Consumption> localEntry : data.entrySet()) {
            if (localEntry.getKey().equals(startDate)) {
                entry = localEntry;
            }
        }

        if (endDate.isAfter(data.lastKey())) {
            data.put(endDate.plusMinutes(15), new Consumption(0.0, true));
        }


        while (!entry.getKey().isAfter(endDate) && data.higherEntry(entry.getKey()) != null) {
            double neighborVal = entry.getValue().getValue();
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());

            if ((Helper.getDistance(one, two) / 60) > configuration.getInterval()) {
                values.put(one, entry.getValue().copyAsOriginal());
                /**
                 * if data is missing, the algorithm takes the energy consumption from the previous day at the same time
                 * (by noticing consumption differences at weekdays/weekends)
                 */
                for (LocalDateTime newDate = one.plusMinutes(configuration.getInterval()); newDate.isBefore(two); newDate = newDate.plusMinutes(configuration.getInterval())) {
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
                    } else {
                        /**
                         *
                         * taking value from previous entry (which is current time minus 15 minutes) -> we guess that consumption does not change a lot during this time
                         */

                        neighborVal = Helper.roundDouble(neighborVal, decimals);
                        values.put(newDate, new Consumption(neighborVal, true));
                    }


                }

            } else {
                if (!values.containsKey(one)) {
                    values.put(one, entry.getValue().copyAsOriginal());
                }
            }
            entry = data.higherEntry(entry.getKey());
        }
        return values;
    }

    @Override
    public String getConfigurationExplanation() {
        return "interval=<int>;{start=<date>;end=<date>}";
    }

    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Map<String, String> configuration) throws ParserException {
        return interpolate(data, Algorithm.Configuration.parse(configuration));
    }
}