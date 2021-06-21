package validation;

public class SpecIntRange extends Spec {
    protected Integer min;
    protected Integer max;

    public SpecIntRange(Integer min, Integer max, String[] extraValues) {
        super(Types.INT, extraValues);
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException("min greater than max");
        }
        this.min = min;
        this.max = max;

        if (min != null) {
            this.msg += ", minimum " + min;
        }
        if (max != null) {
            this.msg += ", maximum " + max;
        }
    }
}