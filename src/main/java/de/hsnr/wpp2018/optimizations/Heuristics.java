package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.base.ParserException;

public enum Heuristics {

    NIGHT_DAY,
    PATTERN,
    SEASON;

    public static Heuristics get(String string) throws ParserException {
        try {
            return Heuristics.valueOf(string.replace('-', '_').toUpperCase());
        } catch (IllegalStateException e) {
            throw new ParserException("invalid heuristics name");
        }
    }
}