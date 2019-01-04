package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Household;
import de.hsnr.wpp2018.database.Database;
import de.hsnr.wpp2018.database.Descriptor;
import de.hsnr.wpp2018.database.Element;
import de.hsnr.wpp2018.database.StringDescriptor;
import de.hsnr.wpp2018.optimizations.*;
import org.junit.Test;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AlgorithmTest extends BaseTest {
    static final int INTERVAL = 15 * 60;
    private static final int PERSONS = 2;
    private static final double SIZE = 80.0;

    private void applyHeuristics(int algorithmNumber) {
        Holidays.scanFile();
        switch (algorithmNumber) {
            case 1:
                Heuristics.useHeuristics(result, new Household(PERSONS, SIZE)); //noch umbauen!
                break;
            case 2:
                AvgNightDay.nightDayWaste(result, new Household(PERSONS, SIZE));
                SeasonalDifferences.adjustSeasons(result, true);
                break;
            case 3: //Die Ergebnisse von linear + case 3 scheinen meist recht realistisch zu sein
                PatternRecognition.checkBehaviour(result, 3, 0.1);
        }
    }

    @Test
    public void linear() {
        result = new Linear().interpolate(testData, new Algorithm.Configuration(INTERVAL));
        applyHeuristics(2);
    }

    @Test
    public void newton() {
        result = new Newton().interpolate(testData, new Newton.Configuration(INTERVAL, 10));
        applyHeuristics(2);
    }

    @Test
    public void averaging() {
        ArrayList<Averaging.ConfigurationInterval> intervals = new ArrayList<>();
        intervals.add(new Averaging.ConfigurationInterval(5, Math.toIntExact(TimeUnit.DAYS.toSeconds(7)), true, 5));
        intervals.add(new Averaging.ConfigurationInterval(7, Math.toIntExact(TimeUnit.DAYS.toSeconds(1)), true, 3));
        intervals.add(new Averaging.ConfigurationInterval(15, INTERVAL));
        result = new Averaging().interpolate(testData, new Averaging.Configuration(INTERVAL, intervals));
    }

    @Test
    public void splines() {
        result = new CubicSplines().interpolate(testData, new CubicSplines.Configuration(INTERVAL, 10));
        applyHeuristics(2);
    }

    @Test
    public void database() {
        Database database = new Database();
        List<Double> values = new ArrayList<>();
        values.add(0d);
        values.addAll(original.entrySet().stream()
                .filter(entry -> !(entry.getKey().getMonth().equals(Month.FEBRUARY) && entry.getKey().getDayOfMonth() == 29))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().getValue())
                .collect(Collectors.toList())
        );
        ArrayList<Descriptor> descriptors = new ArrayList<>();
        descriptors.add(new StringDescriptor("test"));
        database.addElement(new Element(INTERVAL, values, descriptors));
        result = new DatabaseInterface(database).interpolate(testData, new DatabaseInterface.Configuration(INTERVAL, descriptors));
    }
}