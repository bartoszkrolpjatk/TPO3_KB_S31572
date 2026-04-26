package zad1;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.nnn");
    private final StringBuilder stringBuilder;

    public Log() {
        this.stringBuilder = new StringBuilder();
    }

    public void log(String message) {
        log(message, false);
    }

    public synchronized void log(String message, boolean appendTime) {
        if (appendTime)
            stringBuilder.append(LocalTime.now().format(FORMATTER))
                    .append(" ")
                    .append(message);
        else
            stringBuilder.append(message);
        stringBuilder.append("\n");
    }

    public String getLog() {
        return stringBuilder.toString();
    }
}
