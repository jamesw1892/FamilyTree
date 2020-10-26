package validation;

public class SpecStr extends Spec {
    protected String[] allowedStrings;
    protected char[] allowedChars;
    protected boolean toLower;

    public SpecStr(String[] allowedStrings, char[] allowedChars, boolean toLower, String[] extraValues) {
        super(Types.STRING, extraValues);
        this.toLower = toLower;

        if (allowedStrings == null || allowedStrings.length == 0) {
            this.allowedStrings = new String[0];
        } else {
            if (toLower) {
                this.allowedStrings = new String[allowedStrings.length];
                this.allowedStrings[0] = allowedStrings[0].toLowerCase();
                this.msg += " and once converted to lower case, must be one of the following: '" + this.allowedStrings[0] + "'";
                for (int i = 1; i < allowedStrings.length; i++) {
                    this.allowedStrings[i] = allowedStrings[i].toLowerCase();
                    this.msg += " , '" + this.allowedStrings[i] + "'";
                }
            } else {
                this.allowedStrings = allowedStrings;
                this.msg += " and must be one of the following: '" + allowedStrings[0] + "'";
                for (String allowedString: allowedStrings) {
                    this.msg += " , '" + allowedString + "'";
                }
            }
        }

        if (allowedChars == null || allowedChars.length == 0) {
            this.allowedChars = new char[0];
        } else {
            if (toLower) {
                this.allowedChars = new char[allowedChars.length];
                this.allowedChars[0] = Character.toLowerCase(allowedChars[0]);
                this.msg += " and once converted to lower case, every character must be one of the following: '" + this.allowedChars[0] + "'";
                for (int i = 1; i < allowedChars.length; i++) {
                    this.allowedChars[i] = Character.toLowerCase(allowedChars[i]);
                    this.msg += " , '" + this.allowedChars[i] + "'";
                }
            } else {
                this.allowedChars = allowedChars;
                this.msg += " and every character must be one of the following: ";
                for (char allowedChar: allowedChars) {
                    this.msg += " , '" + allowedChar + "'";
                }
            }
        }
    }
}