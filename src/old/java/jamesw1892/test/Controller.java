package test;

import java.lang.reflect.Method;

public class Controller {
    private static boolean allTestsPassed = true;

    private String method;
    private String description;

    /**
     * Instantiate at the start of each test method
     * @param fileRef
     * @param method
     * @param inputs
     */
    public Controller(String method, String description) {
        this.method = method;
        this.description = description;
    }

    /**
     * Call if the test passed
     */
    public void pass() {
        this.result(true);
    }

    /**
     * Call if the test failed
     */
    public void fail() {
        this.result(false);
    }

    /**
     * Call if the output of the test is `passed`
     * @param passed
     */
    public void result(boolean passed) {
        Controller.outputResult(passed, this.method, this.description);
    }

    private static void outputResult(boolean passed, String method, String description) {
        String s = "    " + method + ": " + description;
        if (passed) {
            s = "[PASSED]" + s;
        } else {
            s = "[FAILED]" + s;
            allTestsPassed = false;
        }
        System.out.println(s);
    }

    public void output(String s) {
        System.out.println("[OUTPUT]    " + this.method + ": " + s);
    }

    public void outputExpected(String expected, String got, String from) {
        if (from.equals("")) {
            this.output("Expected `" + expected + "` but got `" + got + "`");
        } else {
            this.output("Expected `" + expected + "` from " + from + " but got `" + got + "`");
        }
    }

    // TODO: keep?
    public static void wrapTry(Method m) {
        try {
            m.invoke(null, new Object[0]);
        } catch (Exception e) {
            System.out.println("[ERROR]:    " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Run the tests
     * @param args
     */
    public static void main(String[] args) {
        allTestsPassed = true;
        System.out.println("Running tests...");

        // Call the test files to run
        new TestCorePerson();
        new TestCoreUtil();

        // Output overall result
        String s = System.lineSeparator();
        if (allTestsPassed) {
            s += "[SUCCESS]   All tests passed";
        } else {
            s += "[FAILURE]   Not all tests passed";
        }
        System.out.println(s);
    }
}