package uk.fergcb.Painter;

import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;

public class UserScanner {
    private final Scanner scanner;

    public UserScanner (InputStream in) {
        this.scanner = new Scanner(in);
    }

    public UserScanner () {
        this(System.in);
    }

    public void close() {
        scanner.close();
    }

    /**
     * Check whether a string can be converted into a double
     *
     * @param string The string to parse
     * @return true if the string can be converted into a double, else false
     */
    public static boolean isValidDouble(String string) {
        double value;
        try {
            value = Double.parseDouble(string);
        } catch (NullPointerException | NumberFormatException ex) {
            return false;
        }
        return value > 0;
    }

    /**
     * Check whether a string can be converted into an integer
     *
     * @param string The string to parse
     * @return true if the string can be converted into an integer, else false
     */
    public static boolean isValidInt(String string) {
        int value;
        try {
            value = Integer.parseInt(string);
        } catch (NullPointerException | NumberFormatException ex) {
            return false;
        }
        return value > 0;
    }

    /**
     * Read a line from the scanner and normalise it
     *
     * @return A line of cleaned user input
     */
    public String takeLine() {
        return scanner.nextLine()
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Get a double from stdin. Keep trying until a valid double is entered.
     *
     * @param prompt A user-facing prompt string
     * @return a decimal value entered by the user
     */
    public double takeDouble(String prompt) {
        System.out.print(prompt);

        String line = takeLine();
        while (!isValidDouble(line)) {
            System.out.println("Invalid input. Please enter a decimal value greater than zero.");
            System.out.print(prompt);
            line = takeLine();
        }

        return Double.parseDouble(line);
    }

    /**
     * Get an int from stdin. Keep trying until a valid integer is entered.
     *
     * @param prompt A user-facing prompt string
     * @return an integer value entered by the user
     */
    public int takeInt(String prompt) {
        System.out.print(prompt);

        String line = takeLine();
        while (!isValidInt(line)) {
            System.out.println("Invalid input. Please enter a whole number greater than zero.");
            System.out.print(prompt);
            line = takeLine();
        }

        return Integer.parseInt(line);
    }

    /**
     * Ask a yes/no question
     *
     * @param prompt   The user-facing question string
     * @param fallback The default response if none is given
     * @return true for input starting with "y", false for "n", otherwise return fallback
     */
    public boolean query(String prompt, boolean fallback) {
        final String msg = prompt + (fallback ? " (Y/n) " : " (y/N) ");
        System.out.print(msg);

        final String res = scanner
                .nextLine()
                .trim()
                .toLowerCase(Locale.ROOT);

        return switch (res) {
            case "y", "yes", "yeah", "yep" -> true;
            case "n", "no", "nah", "nope" -> false;
            default -> fallback;
        };
    }

    /**
     * Ask a yes/no question, defaulting to false if no answer given.
     *
     * @param prompt The user-facing question string
     * @return true for input starting with "y", otherwise false
     */
    public boolean query(String prompt) {
        return query(prompt, false);
    }
}
