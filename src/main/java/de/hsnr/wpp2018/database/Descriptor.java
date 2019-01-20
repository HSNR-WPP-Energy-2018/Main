package de.hsnr.wpp2018.database;

import de.hsnr.wpp2018.base.ParserException;

public interface Descriptor {

    static Descriptor parse(String descriptor) throws ParserException {
        String[] parts = descriptor.split("[|]");
        if (parts.length != 2) {
            throw new ParserException("descriptor line needs to contain two parts separated by \"|\"");
        }
        switch (parts[0].toLowerCase()) {
            case StringDescriptor.TYPE:
                return StringDescriptor.fromString(parts[1]);
            case NumberDescriptor.TYPE:
                return NumberDescriptor.fromString(parts[1]);
            default:
                throw new ParserException("unrecognized descriptor: " + parts[0].toLowerCase());
        }
    }

    boolean matches(Descriptor descriptor);

    String output();
}