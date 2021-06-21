package validation;

import java.util.Scanner;

class ValidValue {
    boolean valid;
    Object value;
    ValidValue(boolean valid, Object value) {
        this.valid = valid;
        this.value = value;
    }
}

/**
 * TODO:
 * - Match to Python version
 * - Make isValid call a special method in the spec itself to format the output
 */
public class Validator {
    Scanner scanner;

    /**
     * Create a new validator
     */
    public Validator() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Create a new validator with given scanner.
     * Use this if have already opened a scanner as a single program
     * should only have 1 open scanner at a time as closing one closes
     * System.in meaning others cannot work. Also only use 'nextLine' and
     * parse once inputted or problems occur like skipping lines etc
     * @param scanner
     */
    public Validator(Scanner scanner) {
        this.scanner = scanner;
    }

    public void close() {
        this.scanner.close();
    }

    public ValidValue isValid(String value, Spec spec) {

        ValidValue fail = new ValidValue(false, value);

        value = value.strip();

        if (spec.extraValues != null) {
            for (String extraValue: spec.extraValues) {
                if (value.equals(extraValue)) {
                    return new ValidValue(true, value);
                }
            }
        }

        if (spec instanceof SpecIntRange) {
            SpecIntRange specIntRange = (SpecIntRange) spec;
            try {
                int valueInt = Integer.parseInt(value);
                if (specIntRange.min != null && specIntRange.max != null) {
                    return new ValidValue(specIntRange.min <= valueInt && valueInt <= specIntRange.max, valueInt);
                } else if (specIntRange.min != null) {
                    return new ValidValue(specIntRange.min <= valueInt, valueInt);
                } else if (specIntRange.max != null) {
                    return new ValidValue(valueInt <= specIntRange.max, valueInt);
                } else {
                    return new ValidValue(true, valueInt);
                }
            } catch (NumberFormatException e) {
                return fail;
            }
        } else if (spec instanceof SpecStr) {
            SpecStr specStr = (SpecStr) spec;
            if (specStr.toLower) {
                value = value.toLowerCase();
            }
            if (specStr.allowedChars.length != 0) {
                for (char chr: value.toCharArray()) {
                    boolean success = false;
                    for (char allowedChar: specStr.allowedChars) {
                        if (allowedChar == chr) {
                            success = true;
                        }
                    }
                    if (!success) {
                        return fail;
                    }
                }
            }
            if (specStr.allowedStrings.length != 0) {
                boolean success = false;
                for (String allowedString: specStr.allowedStrings) {
                    if (allowedString.equals(value)) {
                        success = true;
                    }
                }
                if (!success) {
                    return fail;
                }
            }
            return new ValidValue(true, value);
        }
        return fail;
    }

    public Object validateInput(Spec spec, String prompt) {
        while (true) {
            ValidValue vv = isValid(input(prompt), spec);
            if (vv.valid) {
                return vv.value;
            } else {
                System.out.println(spec.msg);
            }
        }
    }

    /**
     * Get the next line of input from the user after outputting the prompt given
     * @param prompt
     * @return
     */
    public String input(String prompt) {
        System.out.print(prompt);
        return this.scanner.nextLine();
    }

    /**
     * Will output msg followed by options numbered and return the number of the option chosen
     * where an option's number is 1+(it's index in the provided options array)
     * @param msg
     * @param options
     * @return
     */
    public int menu(String msg, String[] options) {
        for (int i = 0; i < options.length; i++) {
            msg += "\n" + (i+1) + ": " + options[i];
        }
        msg += "\nInput the number corresponding to the option you wish to select: ";
        return menu(msg, options.length);
    }

    /**
     * Will output msg and return the number of the option chosen where the number is between 1 and max inclusive
     * @param msg   Should number the options between 1 and max
     * @param max
     * @return
     */
    public int menu(String msg, int max) {

        SpecIntRange spec = new SpecIntRange(1, max, null);

        return (int) this.validateInput(spec, msg);
    }

    // TODO: make special Specs for months
    public ValidValue isValidMonth(String value) {
        value = value.strip().toLowerCase();
        if (value.equals("")) {
            return new ValidValue(true, null);
        } else if (value.equals("jan") || value.equals("january")) {
            return new ValidValue(true, 1);
        } else if (value.equals("feb") || value.equals("february")) {
            return new ValidValue(true, 2);
        } else if (value.equals("mar") || value.equals("march")) {
            return new ValidValue(true, 3);
        } else if (value.equals("apr") || value.equals("april")) {
            return new ValidValue(true, 4);
        } else if (value.equals("may") || value.equals("may")) {
            return new ValidValue(true, 5);
        } else if (value.equals("jun") || value.equals("june")) {
            return new ValidValue(true, 6);
        } else if (value.equals("jul") || value.equals("july")) {
            return new ValidValue(true, 7);
        } else if (value.equals("aug") || value.equals("august")) {
            return new ValidValue(true, 8);
        } else if (value.equals("sep") || value.equals("september")) {
            return new ValidValue(true, 9);
        } else if (value.equals("oct") || value.equals("october")) {
            return new ValidValue(true, 10);
        } else if (value.equals("nov") || value.equals("november")) {
            return new ValidValue(true, 11);
        } else if (value.equals("dec") || value.equals("december")) {
            return new ValidValue(true, 12);
        } else {
            try {
                int num = Integer.parseInt(value);
                if (num >= 1 && num <= 12) {
                    return new ValidValue(true, num);
                } else {
                    return new ValidValue(false, null);
                }
            } catch (NumberFormatException e) {
                return new ValidValue(false, null);
            }
        }
    }

    // TODO: make special Specs for months
    public Integer validateInputMonth(String prompt) {
        while (true) {
            ValidValue vv = isValidMonth(input(prompt));
            if (vv.valid) {
                return (Integer) vv.value;
            } else {
                System.out.println("Must be an integer between 1 and 12 or the month as a word, either 3 letters or the whole thing");
            }
        }
    }
}