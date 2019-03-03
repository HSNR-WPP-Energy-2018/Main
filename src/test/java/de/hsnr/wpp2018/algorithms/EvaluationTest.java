package de.hsnr.wpp2018.algorithms;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Simple test for the averaging evaluation implemented in {@link Averaging.Optimizer}
 */
public class EvaluationTest extends BaseTest {

    @Test
    public void evaluateAveraging() {
        ArrayList<Integer> adjusters = new ArrayList<>();
        adjusters.add(Math.toIntExact(TimeUnit.DAYS.toSeconds(7)));
        adjusters.add(Math.toIntExact(TimeUnit.DAYS.toSeconds(1)));
        Averaging.Configuration configuration = new Averaging.Optimizer(AlgorithmTest.INTERVAL, adjusters, 1, 10, 1, 10, 1).optimize(original, testData);
        System.out.println("Configuration for best score: " + configuration);
        result = new Averaging().interpolate(testData, configuration);
    }
}