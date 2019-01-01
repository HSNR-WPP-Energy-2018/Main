package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.Helper;
import de.hsnr.wpp2018.base.Consumption;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.TreeMap;

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

    public static boolean isWinterSeason(Month month) {
        //Hierfür habe ich bisher keine elegantere Lösung gefunden, die gleichzeitig auch das Jahr ignoriert
        Month[] months = {
                Month.NOVEMBER,
                Month.DECEMBER,
                Month.JANUARY,
                Month.FEBRUARY,
                Month.MARCH,
                Month.APRIL,
        };
        for (Month m : months) {
            if (month.equals(m)) {
                return true;
            }
        }
        return false;
    }

    public static void adjustSeasons(TreeMap<LocalDateTime, Consumption> data) {
        double percents = 10;
        int decimals = 6; //Nachkommastellen zum Runden
        for (LocalDateTime time : data.keySet()) {
            if (data.get(time).isInterpolated()) {
                //WinterSaison -> (+10%) Grundumsatz
                if (isWinterSeason(time.getMonth())) {
                    data.get(time).setValue(data.get(time).getValue() + (percents / 100) * data.get(time).getValue());
                    data.get(time).setValue(Helper.roundDouble(data.get(time).getValue(), decimals));
                }
                //SommerSaison -> (-10%) Grundumsatz
                else {
                    data.get(time).setValue(data.get(time).getValue() - (percents / 100) * data.get(time).getValue());
                    data.get(time).setValue(Helper.roundDouble(data.get(time).getValue(), decimals));
                }
            }
        }
        //data.forEach((i) -> System.out.println("Time: " + i.getTime() + ". Value: " + i.getValue() + ". Interpolated? " + i.isInterpolated()));
    }
}