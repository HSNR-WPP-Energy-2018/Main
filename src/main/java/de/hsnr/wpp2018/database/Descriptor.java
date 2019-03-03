package de.hsnr.wpp2018.database;

import de.hsnr.wpp2018.base.ParserException;

/**
 * Base interface for database entry descriptors
 */
public interface Descriptor {

    /**
     * Parser from string to descriptor.
     * The string representation contains the type and the actual configuration divided by "|"
     *
     * @param descriptor string representation
     * @return instantiated descriptor
     * @throws ParserException on syntax error
     */
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

    /**
     * Comparator for descriptors
     *
     * @param descriptor Other descriptor
     * @return TRUE on match
     */
    boolean matches(Descriptor descriptor);

    /**
     * Generator for output in string representation. The output is written into files and used in the {@link Descriptor#parse}-method
     *
     * @return output string
     */
    String output();
}