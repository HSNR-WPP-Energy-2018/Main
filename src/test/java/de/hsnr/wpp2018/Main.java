package de.hsnr.wpp2018;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        Importer importer = new Importer();
        Algorithms algorithm = new Algorithms();
        importer.readFile("2016.csv");
        System.out.println("Parsed data");


        //algorithm.newtonInterpolation(importer.data);

        /*
        Den Methodenaufruf hab ich erstmal ausgegraut, weil ich hier zum Testen eine kleinere Tabelle genommen habe,
        denn mit der riesigen Originaltabelle gibt es bei mir einen java.lang.OutOfMemoryError: GC overhead limit exceeded,
        weil für alle Werte extrem kleine Polynome berechnet werden müssen
         */
    }
}
