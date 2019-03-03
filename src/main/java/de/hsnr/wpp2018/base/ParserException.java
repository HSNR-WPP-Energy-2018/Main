package de.hsnr.wpp2018.base;

/**
 * Parser error. Separate class to distinct from other errors
 */
public class ParserException extends Exception {

    /**
     * {@inheritDoc}
     */
    public ParserException(String message) {
        super(message);
    }
}