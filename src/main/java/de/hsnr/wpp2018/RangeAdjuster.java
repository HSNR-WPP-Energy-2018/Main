package de.hsnr.wpp2018;

import java.time.LocalDateTime;

public interface RangeAdjuster {
    LocalDateTime nextRange(LocalDateTime current);
}
