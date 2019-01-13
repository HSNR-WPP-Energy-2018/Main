package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Helper;
import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class Newton implements Algorithm<Newton.Configuration> {
    public static final String NAME = "newton";

    private Map<LocalDateTime, ArrayList<Consumption>> fValuesCreation(Map<LocalDateTime, Consumption> neighborsAsc) {
        Map<LocalDateTime, ArrayList<Consumption>> fValues = new LinkedHashMap<>();

        neighborsAsc.forEach((key, value) -> {
            ArrayList<Consumption> temp = new ArrayList<>();
            temp.add(value);
            for (int j = 1; j < neighborsAsc.size(); j++) {
                temp.add(new Consumption(0));
            }
            fValues.put(key, temp);
        });
        return fValues;
    }

    private double createNewtonPolynoms(Map<LocalDateTime, ArrayList<Consumption>> values, int x, LocalDateTime newDate, LocalDateTime lastDate) {
        int decimals = 6;
        int i = 1;

        //Schritt 1: Polynome berechnen
        LocalDateTime previousKey = null;
        ArrayList<Consumption> previousVal = null;
        for (Map.Entry<LocalDateTime, ArrayList<Consumption>> entry_i : values.entrySet()) {
            if (previousKey != null && previousVal != null) {
                for (int j = 1; j <= i; j++) {
                    double numerator = entry_i.getValue().get(j - 1).getValue() - previousVal.get(j - 1).getValue();
                    double denumerator = (i) - (i - j);
                    if (denumerator == 0) {
                        denumerator = denumerator + 0.001;
                    }
                    double temp = numerator / denumerator;
                    entry_i.getValue().add(j, new Consumption(temp));
                }
                i++;
            }
            previousKey = entry_i.getKey();
            previousVal = entry_i.getValue();

        }


        //Schritt 2: Setze Polynome in folgende Formel ein:
        //P(x) = f[x0]+f[x0,x1](x-x0)+f[x0,x1,x2](x-x0)(x-x1)(x-x2)...
        double p = values.entrySet().iterator().next().getValue().get(0).getValue();
        double a = 1.0;
        int iCounter = 0;
        for (Map.Entry<LocalDateTime, ArrayList<Consumption>> entry : values.entrySet()) {
            //System.out.println(entry.getValue().get(0));
            if (iCounter > 0 && iCounter < values.size()) {
                a = a * (x - (iCounter - 1));
                p = p + entry.getValue().get(iCounter).getValue() * a;
            }
            iCounter++;
        }
        p = Helper.roundDouble(p, decimals);
        /*
        if (p < 0) {
            p = Heuristics.castNegativesToZero(p);
        }
        */

        //System.out.printf("Approximation for next x is " + "%f" + " at " + newDate + "\n", p); //"%f\n"
        if (p == Double.POSITIVE_INFINITY) {
            //Heuristik ergänzen
        }

        return p;
    }

    //TODO: support configured time range: OK
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        double p;
        TreeMap<LocalDateTime, Double> resultMap = new TreeMap<>();
        TreeMap<LocalDateTime, Consumption> neighborsMap = new TreeMap<>();
        Map<LocalDateTime, Consumption> neighborsAsc = new LinkedHashMap<>();
        TreeMap<LocalDateTime, Consumption> values = new TreeMap<>();

        LocalDateTime startDate = configuration.hasStart() ? configuration.getStart() : data.firstKey();
        LocalDateTime endDate = configuration.hasEnd() ? configuration.getEnd() : data.lastKey();

        Map.Entry<LocalDateTime, Consumption> entry = null;
        for (Map.Entry<LocalDateTime, Consumption> localEntry : data.entrySet()) {
            if (localEntry.getKey().equals(startDate))
            {
                entry = localEntry;
            }
        }

        while (entry.getKey().isBefore(endDate) && data.higherEntry(entry.getKey()) != null) { //had to use isBefore instead of !isAfter
            neighborsMap.put(entry.getKey(), entry.getValue());
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());


            Map<LocalDateTime, Consumption> neighborsDesc = neighborsMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.<LocalDateTime, Consumption>comparingByKey().reversed())
                    .collect(toMap(Map.Entry::getKey,
                            Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            //damit die k nächsten Nachbarn nicht später von der falschen Seite abgeschnitten werden


            if (Helper.getDistance(one, two) > configuration.getInterval()) {
                int counter = 0;
                for (Iterator<Map.Entry<LocalDateTime, Consumption>> it = neighborsDesc.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<LocalDateTime, Consumption> entry2 = it.next();
                    if (counter >= configuration.getNeighbors()) {
                        it.remove();
                    }
                    counter++;
                }

                neighborsDesc.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEachOrdered(x -> neighborsAsc.put(x.getKey(), x.getValue()));


                int xAmount = 0;
                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15)) {
                    xAmount++;
                }

                Map<LocalDateTime, ArrayList<Consumption>> fValues = fValuesCreation(neighborsAsc);


                int x = fValues.size(); //Das nächste x, das berechnet werden soll

                List<Map.Entry<LocalDateTime, ArrayList<Consumption>>> entryList = new ArrayList<>(fValues.entrySet());
                LocalDateTime lastDate = entryList.get(entryList.size() - 1).getKey();

                LocalDateTime newDate = lastDate.plusMinutes(15);
                p = createNewtonPolynoms(fValues, x, newDate, lastDate);
                values.put(newDate, new Consumption(p, true));
                resultMap.put(newDate, p);

                /*
                Falls mehrere x-Werte gesucht werden:
                fValues nicht komplett neu initialisieren, sondern das vorhandene Differenzschema nur um eine weitere untere Schrägzeile in der Dreiecksmatrix ergänzen
                -> Spart Rechenzeit
                */

                if (xAmount >= 2) {

                    for (int i = 1; i < xAmount; i++) {
                        ArrayList<Consumption> temp = new ArrayList<>();
                        temp.add(new Consumption(p));
                        for (int j = 1; j < fValues.size(); j++) {
                            temp.add(new Consumption(0));
                        }

                        fValues.put(newDate, temp);

                        x = fValues.size();
                        p = createNewtonPolynoms(fValues, x, newDate, lastDate); //Bei p kommt dann der interpolierte Wert für's neue aktuelle x raus
                        resultMap.put(newDate, p);
                        values.put(newDate, new Consumption(p, true));
                    }
                }
                values.put(one, entry.getValue().copyAsOriginal());
                neighborsDesc.clear();
                neighborsAsc.clear();
                neighborsMap.clear();
            } else {
                values.put(entry.getKey(), entry.getValue().copyAsOriginal());
            }
            entry = data.higherEntry(entry.getKey());
        }
/*
        resultMap.forEach((key, value) ->
        {
            values.add(new Consumption(key, value, true));
            data.put(key, value);
        });
*/
        //values.forEach((time, value) -> System.out.println("Time: " + time + ". Value: " + value.getValue() + ". Interpolated? " + value.isInterpolated()));
        return values;
    }

    @Override
    public String getConfigurationExplanation() {
        return "interval=<int>;{start=<date>;end=<date>;}neighbors=<int>";
    }

    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Map<String, String> configuration) throws ParserException {
        int neighbors = ParserHelper.getInteger(configuration, "neighbors", 0);
        return interpolate(data, new Configuration(Algorithm.Configuration.parse(configuration), neighbors));
    }

    public static class Configuration extends Algorithm.Configuration {
        private int neighbors;

        public Configuration(int interval, int neighbors) {
            super(interval);
            this.neighbors = neighbors;
        }

        public Configuration(Algorithm.Configuration base, int neighbors) {
            super(base.getInterval(), base.getStart(), base.getEnd());
            this.neighbors = neighbors;
        }

        public Configuration(int interval, LocalDateTime start, LocalDateTime end, int neighbors) {
            super(interval, start, end);
            this.neighbors = neighbors;
        }

        public int getNeighbors() {
            return neighbors;
        }
    }
}