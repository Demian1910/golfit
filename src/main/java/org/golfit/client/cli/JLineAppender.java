package org.golfit.client.cli;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class JLineAppender extends AppenderBase<ILoggingEvent> {
    private String pattern = "%d{HH:mm:ss.SSS dd-MM} [%thread] %-5level - %msg%n";

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