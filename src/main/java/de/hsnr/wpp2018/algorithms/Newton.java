package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Helper;
import de.hsnr.wpp2018.Heuristics;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class Newton implements Algorithm<Newton.Configuration> {

    private ArrayList<ArrayList<Double>> f_valuesCreation(ArrayList<Double> neighbors) {
        ArrayList<ArrayList<Double>> f_values = new ArrayList<>();

        for (int i = 0; i < neighbors.size(); i++) {
            ArrayList<Double> temp = new ArrayList<>();
            temp.add(neighbors.get(i));
            for (int j = 1; j < neighbors.size(); j++) {
                temp.add(0.0);
            }
            f_values.add(i, temp);
        }
        return f_values;
    }


    private double createNewtonPolynoms(ArrayList<ArrayList<Double>> f_values, int x) {
        int decimals = 5;
        //Schritt 1: Polynome berechnen
        for (int i = 1; i < f_values.size(); i++) {
            for (int j = 1; j <= i; j++) {
                double numerator = f_values.get(i).get(j - 1) - f_values.get(i - 1).get(j - 1);
                double denumerator = (i) - (i - j);
                if (denumerator==0)
                {
                    denumerator = denumerator + 0.001;
                }
                double temp = numerator / denumerator;
                f_values.get(i).add(j, temp);
            }
        }

        //Schritt 2: Setze Polynome in folgende Formel ein:
        //P(x) = f[x0]+f[x0,x1](x-x0)+f[x0,x1,x2](x-x0)(x-x1)(x-x2)...

        double p = f_values.get(0).get(0);
        double a = 1.0;
        for (int i = 1; i < f_values.size(); i++) {
            a = a * (x - (i - 1));
            p = p + f_values.get(i).get(i) * a;
        }
        p = Helper.roundDouble(p,decimals);
        if (p < 0) {
            p = Heuristics.castNegativesToZero(p);
        }

        //System.out.printf("Approximation for next x is " + "%f\n", p);
        if (p == Double.POSITIVE_INFINITY)
        {
            //Heuristik ergänzen
        }
        return p;
    }

    public TreeMap<LocalDateTime, Double> interpolate(TreeMap<LocalDateTime, Double> data, Newton.Configuration configuration) {
        double P;
        //int size_of_neighbors = 10;
        ArrayList<Double> neighbors = new ArrayList<>();
        TreeMap<LocalDateTime, Double> neighbors_map = new TreeMap<>();
        Map<LocalDateTime, Double> tempmap = new LinkedHashMap<>();
        Map<LocalDateTime, Double> neighbors_asc = new LinkedHashMap<>();
        Map.Entry<LocalDateTime, Double> entry = data.firstEntry();
        while (data.higherEntry(entry.getKey()) != null) {
            neighbors.add(entry.getValue());
            neighbors_map.put(entry.getKey(), entry.getValue());
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());


            Map<LocalDateTime, Double> neighbors_desc = neighbors_map.entrySet()
                    .stream()
                    .sorted(Map.Entry.<LocalDateTime, Double>comparingByValue().reversed())
                    .collect(toMap(Map.Entry::getKey,
                            Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            //damit die k nächsten Nachbarn nicht später von der falschen Seite abgeschnitten werden


            if (Helper.getDistance(one, two) > configuration.getInterval()) {
                if (neighbors.size() >= configuration.getNeighbors()) {
                    neighbors.subList(configuration.neighbors, neighbors.size()).clear();


                    int counter = 0;
                    for(Iterator<Map.Entry<LocalDateTime, Double>>it=neighbors_desc.entrySet().iterator();it.hasNext();){
                        Map.Entry<LocalDateTime, Double> entry2 = it.next();
                        if (counter >= configuration.neighbors) {
                            it.remove();
                        }
                        counter++;
                    }

                }

                Collections.sort(neighbors);
                neighbors_desc.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .forEachOrdered(x -> neighbors_asc.put(x.getKey(), x.getValue()));


               // Zur Validierung
                /*
                for (int i=0; i<neighbors.size();i++)
                {
                    System.out.println(neighbors.get(i));
                }
                System.out.println("");
                for (Map.Entry<LocalDateTime, Double> entry2 : neighbors_asc.entrySet()) {
                    System.out.println("Key: " + entry2.getKey() + ". Value: " + entry2.getValue());
                }
                System.out.println("");
                System.out.println("Ende");
                System.out.println("");
                */

                int amount_of_x = 0;
                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15)) {
                    amount_of_x++;
                }
                ArrayList<ArrayList<Double>> f_values = f_valuesCreation(neighbors);
                int x = f_values.size(); //Das nächste x, das berechnet werden soll
                P = createNewtonPolynoms(f_values, x);

                /*
                Falls mehrere x-Werte gesucht werden:
                f_values nicht komplett neu initialisieren, sondern das vorhandene Differenzschema nur um eine weitere untere Schrägzeile in der Dreiecksmatrix ergänzen
                -> Spart Rechenzeit
                */
                if (amount_of_x >= 2) {
                    for (int i = 1; i < amount_of_x; i++) {
                        ArrayList<Double> temp = new ArrayList<>();
                        temp.add(P);
                        for (int j = 1; j < f_values.size(); j++) {
                            temp.add(0.0);
                        }
                        f_values.add(x, temp);
                        x = f_values.size();
                        P = createNewtonPolynoms(f_values, x); //Bei P kommt dann der interpolierte Wert für's aktuelle x raus
                    }
                }
                neighbors.clear();
                neighbors_desc.clear();
                neighbors_asc.clear();
                neighbors_map.clear();
                tempmap.clear();
            }
            entry = data.higherEntry(entry.getKey());
        }
        //Nächster Schritt: Interpolierte P-Werte in neue Excel-Tabelle schreiben oder in Datenstruktur speichern
        return data;
    }

    public static class Configuration extends Algorithm.Configuration {
        private int neighbors;

        public Configuration(int interval, int neighbors) {
            super(interval);
            this.neighbors = neighbors;
        }

        public int getNeighbors() {
            return neighbors;
        }
    }
}
