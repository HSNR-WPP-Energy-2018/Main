package de.hsnr.wpp2018.database;

import org.junit.Test;

import java.time.Month;
import java.util.ArrayList;

public class ElementTest {

    @Test
    public void testValue() {
        ArrayList<Double> values = new ArrayList<>();
        values.add(1d);
        values.add(0d);
        values.add(1d);
        values.add(0d);
        Element element = new Element(15, values, new ArrayList<>());

        System.out.println(element.getValue(Month.JANUARY, 1, 0, 30, 0));
        System.out.println(element.getValue(Month.FEBRUARY, 10, 0, 30, 0));
    }
}
