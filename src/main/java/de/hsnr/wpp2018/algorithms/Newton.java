package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Helper;
import de.hsnr.wpp2018.Heuristics;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class Newton implements Algorithm<Newton.Configuration> {




    private Map<LocalDateTime, ArrayList<Double>> f_valuesCreation(Map<LocalDateTime, Double> neighbors_asc) {
        Map<LocalDateTime,ArrayList<Double>> f_values = new LinkedHashMap<>();

        neighbors_asc.forEach((key, value) -> {
            ArrayList<Double> temp = new ArrayList<>();
            temp.add(value);
            for (int j=1; j<neighbors_asc.size();j++)
            {
                temp.add(0.0);
            }
            f_values.put(key,temp);
        });
        return f_values;
    }



    private double createNewtonPolynoms(Map<LocalDateTime, ArrayList<Double>> f_values, int x, LocalDateTime newDate, LocalDateTime lastDate) {
        int decimals = 5;
        int i=1;

        //Schritt 1: Polynome berechnen
        LocalDateTime previous_key = null;
        ArrayList<Double> previous_val = null;
        for (Map.Entry<LocalDateTime,ArrayList<Double>> entry_i : f_values.entrySet()) {
            if (previous_key!= null && previous_val != null)
            {
                for (int j = 1; j <= i; j++) {
                    double numerator = entry_i.getValue().get(j-1) - previous_val.get(j-1);
                    double denumerator = (i) - (i - j);
                    if (denumerator==0)
                    {
                        denumerator = denumerator + 0.001;
                    }
                    double temp = numerator / denumerator;
                    entry_i.getValue().add(j,temp);
                }
                i++;
            }
            previous_key = entry_i.getKey();
            previous_val = entry_i.getValue();

        }


        //Schritt 2: Setze Polynome in folgende Formel ein:
        //P(x) = f[x0]+f[x0,x1](x-x0)+f[x0,x1,x2](x-x0)(x-x1)(x-x2)...
        double p = f_values.entrySet().iterator().next().getValue().get(0);
        double a = 1.0;
        int i_counter = 0;
        for(Map.Entry<LocalDateTime,ArrayList<Double>> entry_i : f_values.entrySet()) {
            //System.out.println(entry_i.getValue().get(0));
            if (i_counter > 0 && i_counter<f_values.size()) {
                a = a * (x - (i_counter - 1));
                p = p + entry_i.getValue().get(i_counter) * a;
            }
            i_counter++;
        }
        p = Helper.roundDouble(p,decimals);
        if (p < 0) {
            p = Heuristics.castNegativesToZero(p);
        }

        //System.out.printf("Approximation for next x is " + "%f" + " at " + newDate + "\n", p); //"%f\n"
        if (p == Double.POSITIVE_INFINITY)
        {
            //Heuristik ergänzen
        }

        return p;
    }



    public ArrayList<Algorithm.Consumption> interpolate(TreeMap<LocalDateTime, Double> data, Configuration configuration) {
        double P;
        TreeMap<LocalDateTime, Double> resultmap = new TreeMap<>();
        TreeMap<LocalDateTime, Double> neighbors_map = new TreeMap<>();
        Map<LocalDateTime, Double> neighbors_asc = new LinkedHashMap<>();
        Map.Entry<LocalDateTime, Double> entry = data.firstEntry();
        ArrayList<Algorithm.Consumption> values = new ArrayList<>();


        while (data.higherEntry(entry.getKey()) != null) {
            neighbors_map.put(entry.getKey(), entry.getValue());
            LocalDateTime one = entry.getKey();
            LocalDateTime two = data.higherKey(entry.getKey());


            Map<LocalDateTime, Double> neighbors_desc = neighbors_map.entrySet()
                    .stream()
                    .sorted(Map.Entry.<LocalDateTime, Double>comparingByKey().reversed())
                    .collect(toMap(Map.Entry::getKey,
                            Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            //damit die k nächsten Nachbarn nicht später von der falschen Seite abgeschnitten werden


            if (Helper.getDistance(one, two) > configuration.getInterval()) {
                int counter = 0;
                for(Iterator<Map.Entry<LocalDateTime, Double>>it=neighbors_desc.entrySet().iterator();it.hasNext();){
                    Map.Entry<LocalDateTime, Double> entry2 = it.next();
                    if (counter >= configuration.getNeighbors()) {
                        it.remove();
                    }
                    counter++;
                }

                neighbors_desc.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEachOrdered(x -> neighbors_asc.put(x.getKey(), x.getValue()));



                int amount_of_x = 0;
                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15)) {
                    amount_of_x++;
                }

                Map<LocalDateTime, ArrayList<Double>> f_values = new LinkedHashMap<>();
                f_values = f_valuesCreation(neighbors_asc);


                int x = f_values.size(); //Das nächste x, das berechnet werden soll

                List<Map.Entry<LocalDateTime,ArrayList<Double>>> entryList = new ArrayList<>(f_values.entrySet());
                LocalDateTime lastDate = entryList.get(entryList.size()-1).getKey();

                LocalDateTime newDate = lastDate.plusMinutes(15);
                P = createNewtonPolynoms(f_values, x, newDate, lastDate);
                values.add(new Algorithm.Consumption(newDate,P,true));
                resultmap.put(newDate,P);

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

                        f_values.put(newDate,temp);

                        x = f_values.size();
                        P = createNewtonPolynoms(f_values, x, newDate, lastDate); //Bei P kommt dann der interpolierte Wert für's neue aktuelle x raus
                        resultmap.put(newDate,P);
                        values.add(new Algorithm.Consumption(newDate,P,true));
                    }
                }

                neighbors_desc.clear();
                neighbors_asc.clear();
                neighbors_map.clear();
            }
            else
            {
                values.add(new Algorithm.Consumption(entry.getKey(),entry.getValue(),false));
            }
            entry = data.higherEntry(entry.getKey());
        }
/*
        resultmap.forEach((key, value) ->
        {
            values.add(new Algorithm.Consumption(key,value,true));
            data.put(key,value);
        });
*/

        for (int i=0;i<values.size();i++)
        {
            System.out.println(values.get(i).getTime() + " + " + values.get(i).getEnergyData() + " + " + values.get(i).isInterpolated());
        }

        return values;
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

    public static class Consumption extends Algorithm.Consumption {
        private LocalDateTime time;
        private double energyData;
        private boolean isInterpolated;

        public Consumption(LocalDateTime date, Double energyData, boolean isInterpolated) {
            super(date, energyData,isInterpolated);
            //this.energyData = energyData;
            //this.isInterpolated = isInterpolated;
        }

    }
}
