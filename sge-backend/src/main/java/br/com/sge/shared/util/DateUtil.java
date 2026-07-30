package br.com.sge.shared.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private DateUtil() {
    }

    public static String nowIso() {
        return OffsetDateTime.now().format(ISO_FORMATTER);
    }
}
