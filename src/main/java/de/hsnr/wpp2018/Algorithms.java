package de.hsnr.wpp2018;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class Algorithms {

    TreeMap<LocalDateTime, Double> mergeTreeMaps(TreeMap<LocalDateTime, Double> map1, TreeMap<LocalDateTime, Double> map2) {
        map2.putAll(map1);
        return map2;
    }


    public long findMissingTimes(LocalDateTime one, LocalDateTime two)
    {
        Duration duration = Duration.between(one, two);
        long diff = Math.abs(duration.toMinutes());
        return diff;
    }

    public double linearInterpolation(double x, double x1, double x2, double y1, double y2)
    {
        double y;
        y = y1 + (x-x1)/(x2-x1)*(y2-y1);
        return y;
    }


    public void interpolate(TreeMap<LocalDateTime, Double> dataTreemap)
    {
        TreeMap<LocalDateTime, Double> newMap = new TreeMap<>(); //Neue Treemap mit den interpolierten Ergebnissen
        int counter = 1;
        long diff;
        double y_linear, y_newton;
        Map.Entry<LocalDateTime, Double> entry=dataTreemap.firstEntry();
        while(dataTreemap.higherEntry(entry.getKey())!=null)
        {
            LocalDateTime one = entry.getKey();
            LocalDateTime two = dataTreemap.higherKey(entry.getKey());
            diff = findMissingTimes(one,two);
            counter++;
            if (diff>15)
            {
                //x1<=x<=x2
                y_linear = linearInterpolation(counter,counter-1,counter+1,entry.getValue(),dataTreemap.higherEntry(entry.getKey()).getValue());
                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15))
                {
                    newMap.put(newDate,y_linear);
                }
            }
            entry=dataTreemap.higherEntry(entry.getKey());
        }
        newMap = mergeTreeMaps(dataTreemap,newMap);
        //Ergebnisse der newMap später noch in eine neue Excel-Datei schreiben
    }


    public ArrayList<ArrayList<Double>> f_valuesCreation(ArrayList<Double> neighbors)
    {
        ArrayList<ArrayList<Double>> f_values = new ArrayList<>();

        for (int i=0; i<neighbors.size();i++)
        {
            ArrayList<Double> temp = new ArrayList<>();
            temp.add(neighbors.get(i));
            for(int j=1; j<neighbors.size();j++)
            {
                temp.add(0.0);
            }
            f_values.add(i,temp);
        }
        return f_values;
    }


    public double createNewtonPolynoms(ArrayList<ArrayList<Double>> f_values, int x)
    {

        //Schritt 1: Polynome berechnen
        for(int i = 1; i<f_values.size();i++)
        {
            for (int j = 1; j<=i; j++)
            {
                double numerator = f_values.get(i).get(j-1)-f_values.get(i-1).get(j-1);
                double denumerator = (i)-(i-j);
                double temp = numerator/denumerator;
                f_values.get(i).add(j,temp);
            }
        }

        //Schritt 2: Setze Polynome in folgende Formel ein:
        //P(x) = f[x0]+f[x0,x1](x-x0)+f[x0,x1,x2](x-x0)(x-x1)(x-x2)...

        double P = f_values.get(0).get(0);
        double a = 1.0;
        for (int i=1; i<f_values.size();i++)
        {
            a = a*(x-(i-1));
            P = P + f_values.get(i).get(i)*a;
        }
        //System.out.println("Approximation beim nächsten x ist " + P);

        return P;
    }



    public void newtonInterpolation(TreeMap<LocalDateTime, Double> dataTreemap)
    {
        long diff;
        double P;
        int size_of_neighbors = 2;
        ArrayList<Double> neighbors = new ArrayList<>();
        Map.Entry<LocalDateTime, Double> entry=dataTreemap.firstEntry();
        while(dataTreemap.higherEntry(entry.getKey())!=null)
        {
            neighbors.add(entry.getValue());
            LocalDateTime one = entry.getKey();
            LocalDateTime two = dataTreemap.higherKey(entry.getKey());
            diff = findMissingTimes(one, two);
            Collections.sort(neighbors, Collections.reverseOrder()); //damit die k nächsten Nachbarn nicht später von der falschen Seite abgeschnitten werden

            if (diff > 15)
            {
                if (neighbors.size()>=size_of_neighbors)
                {
                    neighbors.subList(size_of_neighbors, neighbors.size()).clear();
                }
                Collections.sort(neighbors);

                int amount_of_x = 0;
                for (LocalDateTime newDate = one.plusMinutes(15); newDate.isBefore(two); newDate = newDate.plusMinutes(15))
                {
                    amount_of_x++;
                }
                ArrayList<ArrayList<Double>> f_values = f_valuesCreation(neighbors);
                int x = f_values.size(); //Das nächste x, das berechnet werden soll
                P = createNewtonPolynoms(f_values,x);

                /*
                Falls mehrere x-Werte gesucht werden:
                f_values nicht komplett neu initialisieren, sondern das vorhandene Differenzschema nur um eine weitere untere Schrägzeile in der Dreiecksmatrix ergänzen
                -> Spart Rechenzeit
                */
                if (amount_of_x>=2)
                {
                    for (int i=1; i<amount_of_x;i++)
                    {
                        ArrayList<Double> temp = new ArrayList<>();
                        temp.add(P);
                        for(int j=1; j<f_values.size();j++)
                        {
                            temp.add(0.0);
                        }
                        f_values.add(x,temp);
                        x = f_values.size();
                        P = createNewtonPolynoms(f_values,x); //Bei P kommt dann der interpolierte Wert für's aktuelle x raus
                    }

                }
                neighbors.clear();
            }
            entry=dataTreemap.higherEntry(entry.getKey());
        }
        //Nächster Schritt: Interpolierte P-Werte in neue Excel-Tabelle schreiben oder in Datenstruktur speichern
    }

}
