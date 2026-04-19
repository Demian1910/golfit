package org.golfit.client.cli;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/** Logback appender that routes log messages through {@link ConsoleManager} to avoid disrupting JLine input. */
public class JLineAppender extends AppenderBase<ILoggingEvent> {
    private String pattern = "%d{HH:mm:ss.SSS dd-MM} [%thread] %-5level - %msg%n";

    /** Formats and prints the log event above the current input line. */
    @Override
    protected void append(ILoggingEvent event) {
        ConsoleManager.println(
                String.format("[%s] %s - %s",
                        event.getLevel(),
                        event.getLoggerName(),
                        event.getFormattedMessage()
                )
        );
    }
}