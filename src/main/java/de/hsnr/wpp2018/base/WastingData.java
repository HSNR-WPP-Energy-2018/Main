package de.hsnr.wpp2018.base;

public class WastingData {
    private double processHeating;
    private double processCooling;
    private double ICT;
    private double warmWater;
    private double illumination;
    private double heating;
    private double mechanicalEquip;

    //Prozentualer Verbrauchsanteil der Haushaltsgeräte
    public WastingData(double waste) {
        this.processHeating = waste * 30 / 100; //Prozesswärme
        this.processCooling = waste * 23 / 100; //Prozesskälte
        this.ICT = waste * 17 / 100; //IuK-Systeme
        this.warmWater = waste * 12 / 100; //Warmwasseraufbereitung
        this.illumination = waste * 8 / 100; //Beleuchtung
        this.heating = waste * 7 / 100; //Heizung
        this.mechanicalEquip = waste * 3 / 100; //Mechanische Geräte
    }

    public double getProcessHeating() {
        return processHeating;
    }

    public double getProcessCooling() {
        return processCooling;
    }

    public double getICT() {
        return ICT;
    }

    public double getWarmWater() {
        return warmWater;
    }

    public double getIllumination() {
        return illumination;
    }

    public double getHeating() {
        return heating;
    }

    public double getMechanicalEquip() {
        return mechanicalEquip;
    }
}