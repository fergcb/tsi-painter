package uk.fergcb.Painter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Main {

    private static final int AUTO_CALC_SIZE = -1;

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Paints available
        final List<Paint> paints = Arrays.asList(
                new Paint("Foo Green", 6, new double[]{0.5, 1, 2.5, 5}),
                new Paint("Bar Blue", 6.75, new double[]{1, 2.5, 5, 10}),
                new Paint("Baz Beige", 5.5, new double[]{5, 10, 20})
        );

        // Calculate the area we need to cover
        final double wallArea = getTotalWallArea();
        final double negativeArea = getTotalNegativeArea();
        final double totalArea = wallArea - negativeArea;

        // Select the paint we need
        final Paint selectedPaint = choosePaint(paints);
        final double canSize = chooseSize(selectedPaint);

        // Get the number of cans required
        final double litresRequired = totalArea / selectedPaint.coverage;
        final Map<Double, Integer> cansRequired = getCansRequired(selectedPaint, litresRequired, canSize);
        final String canString = buildCansList(cansRequired);

        System.out.printf("\nTo cover %.2f square meters of wall, you will require %.2f litres of paint.\n", totalArea, litresRequired);
        System.out.printf("You will need " + canString + ".");

        // Free stdin from the scanner
        scanner.close();
    }

    /**
     * Build a string listing the number of each size of can required.
     *
     * @param cansRequired A map of can sizes and the number required of each
     * @return A user-friendly list of the cans needed
     */
    private static String buildCansList(Map<Double, Integer> cansRequired) {
        StringBuilder sb = new StringBuilder();
        List<Double> canSizes = new ArrayList<>(cansRequired.keySet());
        for (int i = 0; i < canSizes.size(); i++) {
            final double size = canSizes.get(i);
            final int count = cansRequired.get(size);
            // "{count}×{size}L can(s)"
            sb.append(String.format("%d×%.1fL ", count, size));
            sb.append(count == 1 ? "can" : "cans");
            // Nice list formatting
            if (i < canSizes.size() - 2) sb.append(", ");
            else if (i < canSizes.size() - 1) sb.append(" and ");
        }

        return sb.toString();
    }

    /**
     * Check whether a string can be converted into a double
     *
     * @param string The string to parse
     * @return true if the string can be converted into a double, else false
     */
    private static boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
        } catch (NullPointerException | NumberFormatException ex) {
            return false;
        }
        return true;
    }

    /**
     * Check whether a string can be converted into an integer
     *
     * @param string The string to parse
     * @return true if the string can be converted into an integer, else false
     */
    private static boolean isInt(String string) {
        try {
            Integer.parseInt(string);
        } catch (NullPointerException | NumberFormatException ex) {
            return false;
        }
        return true;
    }

    /**
     * Read a line from the scanner and normalise it
     *
     * @return A line of cleaned user input
     */
    private static String takeLine() {
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
    private static double takeDouble(String prompt) {
        System.out.print(prompt);

        String line = takeLine();
        while (!isDouble(line)) {
            System.out.println("Invalid input. Please enter a decimal value.");
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
    private static int takeInt(String prompt) {
        System.out.print(prompt);

        String line = takeLine();
        while (!isInt(line)) {
            System.out.println("Invalid input. Please enter a whole number.");
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
    private static boolean query(String prompt, boolean fallback) {
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
    private static boolean query(String prompt) {
        return query(prompt, false);
    }

    /**
     * Get the number of each size of paint can required.
     *
     * @param paint          The type of paint selected
     * @param litresRequired The amount of paint required
     * @param canSize        The chosen size of paint can (-1 for auto mode)
     * @return A map of can sizes to the number required of each
     */
    private static Map<Double, Integer> getCansRequired(Paint paint, double litresRequired, double canSize) {
        Map<Double, Integer> cans = new LinkedHashMap<>();
        if (canSize == AUTO_CALC_SIZE) {
            // All can sizes available in the selected colour
            // Sorted descending
            List<Double> sizes = paint.sizes.stream()
                    .sorted((a, b) -> (int) Math.signum(b - a))
                    .toList();

            // Round up to the nearest multiple of the smallest can
            // Prevents an extra small can being added when a larger multiple size exists
            // e.g. 2*0.5L --> 1*1L
            double smallestSize = sizes.get(sizes.size() - 1);
            litresRequired = smallestSize * Math.ceil(litresRequired / smallestSize);

            // Calculate how many of each size
            for (Double size : sizes) {
                int cansRequired = (int) Math.floor(litresRequired / size);
                litresRequired %= size;
                if (cansRequired == 0) continue;
                cans.put(size, cansRequired);
            }

        } else {
            int cansRequired = (int) Math.ceil(litresRequired / canSize);
            cans.put(canSize, cansRequired);
        }

        return cans;
    }

    /**
     * Display a list of available paints, and prompt the user to choose one by its list index.
     *
     * @param paints A list of Paints to select from
     * @return the selected Paint
     */
    private static Paint choosePaint(List<Paint> paints) {
        System.out.println("\nPlease select a paint:");
        for (int i = 0; i < paints.size(); i++) {
            Paint paint = paints.get(i);
            System.out.printf("%d - %s\n", i + 1, paint.name);
        }

        int selection;
        do {
            selection = takeInt("Selection: ") - 1;
        } while (selection < 0 || selection >= paints.size());

        final Paint selectedPaint = paints.get(selection);
        System.out.printf("You have selected '%s'.\n", selectedPaint.name);

        return selectedPaint;
    }

    /**
     * Prompt the user to choose a size of paint can from the available sizes
     *
     * @param paint The Paint chosen by the user
     * @return the size of the selected can in litres
     */
    private static double chooseSize(Paint paint) {
        final List<Double> sizes = paint.sizes;

        System.out.println("\nPlease select a paint can size (litres).");
        System.out.println("Enter 'auto' to minimise the number of paint cans needed.");

        String prompt = String.format("Selection %s or 'auto': ", sizes.toString());
        System.out.print(prompt);

        String line = takeLine();
        while (!isDouble(line) && !line.equals("auto")) {
            System.out.println("Invalid input. Please enter a listed size, or 'auto'.");
            System.out.print(prompt);
            line = takeLine();
        }

        if (line.equals("auto")) return AUTO_CALC_SIZE;

        final double selectedSize = Double.parseDouble(line);
        System.out.printf("You have selected a %.1fL can.\n", selectedSize);
        return selectedSize;
    }

    /**
     * Prompt the user to get input the dimensions of one or more walls.
     *
     * @return The sum of the areas of the walls in square meters.
     */
    private static double getTotalWallArea() {
        double totalArea = 0;

        do {
            System.out.println("How large is the wall?");
            totalArea += getRectArea();
        } while (query("Are there any more walls?"));

        return totalArea;
    }

    /**
     * Prompt the user to get input the dimensions of one or more negative spaces.
     *
     * @return The sum of the areas of the negative spaces in square meters.
     */
    private static double getTotalNegativeArea() {
        if (!query("\nAre there any obstructions (windows, doors, sockets, etc.)?"))
            return 0;

        double totalArea = 0;

        do {
            System.out.println("What shape is the obstruction?");
            String shape = getShape();

            System.out.println("How large is the obstruction?");
            totalArea += switch (shape) {
                case "rect" -> getRectArea();
                case "circle" -> getCircleArea();
                case "ellipse" -> getEllipseArea();
                default -> throw new IllegalArgumentException(String.format("Invalid shape %s specified.", shape));
            };
        } while (query("Are there any more obstructions?"));

        return totalArea;
    }

    /**
     * Prompt the user to enter the name of a shape.
     *
     * @return The name of the shape.
     */
    private static String getShape() {
        List<String> possibleShapes = Arrays.asList("rect", "ellipse", "circle");

        String shape;
        do {
            System.out.printf("Shape %s: ", possibleShapes.toString());
            shape = scanner.nextLine();
        } while (!possibleShapes.contains(shape));

        return shape;
    }

    /**
     * Prompt the user to enter the dimensions of a rectangle, and calculate its area.
     *
     * @return The area of the rectangle in square meters.
     */
    private static double getRectArea() {
        final double height = takeDouble("Height (m): ");
        final double width = takeDouble("Width (m): ");
        return (height * width);
    }

    /**
     * Prompt the user to enter the diameter of a circle, and calculate its area.
     *
     * @return The area of the rectangle in square meters.
     */
    private static double getCircleArea() {
        final double d = takeDouble("Diameter (m): ");
        final double r = d * .5;
        return Math.PI * r * r;
    }

    /**
     * Prompt the user to enter the dimensions of an ellipse, and calculate its area.
     *
     * @return The area of the rectangle in square meters.
     */
    private static double getEllipseArea() {
        final double a = takeDouble("Height (m): ");
        final double b = takeDouble("Width (m): ");
        return Math.PI * a * b;
    }

    /**
     * A data class to describe a colour of paint.
     */
    private static class Paint {
        public final String name;
        public final double coverage;
        public final List<Double> sizes;

        public Paint(String name, double coverage, double[] sizes) {
            this.name = name;
            this.coverage = coverage;
            this.sizes = DoubleStream.of(sizes)
                    .boxed()
                    .collect(Collectors.toList());
        }
    }
}
