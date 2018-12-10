package de.hsnr.wpp2018.database;

public class StringDescriptor implements Descriptor {

    private String value;

    public StringDescriptor(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean matches(Descriptor descriptor) {
        if (descriptor instanceof StringDescriptor) {
            return ((StringDescriptor) descriptor).getValue().equals(getValue());
        }
        return false;
    }
}