package uk.fergcb.Painter;

import java.util.*;

public class Main {

    private static final int AUTO_CALC_SIZE = -1;

    private static final UserScanner us = new UserScanner();

    public static void main(String[] args) {

        // Calculate the area we need to cover
        final double wallArea = getTotalWallArea();
        final double negativeArea = getTotalNegativeArea();
        final double totalArea = wallArea - negativeArea;

        // Select the paint we need
        final Paint selectedPaint = choosePaint(Paint.paintsAvailable);
        final double canSize = chooseSize(selectedPaint);

        // Get the number of cans required
        final double litresRequired = totalArea / selectedPaint.coverage;
        final Map<Double, Integer> cansRequired = getCansRequired(selectedPaint, litresRequired, canSize);
        final String canString = buildCansList(cansRequired);

        System.out.printf("\nTo cover %.2f square meters of wall, you will require %.2f litres of paint.\n", totalArea, litresRequired);
        System.out.printf("You will need " + canString + ".");

        // Free stdin from the scanner
        us.close();
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
                int cansRequired = (int) (litresRequired / size);
                if (cansRequired == 0) continue;
                litresRequired %= size;
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
            selection = us.takeInt("Selection: ") - 1;
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

        String line = us.takeLine();
        while (!isValidSize(paint, line)) {
            System.out.println("Invalid input. Please enter a listed size, or 'auto'.");
            System.out.print(prompt);
            line = us.takeLine();
        }

        if (line.equals("auto")) return AUTO_CALC_SIZE;

        final double selectedSize = Double.parseDouble(line);
        System.out.printf("You have selected a %.1fL can.\n", selectedSize);
        return selectedSize;
    }

    /**
     * Check if a string is a valid paint size
     * @param paint The selected paint
     * @param line The size string to check
     * @return true if the size is valid, else false
     */
    private static boolean isValidSize(Paint paint, String line) {
        if (line.equals("auto")) return true;
        if (!UserScanner.isValidDouble(line)) return false;
        final double size = Double.parseDouble(line);
        return paint.sizes.contains(size);
    }

    /**
     * Prompt the user to get input the dimensions of one or more walls.
     *
     * @return The sum of the areas of the walls in square meters.
     */
    private static double getTotalWallArea() {
        double totalArea = 0;

        int i = 1;
        do {
            System.out.printf("How large is wall #%d?\n", i++);
            totalArea += getRectArea();
        } while (us.query("Are there any more walls?"));

        return totalArea;
    }

    /**
     * Prompt the user to get input the dimensions of one or more negative spaces.
     *
     * @return The sum of the areas of the negative spaces in square meters.
     */
    private static double getTotalNegativeArea() {
        if (!us.query("\nAre there any obstructions (windows, doors, sockets, etc.)?"))
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
        } while (us.query("Are there any more obstructions?"));

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
            shape = us.takeLine();
        } while (!possibleShapes.contains(shape));

        return shape;
    }

    /**
     * Prompt the user to enter the dimensions of a rectangle, and calculate its area.
     *
     * @return The area of the rectangle in square meters.
     */
    private static double getRectArea() {
        final double height = us.takeDouble("Height (m): ");
        final double width = us.takeDouble("Width (m): ");
        return (height * width);
    }

    /**
     * Prompt the user to enter the diameter of a circle, and calculate its area.
     *
     * @return The area of the rectangle in square meters.
     */
    private static double getCircleArea() {
        final double d = us.takeDouble("Diameter (m): ");
        final double r = d * .5;
        return Math.PI * r * r;
    }

    /**
     * Prompt the user to enter the dimensions of an ellipse, and calculate its area.
     *
     * @return The area of the rectangle in square meters.
     */
    private static double getEllipseArea() {
        final double a = us.takeDouble("Height (m): ");
        final double b = us.takeDouble("Width (m): ");
        return Math.PI * a * b;
    }
}
