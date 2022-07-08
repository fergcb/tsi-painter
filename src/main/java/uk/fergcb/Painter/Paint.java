package uk.fergcb.Painter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * A data class to describe a colour of paint.
 */
public class Paint {

    public static final List<Paint> paintsAvailable = Arrays.asList(
            new Paint("Foo Green", 6, new double[]{0.5, 1, 2.5, 5}),
            new Paint("Bar Blue", 6.75, new double[]{1, 2.5, 5, 10}),
            new Paint("Baz Beige", 5.5, new double[]{5, 10, 20})
    );

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