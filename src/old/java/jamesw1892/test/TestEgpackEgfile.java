package test;

public class TestEgpackEgfile {
    public TestEgpackEgfile() {
        System.out.println("Testing Egpack.Egfile:");

        // Call the tests to run
        egmethodEgshortdescription();
    }

    private static void egmethodEgshortdescription() {
        Controller c = new Controller("egmethod", "Example long description");

        // carry out test here and can output extra information using the following if wanted
        c.output("thing to output");
        c.outputExpected("a", "b", "egmethod(param1, param2, ...)");

        // call one of the following with the outcome of the test
        c.pass();
        c.fail();
        c.result(true);
    }
}