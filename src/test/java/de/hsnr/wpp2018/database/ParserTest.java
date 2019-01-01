package de.hsnr.wpp2018.database;

import org.junit.Test;

public class ParserTest {

    @Test
    public void test() {
        String data =
                "15\n" +
                "test,number=1.5,tolerance=10:1.1\n" +
                "17,23.4\n" +
                "1\n" +
                "3\n";
        Element element;
        try {
            element = Parser.parse(data);
            System.out.println("Parsed element: " + element);
        } catch (Parser.ParseException e) {
            System.out.println("error parsing data: " + e);
        }
    }
}