package de.hsnr.wpp2018.database;

public interface Descriptor {

    boolean matches(Descriptor descriptor);

    String output();
}