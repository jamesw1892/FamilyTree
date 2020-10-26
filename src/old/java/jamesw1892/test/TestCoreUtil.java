package test;

import core.Util;

public class TestCoreUtil {
    public TestCoreUtil() {
        System.out.println(System.lineSeparator() + "Testing Core.Util:");

        // Call the tests to run
        monthWordValid();
        monthWordInvalid();
    }

    private static void monthWordValid() {
        Controller c = new Controller("monthWord", "Checks works as intended for valid inputs");

        boolean allCorrect = true;
        String[] words = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < words.length; i++) {
            String got = Util.monthWord(i+1);
            if (!words[i].equals(got)) {
                allCorrect = false;
                c.outputExpected(words[i], got, "monthWord(" + (i+1) + ")");
            }
        }

        c.result(allCorrect);
    }

    private static void monthWordInvalid() {
        Controller c = new Controller("monthWord", "Checks disallows invalid inputs");

        try {
            Util.monthWord(0);
            c.fail();
        } catch (IllegalArgumentException e) {
            c.pass();
        }
    }
}