package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.base.ParserException;

/**
 * Heuristics type enum
 */
public enum Heuristics {

    NIGHT_DAY,
    PATTERN,
    SEASON;

    /**
     * Get enum value from string
     *
     * @param string string representation
     * @return enum value
     * @throws ParserException on invalid string
     */
    public static Heuristics get(String string) throws ParserException {
        try {
            return Heuristics.valueOf(string.replace('-', '_').toUpperCase());
        } catch (IllegalStateException e) {
            throw new ParserException("invalid heuristics name");
        }
    }
}