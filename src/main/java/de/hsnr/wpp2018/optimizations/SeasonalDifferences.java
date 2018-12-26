package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Helper;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

public class SeasonalDifferences {


    /*
    Quelle: Geographie Innsbruck Tirol Atlas (Projekt vom Europäischen Fonds für regionale Entwicklung (EFRE))
    http://tirolatlas.uibk.ac.at/maps/thema/query.py/text?lang=de;id=1099
    Winterhalbjahr: Dauer vom 1. November bis zum 30. April des Folgejahres [verbraucht mehr Heizung und Licht]
    Sommerhalbjahr mit der Dauer vom 1. Mai bis zum 31. Oktober

    Quelle: Strom-Magazin https://www.strom-magazin.de/strommarkt/stromverbrauch-im-jahresverlauf-der-winter-ist-stromsaison_51786.html
    Die Stromverbrauchskurve im Sommer liegt in Deutschland etwa zehn Prozentpunkte unter der Verbrauchskurve im Winter
    (Stand ist jedoch 2002)
     */

    public static boolean isWinterSeason(LocalDateTime thisDate){
        Month m = thisDate.getMonth();
        //Hierfür habe ich bisher keine elegantere Lösung gefunden, die gleichzeitig auch das Jahr ignoriert
        if (m.equals(Month.NOVEMBER) || m.equals(Month.DECEMBER) || m.equals((Month.JANUARY))
                || m.equals(Month.FEBRUARY) || m.equals(Month.MARCH) || m.equals(Month.APRIL)) {
            return true;
        } else {
            return false;
        }
    }


    public static ArrayList<Algorithm.Consumption> adjustSeasons(ArrayList<Algorithm.Consumption> newdata) {
        double percents = 10;
        int decimals = 6; //Nachkommastellen zum Runden
        for (int i=0; i<newdata.size();i++)
        {
            LocalDateTime today = newdata.get(i).getTime();
            if (newdata.get(i).isInterpolated())
            {
                //WinterSaison -> (+10%) Grundumsatz
                if (isWinterSeason(today))
                {
                    newdata.get(i).setEnergyData(newdata.get(i).getEnergyData() + (percents / 100) * newdata.get(i).getEnergyData());
                    newdata.get(i).setEnergyData(Helper.roundDouble(newdata.get(i).getEnergyData(),decimals));
                }
                //SommerSaison -> (-10%) Grundumsatz
                else
                {
                    newdata.get(i).setEnergyData(newdata.get(i).getEnergyData() - (percents / 100) * newdata.get(i).getEnergyData());
                    newdata.get(i).setEnergyData(Helper.roundDouble(newdata.get(i).getEnergyData(),decimals));
                }
            }
        }

        //newdata.forEach((i) -> System.out.println("Time: " + i.getTime() + ". Value: " + i.getEnergyData() + ". Interpolated? " + i.isInterpolated()));
        return newdata;
    }

}
