package org.golfit.client.cli;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/** Manages console I/O using JLine to allow log output without disrupting the user's input line. */
public class ConsoleManager {
    private static Terminal terminal;
    private static LineReader lineReader;

    /** Initializes the JLine terminal and line reader. Must be called before any other methods. */
    public static void init() throws Exception {
        terminal = TerminalBuilder.builder().system(true).build();
        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
    }

    /** Prints a message above the current input line without disrupting user input. */
    public static void println(String message) {
        if (lineReader != null) {
            lineReader.printAbove(message);
        } else {
            System.out.println(message);
        }
    }

    /** Reads a line of user input with the given prompt. */
    public static String readLine(String prompt) {
        return lineReader.readLine(prompt);
    }
}