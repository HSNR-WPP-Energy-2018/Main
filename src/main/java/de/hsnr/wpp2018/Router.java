package de.hsnr.wpp2018;

import com.sun.istack.internal.Nullable;
import de.hsnr.wpp2018.algorithms.*;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;
import de.hsnr.wpp2018.evaluation.Analyser;
import de.hsnr.wpp2018.io.Exporter;
import de.hsnr.wpp2018.io.Importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Router {

    public static void main(String[] args) {
        System.out.println(String.join(", ", args));
        new Router(args).run();
    }

    private String[] args;

    private Router(String[] args) {
        this.args = args;
    }

    private void run() {
        String command = (args.length > 0) ? args[0].toLowerCase() : "";
        try {
            switch (command) {
                case "run":
                    if (args.length < 2) {
                        throw new ParserException("missing algorithm name");
                    }
                    if (args.length < 3) {
                        throw new ParserException("missing input file location");
                    }
                    if (args.length < 4) {
                        throw new ParserException("missing output file location");
                    }
                    if (args.length < 5) {
                        throw new ParserException("missing configuration string");
                    }
                    runAlgorithm(args[1], args[2], args[3], args[4]);
                    break;
                case "recommend":
                    if (args.length < 2) {
                        throw new ParserException("missing input file location");
                    }
                    if (args.length < 3) {
                        throw new ParserException("missing interval");
                    }
                    int inteval = ParserHelper.getInteger(args[2]);
                    LocalDateTime from = null, to = null;
                    if (args.length > 4) {
                        from = ParserHelper.getDate(args[3]);
                        to = ParserHelper.getDate(args[4]);
                    }
                    printRecommendation(args[1], inteval, from, to);
                default:
                    printHelp();
                    break;
            }
        } catch (ParserException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }

    private TreeMap<LocalDateTime, Consumption> readData(String path) throws ParserException {
        File input = new File(path);
        if (!input.exists()) {
            throw new ParserException("input file does not exist");
        }
        Importer importer = new Importer();
        try {
            importer.readFile(input);
        } catch (IOException e) {
            throw new ParserException("Error reading input file");
        }
        return importer.getData();
    }

    private void runAlgorithm(String name, String inputFile, String outputFile, String configurationString) throws ParserException {
        Algorithm algorithm;
        switch (name.toLowerCase()) {
            case Averaging.NAME:
                algorithm = new Averaging();
                break;
            case CubicSplines.NAME:
                algorithm = new CubicSplines();
                break;
            case DatabaseInterface.NAME:
                algorithm = new DatabaseInterface();
                break;
            case Linear.NAME:
                algorithm = new Linear();
                break;
            case Newton.NAME:
                algorithm = new Newton();
                break;
            case Yesterday.NAME:
                algorithm = new Yesterday();
                break;
            default:
                String[] algorithms = { Averaging.NAME, CubicSplines.NAME, DatabaseInterface.NAME, Linear.NAME, Newton.NAME };
                throw new ParserException("unrecognized algorithm. Available options: " + String.join(", ", algorithms));
        }
        TreeMap<LocalDateTime, Consumption> data = readData(inputFile);

        Map<String, String> configurationData = new HashMap<>();
        for (String part : configurationString.split(";")) {
            String[] elements = part.split("=");
            if (elements.length != 2) {
                throw new ParserException("Invalid format for configuration part \"" + part + "\"");
            }
            configurationData.put(elements[0], elements[1]);
        }
        TreeMap<LocalDateTime, Consumption> result = algorithm.interpolate(data, configurationData);
        System.out.println("Original: " + data.size());
        System.out.println("Interpolated: " + result.size());
        try {
            Exporter.writeConsumption(result, outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("could not write output file");
        }
    }

    private void printRecommendation(String inputFile, int interval, @Nullable LocalDateTime from, @Nullable LocalDateTime to) throws ParserException {
        TreeMap<LocalDateTime, Consumption> data = readData(inputFile);
        List<String> recommendations = (from == null) ? Analyser.recommendAlgorithm(data, interval) : Analyser.recommendAlgorithm(data, from, to, interval);
        System.out.println("Recommended algorithms: " + String.join(", ", recommendations));
    }

    private void printHelp() {
        System.out.println("======= Available commands =======");
        System.out.println("General syntax: <required parameter> [optional parameter]");
        System.out.println("> help - print this help");
        System.out.println("> run <algorithm> <input-file> <output-file> <configuration> - run a specified algorithm with the provided configuration");
        System.out.println("> recommend <input-file> <interval> [from-date to-date] - recommend algorithm");
    }
}
