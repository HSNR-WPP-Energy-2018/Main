package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Helper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Linear implements Algorithm<Algorithm.Configuration> {

    private double interpolateValue(double x, double x1, double x2, double y1, double y2) {
        return y1 + (x - x1) / (x2 - x1) * (y2 - y1);
    }

    public ArrayList<Consumption> interpolate(TreeMap<LocalDateTime, Double> data, Configuration configuration) {
        int decimals = 5;
        ArrayList<Algorithm.Consumption> values = new ArrayList<>();  //Neue ArrayList mit den interpolierten Ergebnissen
        int counter = 1;
        double y_linear;
        Map.Entry<LocalDateTime, Double> entry = data.firstEntry();
        while (data.higherEntry(entry.getKey()) != null) {
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());
            counter++;
            if (Helper.getDistance(one, two) > configuration.getInterval()) {
                //x1<=x<=x2s
                y_linear = interpolateValue(counter, counter - 1, counter + 1, entry.getValue(), data.higherEntry(entry.getKey()).getValue());
                y_linear = Helper.roundDouble(y_linear,decimals);
                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15)) {
                    values.add(new Algorithm.Consumption(newDate,y_linear,true));
                }
            }
            else
            {
                values.add(new Algorithm.Consumption(one,entry.getValue(),false));
            }
            entry = data.higherEntry(entry.getKey());
        }


        return values;
    }


}