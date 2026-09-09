package demo;

public class CfgSample {

    public String computeGrade(int score, int bonusPoints) {
        String grade;

        if (score >= 90) {
            grade = "A";
        } else if (score >= 80) {
            grade = "B";
        } else if (score >= 70) {
            grade = "C";
        } else {
            grade = "F";
        }

        for (int i = 0; i < bonusPoints / 5; i++) {
            if ("F".equals(grade)) {
                grade = "C";
            } else if ("C".equals(grade)) {
                grade = "B";
            } else if ("B".equals(grade)) {
                grade = "A";
            }
        }

        return grade;
    }

    public int sumPositives(int[] numbers) {
        int sum = 0;
        for (int n : numbers) {
            if (n > 0) {
                sum += n;
            }
        }
        return sum;
    }

    public int collatz(int n) {
        int steps = 0;
        while (n != 1) {
            if (n % 2 == 0) {
                n = n / 2;
            } else {
                n = 3 * n + 1;
            }
            steps++;
        }
        return steps;
    }

    public int countDown(int n) {
        int result = 0;
        do {
            result += n;
            n--;
        } while (n > 0);
        return result;
    }
}
