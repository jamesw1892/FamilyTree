package test;

import core.Person;

public class TestCorePerson {

    public TestCorePerson() {
        System.out.println(System.lineSeparator() + "Testing Core.Person:");

        // Call the tests to run
        constructorID0();
        getNameFull();
    }

    private static void constructorID0() {
        Controller c = new Controller("constructor", "Checks does not accept ID of 0");

        try {
            new Person(0, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
            c.fail();
        } catch (IllegalArgumentException e) {
            c.pass();
        }
    }

    private static void getNameFull() {
        Controller c = new Controller("getNameFull", "Checks concatenates names together correctly");

        boolean allCorrect = true;
        String[][] in = {
            {"", "", "", ""},
            {"First", "First", "", ""},
            {"Middle", "", "Middle", ""},
            {"Last", "", "", "Last"},
            {"First Middle", "First", "Middle", ""},
            {"First Last", "First", "", "Last"},
            {"Middle Last", "", "Middle", "Last"},
            {"First Middle Last", "First", "Middle", "Last"},
            {"First1 First2", "First1 First2", "", ""},
            {"Middle1 Middle2", "", "Middle1 Middle2", ""}
        };

        for (String[] combo: in) {

            String got = new Person(1, combo[1], combo[2], combo[3], null, null, null, null, null, null, null, null, "", null, null).formatNameFull();
            if (!got.equals(combo[0])) {
                allCorrect = false;
                c.outputExpected(combo[0], got, "Person(nameFirst=`" + combo[1] + "`, nameMiddles=`" + combo[2] + "`, nameLast=`" + combo[3] + "`)");
            }
        }

        c.result(allCorrect);
    }
}