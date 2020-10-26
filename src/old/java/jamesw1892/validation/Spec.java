package validation;

public class Spec {
    protected Types type;
    protected String[] extraValues;
    protected String msg;

    public Spec(Types type, String[] extraValues) {
        this.type = type;
        this.msg = "Must be type " + type.toString().toLowerCase();
        if (extraValues == null || extraValues.length == 0) {
            this.extraValues = new String[0];
        } else {
            this.extraValues = new String[extraValues.length];
            this.extraValues[0] = extraValues[0].strip();
            this.msg += " or one of the following: '" + this.extraValues[0] + "'";
            for (int i = 1; i < extraValues.length; i++) {
                this.extraValues[i] = extraValues[i].strip();
                this.msg += " , '" + this.extraValues[i] + "'";
            }
        }
    }

    public String toString() {
        return "Spec(" + this.msg + ")";
    }
}