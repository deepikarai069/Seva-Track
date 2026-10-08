package com.sevatrack.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Fmt {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    private Fmt() {}

    public static String dateTime(LocalDateTime t) {
        return t == null ? "-" : DT.format(t);
    }

    public static String levelLabel(int level) {
        return switch (level) {
            case 1 -> "L1 Ward Officer";
            case 2 -> "L2 Supervisor";
            case 3 -> "L3 Department Head";
            default -> "L" + level;
        };
    }
}
