package net.sothatsit.evaluate.optimiser;

public class OptimiseOptions {

    public static final OptimiseOptions DEFAULT = new OptimiseOptions();

    /**
     * Whether this is a pure function.
     *
     * A function will not be optimised if it is not pure.
     */
    public final boolean isPure;

    /**
     * Whether when performing this function fn(a, b) == fn(b, a), and
     * whether when performing this function fn(a, fn(b, c)) == fn(b, fn(a, c)) == fn(c, fn(a, b))
     *
     * For example, addition (+):
     * (a + b) == (b + a), and
     * (a + (b + c)) == (b + (a + c)) == (c + (a + b))
     */
    public final boolean isOrderDependant;

    public OptimiseOptions() {
        this(true, true);
    }

    public OptimiseOptions(boolean isPure, boolean isOrderDependant) {
        this.isPure = isPure;
        this.isOrderDependant = isOrderDependant;
    }

    public OptimiseOptions withIsPure(boolean isPure) {
        return new OptimiseOptions(isPure, isOrderDependant);
    }

    public OptimiseOptions withIsOrderDependant(boolean isOrderDependant) {
        return new OptimiseOptions(isPure, isOrderDependant);
    }
}
