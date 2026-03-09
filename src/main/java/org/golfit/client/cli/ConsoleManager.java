package org.golfit.client.cli;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class ConsoleManager {
    private static Terminal terminal;
    private static LineReader lineReader;

    public static void init() throws Exception {
        terminal = TerminalBuilder.builder().system(true).build();
        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
    }

    // Use this instead of System.out.println for ALL log/output messages
    public static void println(String message) {
        if (lineReader != null) {
            lineReader.printAbove(message);
        } else {
            System.out.println(message);
        }
    }

    public static String readLine(String prompt) {
        return lineReader.readLine(prompt);
    }
}