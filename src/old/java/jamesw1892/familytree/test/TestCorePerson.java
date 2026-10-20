package test;

import core.Person;

public class TestCorePerson {

    public TestCorePerson() {
        System.out.println(System.lineSeparator() + "Testing Core.Person:");

        // Call the tests to run
        constructorID0();
        constructorIntended();
        dummyPerson0();
        dummyPerson1();
        getNameFull();
    }

    private static void constructorID0() {
        Controller c = new Controller("constructor", "Checks does not accept ID of 0");

        try {
            new Person(0, "a", "b", "c", true, 1970, 1, 1, false, 1970, 1, 2, "", null, null);
            c.fail();
        } catch (IllegalArgumentException e) {
            c.pass();
        }
    }

    private static void constructorIntended() {
        Controller c = new Controller("constructor", "Checks the constructor works as intended on valid inputs");

        try {
            new Person(1, "a", "b", "c", true, 1970, 1, 1, false, 1970, 1, 2, "", null, null);
            c.pass();
        } catch (IllegalArgumentException e) {
            c.fail();
        }
    }

    private static void dummyPerson0() {
        Controller c = new Controller("dummyPerson", "Should not accept ID < 1");

        try {
            Person.dummyPerson(0);
            c.fail();
        } catch (IllegalArgumentException e) {
            c.pass();
        }
    }

    private static void dummyPerson1() {
        Controller c = new Controller("dummyPerson", "Check gives a dummy person as intended");

        try {
            Person dummy = Person.dummyPerson(1);
            if (dummy.getID() == 1 && dummy.getNameFirst().equals("")) {
                c.pass();
            } else {
                c.fail();
            }
        } catch (IllegalArgumentException e) {
            c.fail();
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