package de.hsnr.wpp2018;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class Algorithms {


    public long findMissingTimes(LocalDateTime one, LocalDateTime two)
    {
        Duration duration = Duration.between(one, two);
        long diff = Math.abs(duration.toMinutes());
        return diff;
    }


    public void linearInterpolation(TreeMap<LocalDateTime, Double> dataTreemap)
    {
        long diff;
        double x1, x2, y1, y2, x, y;
        Map.Entry<LocalDateTime, Double> entry=dataTreemap.firstEntry();
        while(dataTreemap.higherEntry(entry.getKey())!=null)
        {
            LocalDateTime one = entry.getKey();
            LocalDateTime two = dataTreemap.higherKey(entry.getKey());
            diff = findMissingTimes(one,two);
            //System.out.println("Bei " + entry.getKey() + " und " + dataTreemap.higherKey(entry.getKey()) + " ist es " + diff);
            if (diff>15)
            {
                x1 = 1; //ist egal, wie hoch die Werte der Indizees sind, die wir hier wählen, solange x1<=x<=x2
                x = 2;
                x2 = 3;
                y1 = entry.getValue();
                y2 = dataTreemap.higherEntry(entry.getKey()).getValue();
                y = y1 + (x-x1)/(x2-x1)*(y2-y1);
                System.out.println("Linear Interpolation: " + y);
                //Muss noch Ergebnis von y in File oder Datenstruktur reinschreiben
            }
            entry=dataTreemap.higherEntry(entry.getKey());
        }
    }


    //Das hier ist erstmal nur der Algorithmus an sich, der davon ausgeht, dass nur ein fehlendes x am Ende berechnet werden muss

    public void newtonInterpolation(TreeMap<LocalDateTime, Double> dataTreemap)
    {
        ArrayList<ArrayList<Double>> f_values = new ArrayList<>();
        int counter = 0;
        for(Map.Entry<LocalDateTime, Double> entry : dataTreemap.entrySet())
        {
            ArrayList<Double> temp = new ArrayList<>();
            temp.add(entry.getValue());
            for(int i=1; i<dataTreemap.size();i++)
            {
                temp.add(0.0);
            }
            f_values.add(counter,temp);
            counter++;
        }

        int x = f_values.size(); //Das nächste x, das berechnet werden soll

        //Schritt 1: Polynome rekursiv berechnen
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
        System.out.println("Approximation bei x ist " + P);
        //Schritt 3, sofern notwendig: P als polynomielle Formel ausschreiben (z.B. mit Horner-Schema)
    }

}
