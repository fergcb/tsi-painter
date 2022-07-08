package uk.fergcb.Painter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * A data class to describe a colour of paint.
 */
public class Paint {
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