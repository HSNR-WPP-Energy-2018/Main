package de.hsnr.wpp2018;

import de.hsnr.wpp2018.algorithms.*;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
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
                        throw new ParserException("missing configuration string");
                    }
                    runAlgorithm(args[1], args[2], args[3]);
                    break;
                default:
                    printHelp();
                    break;
            }
        } catch (ParserException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }

    private void runAlgorithm(String name, String inputPath, String configurationString) throws ParserException {
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
            default:
                String[] algorithms = { Averaging.NAME, CubicSplines.NAME, DatabaseInterface.NAME, Linear.NAME, Newton.NAME };
                throw new ParserException("unrecognized algorithm. Available options: " + String.join(", ", algorithms));
        }
        File input = new File(inputPath);
        if (!input.exists()) {
            throw new ParserException("input file does not exists");
        }
        Importer importer = new Importer();
        try {
            importer.readFile(input);
        } catch (IOException e) {
            throw new ParserException("Error reading input file");
        }

        Map<String, String> configurationData = new HashMap<>();
        for (String part : configurationString.split(";")) {
            String[] elements = part.split("=");
            if (elements.length != 2) {
                throw new ParserException("Invalid format for configuration part \"" + part + "\"");
            }
            configurationData.put(elements[0], elements[1]);
        }
        TreeMap<LocalDateTime, Consumption> result = algorithm.interpolate(importer.getData(), configurationData);
        System.out.println("Original: " + importer.getData().size());
        System.out.println("Interpolated: " + result.size());
    }

    private void printHelp() {
        System.out.println("======= Available commands =======");
        System.out.println("General syntax: <required parameter> [optional parameter]");
        System.out.println("> run <algorithm> <input-file> <configuration> - print this help");
        System.out.println("> help - print this help");
    }
}
