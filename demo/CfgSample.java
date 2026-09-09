package demo;

/**
 * Demo file for CFG generation verification.
 *
 * This file is separate from Sample.java (which is kept untouched for the
 * regression check). The methods here are intentionally written to exercise
 * the CFG builder's branching and loop logic:
 *
 *  - computeGrade: contains an if/else-if/else chain and a for loop,
 *    so the generated DOT must show multiple diverging/rejoining paths
 *    and a back-edge.
 *  - sumPositives: contains a for-each loop and an inner if, verifying
 *    for-each handling and nested control flow.
 *  - collatz: contains a while loop with an inner if/else, verifying
 *    loop + branch interleaving and a non-trivial back-edge.
 */
public class CfgSample {

    /**
     * Converts a numeric score to a letter grade.
     *
     * CFG structure expected:
     *   ENTRY -> B_entry
     *   B_entry -> if(score>=90) [true->A block, false->if(score>=80)]
     *   if(score>=80) [true->B block, false->if(score>=70)]
     *   if(score>=70) [true->C block, false->F block]
     *   A/B/C/F blocks all -> merge -> for-loop-header
     *   for-loop-header [true->loop body, false->post-loop]
     *   loop body -> loop-header (back-edge)
     *   post-loop -> return -> EXIT
     */
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

        // Apply bonus: each 5 bonus points upgrades by one letter (simplified)
        for (int i = 0; i < bonusPoints / 5; i++) {
            if ("F".equals(grade)) {
                grade = "C";
            } else if ("C".equals(grade)) {
                grade = "B";
            } else if ("B".equals(grade)) {
                grade = "A";
            }
            // A stays A
        }

        return grade;
    }

    /**
     * Sums only the positive integers in an array.
     *
     * CFG structure expected:
     *   ENTRY -> sum=0 block
     *   sum=0 -> for-each-header [true->body, false->post-loop]
     *   body -> if(n>0) [true->sum+=n, false->skip]
     *   both branches -> back-edge to for-each-header
     *   post-loop -> return sum -> EXIT
     */
    public int sumPositives(int[] numbers) {
        int sum = 0;
        for (int n : numbers) {
            if (n > 0) {
                sum += n;
            }
        }
        return sum;
    }

    /**
     * Counts the number of steps in the Collatz sequence starting from n.
     *
     * CFG structure expected:
     *   ENTRY -> steps=0 block
     *   steps=0 -> while(n!=1) [true->body, false->post-loop]
     *   body -> if(n%2==0) [true->n/=2, false->n=3n+1]
     *   both branches -> steps++ -> back-edge to while-header
     *   post-loop -> return steps -> EXIT
     */
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

    /**
     * Demonstrates a do-while loop: counts down from n to 1.
     *
     * CFG structure expected:
     *   ENTRY -> body-entry (n--)
     *   body-entry -> do-while(n>0) [true->back to body, false->post-loop]
     *   post-loop -> return result -> EXIT
     */
    public int countDown(int n) {
        int result = 0;
        do {
            result += n;
            n--;
        } while (n > 0);
        return result;
    }
}
