package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Household;
import de.hsnr.wpp2018.database.*;
import de.hsnr.wpp2018.optimizations.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AlgorithmTest extends BaseTest {
    static final int INTERVAL = 15 * 60;
    private static final int PERSONS = 2;
    private static final double SIZE = 80.0;

    private void applyHeuristics(int algorithmNumber) {
        Holidays.scanFile("Thüringen");

        switch (algorithmNumber) {
            case 1:
                Heuristics.compareWithYesterday(result, new Household(PERSONS, SIZE)); //noch umbauen!
                break;
            case 2:
                AvgNightDay.nightDayWaste(result, new Household(PERSONS, SIZE));
                SeasonalDifferences.adjustSeasons(result, true);
                break;
            case 3:
                PatternRecognition.checkBehaviour(result, 4, 0.3);
        }
    }

    @Test
    public void linear() {
        result = new Linear().interpolate(testData, new Algorithm.Configuration(INTERVAL));
        applyHeuristics(1);
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
        applyHeuristics(3);
    }

    @Test
    public void database() {
        Database database = new Database();
        ArrayList<Descriptor> descriptors = new ArrayList<>();
        descriptors.add(new StringDescriptor("test"));
        database.addElement(new Element(INTERVAL, ElementKey.Type.WEEK_OF_YEAR, original, descriptors));
        result = new DatabaseInterface().interpolate(testData, new DatabaseInterface.Configuration(INTERVAL, database, descriptors));
    }
}