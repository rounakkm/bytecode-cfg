package demo;

public class Sample {


    private String BadlyNamedField = "invalid";

    
    private static final int invalidConstant = 42;

    
    public int addNumbers(int a, int b) {
        return a + b;
    }

   
    public void BadMethodName() {
        System.out.println("Bad method name");
    }

   
    public void processText() {
        String text = null;
        int len = text.length();
        System.out.println("Length: " + len);
    }

   
    public void complexMethod(int x) {
        if (x == 1) {
            System.out.println(1);
        }
        if (x == 2) {
            System.out.println(2);
        }
        if (x == 3) {
            System.out.println(3);
        }
        if (x == 4) {
            System.out.println(4);
        }
        if (x == 5) {
            System.out.println(5);
        }
        if (x == 6) {
            System.out.println(6);
        }
        if (x == 7) {
            System.out.println(7);
        }
        if (x == 8) {
            System.out.println(8);
        }
        if (x == 9) {
            System.out.println(9);
        }
        if (x == 10) {
            System.out.println(10);
        }
    }
}
