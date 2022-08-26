package jump61;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Random;

import static jump61.Side.*;

/** An automated Player.
 *  @author P. N. Hilfinger
 */
class AI extends Player {

    /** A new player of GAME initially COLOR that chooses moves automatically.
     *  SEED provides a random-number seed used for choosing moves.
     */

    AI(Game game, Side color, long seed) {
        super(game, color);
        _random = new Random(seed);
    }

    @Override
    String getMove() {
        Board board = getGame().getBoard();

        assert getSide() == board.whoseMove();
        int choice = searchForMove();
        getGame().reportMove(board.row(choice), board.col(choice));
        return String.format("%d %d", board.row(choice), board.col(choice));
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private int searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert getSide() == work.whoseMove();
        assert work.isLegal(getSide());
        _foundMove = -1;
        if (getSide() == RED) {
            value = minMax(work, 2, true,
                    1, (int) Double.NEGATIVE_INFINITY,
                    (int) Double.POSITIVE_INFINITY);
        } else {
            value = minMax(work, 2, true,
                    -1, (int) Double.NEGATIVE_INFINITY,
                    (int) Double.POSITIVE_INFINITY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        if (!board.isLegal(getSide()) || depth == 0) {
            return staticEval(board, board.size() * board.size());
        }
        Side currPlayer = (sense == 1) ? RED : BLUE;
        int bestSoFar = -sense * (int) Double.POSITIVE_INFINITY;
        for (int move : board.possibleMoves(currPlayer)) {
            board.addSpot(currPlayer, move);
            int response = minMax(board, depth - 1, false,
                    -sense, alpha, beta);
            board.undo();
            if (sense == 1) {
                if (response > bestSoFar) {
                    if (saveMove) {
                        _foundMove = move;
                    }
                    bestSoFar = response;
                    alpha = max(alpha, bestSoFar);
                    if (alpha >= beta) {
                        return bestSoFar;
                    }
                }
            } else {
                if (response < bestSoFar) {
                    if (saveMove) {
                        _foundMove = move;
                    }
                    bestSoFar = response;
                    beta = min(beta, bestSoFar);
                    if (alpha >= beta) {
                        return bestSoFar;
                    }
                }
            }
        }
        return bestSoFar;
    }

    /** Return a heuristic estimate of the value of board position B.
     *  Use WINNINGVALUE to indicate a win for Red and -WINNINGVALUE to
     *  indicate a win for Blue. */
    private int staticEval(Board b, int winningValue) {
        return (b.numOfSide(RED) - b.numOfSide(BLUE));
    }

    /** A random-number generator used for move selection. */
    private Random _random;

    /** Used to convey moves discovered by minMax. */
    private int _foundMove;
}
