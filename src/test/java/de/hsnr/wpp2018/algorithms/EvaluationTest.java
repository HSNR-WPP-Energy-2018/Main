package de.hsnr.wpp2018.algorithms;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class EvaluationTest extends BaseTest {

    @Test
    public void evaluateAveraging() {
        ArrayList<Integer> adjusters = new ArrayList<>();
        adjusters.add(Math.toIntExact(TimeUnit.DAYS.toMinutes(7)));
        adjusters.add(Math.toIntExact(TimeUnit.DAYS.toMinutes(1)));
        Averaging.Configuration configuration = new Averaging.Optimizer(AlgorithmTest.INTERVAL, adjusters, 1, 10, 1, 10, 0.1).optimize(original, testData);
        System.out.println("Configuration for best score: " + configuration);
        result = new Averaging().interpolate(testData, configuration);
    }
}