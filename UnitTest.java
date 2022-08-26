package jump61;

import org.junit.Test;
import ucb.junit.textui;

import static jump61.Side.*;
import static jump61.AI.*;

/** The suite of all JUnit tests for the Jump61 program.
 *  @author Nitin Nazeer
 */
public class UnitTest {

    private Side[] fillPlayers = {RED, RED, WHITE, RED, RED, RED, WHITE, BLUE,
        WHITE, WHITE, BLUE, BLUE, WHITE, BLUE, BLUE, BLUE};

    private int[] fillSpots = {1, 2, 1, 2, 3, 4, 1, 3, 1, 1, 2, 3, 1, 2, 1, 2};

    private Side[] fillPlayers1 = {RED, RED, RED, BLUE, RED, RED, RED, BLUE,
        RED, RED, BLUE, BLUE, BLUE, BLUE, BLUE, BLUE};

    private int[] fillSpots1 = {2, 3, 3, 2, 2, 1, 4, 2, 3, 4, 3, 3, 2, 3, 3, 2};

    @Test
    public void testRandomAI() {
        Board B = new Board(4);
        for (int i = 0; i < 16; i += 1) {
            B.internalSet(i, fillSpots[i], fillPlayers[i]);
        }
        System.out.println(B);
        System.out.println(B.numOfSide(RED));
        B.addSpot(RED, 2, 1);
        System.out.println(B);
        System.out.println(B.numOfSide(RED));
    }



    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(jump61.BoardTest.class));
    }

}


