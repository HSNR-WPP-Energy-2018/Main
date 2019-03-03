package de.hsnr.wpp2018.database;

/**
 * String descriptor. Equality is defined by identical values
 */
public class StringDescriptor implements Descriptor {
    public static final String TYPE = "string";

    /**
     * Parse the descriptor from the string representation without the type prefix
     *
     * @param string string representation
     * @return instantiated descriptor
     */
    public static StringDescriptor fromString(String string) {
        return new StringDescriptor(string);
    }

    private String value;

    public StringDescriptor(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(Descriptor descriptor) {
        if (descriptor instanceof StringDescriptor) {
            return ((StringDescriptor) descriptor).getValue().equals(getValue());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String output() {
        return TYPE + "|" + getValue();
    }

    @Override
    public String toString() {
        return getValue();
    }
}