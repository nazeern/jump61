package jump61;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Formatter;

import java.util.function.Consumer;

import static java.lang.System.arraycopy;
import static jump61.Side.*;
import static jump61.Square.square;
import static jump61.Utils.*;

/** Represents the state of a Jump61 game.  Squares are indexed either by
 *  row and column (between 1 and size()), or by square number, numbering
 *  squares by rows, with squares in row 1 numbered from 0 to size()-1, in
 *  row 2 numbered from size() to 2*size() - 1, etc. (i.e., row-major order).
 *
 *  A Board may be given a notifier---a Consumer<Board> whose
 *  .accept method is called whenever the Board's contents are changed.
 *
 *  @author Nitin Nazeer
 */
class Board {

    /** The 2D array representing the game board. */
    private Square[][] _cells;

    /**  */
    private ArrayList<Square[][]> _history;

    /**  */
    private int _lastHistory;

    /**  */
    private int _curr;

    /** An uninitialized Board.  Only for use by subtypes. */
    protected Board() {
        _notifier = NOP;
    }

    /** An N x N board in initial configuration. */
    Board(int N) {
        this();
        _cells = new Square[N][N];
        for (int r = 0; r < size(); r += 1) {
            for (int c = 0; c < size(); c += 1) {
                _cells[r][c] = square();
            }
        }
        _lastHistory = _curr = 0;
        _history = new ArrayList<Square[][]>();
        markUndo();
    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear, and whose notifier does nothing. */
    Board(Board board0) {
        this(board0.size());
        internalCopy(board0);
        this._notifier = NOP;

        _history.clear();
        _curr = _lastHistory = 0;
        markUndo();

        _readonlyBoard = new ConstantBoard(this);
    }

    /** Returns a readonly version of this board. */
    Board readonlyBoard() {
        return _readonlyBoard;
    }

    /** (Re)initialize me to a cleared board with N squares on a side. Clears
     *  the undo history and sets the number of moves to 0. */
    void clear(int N) {
        announce();
        _cells = new Square[N][N];
        for (int r = 0; r < size(); r += 1) {
            for (int c = 0; c < size(); c += 1) {
                _cells[r][c] = square();
            }
        }
        _lastHistory = _curr = 0;
        _history.clear();
        markUndo();

    }

    /** Copy the contents of BOARD into me. */
    void copy(Board board) {
        internalCopy(board);
        this._curr = board.curr();
        this._history = board.history();
    }


    /** Copy the contents of BOARD into me, without modifying my undo
     *  history. Assumes BOARD and I have the same size. */
    private void internalCopy(Board board) {
        assert size() == board.size();
        Square[][] dest = this.cells();
        Square[][] src = board.cells();
        assert src.length == dest.length && src[0].length == dest[0].length;
        for (int i = 0; i < src.length; i += 1) {
            arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
    }

    /** Return the number of rows and of columns of THIS. */
    int size() {
        return _cells.length;
    }

    /** Return the current move number. */
    int curr() {
        return this._curr;
    }

    /** Return the collected history of board states. */
    ArrayList<Square[][]> history() {
        return this._history;
    }

    /** Returns the contents of the square at row R, column C
     *  1 <= R, C <= size (). */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /** Returns the contents of square #N, numbering squares by rows, with
     *  squares in row 1 number 0 - size()-1, in row 2 numbered
     *  size() - 2*size() - 1, etc. */
    Square get(int n) {
        return _cells[row(n) - 1][col(n) - 1];
    }

    /** @return the 2D array of Squares that represent the current board. */
    Square[][] cells() {
        return this._cells;
    }

    /** Returns the total number of spots on the board. */
    int numPieces() {
        int total = 0;
        for (int r = 1; r <= size(); r += 1) {
            for (int c = 1; c <= size(); c += 1) {
                total += get(r, c).getSpots();
            }
        }
        return total;
    }

    /** Returns the total number of spots on the board.
     * @param player the Side currently playing */
    int numPieces(Side player) {
        int total = 0;
        for (int r = 1; r <= size(); r += 1) {
            for (int c = 1; c <= size(); c += 1) {
                if (get(r, c).getSide() == player) {
                    total += get(r, c).getSpots();
                }
            }
        }
        return total;
    }

    /** Returns the Side of the player who would be next to move.  If the
     *  game is won, this will return the loser (assuming legal position). */
    Side whoseMove() {
        return ((numPieces() + size()) & 1) == 0 ? RED : BLUE;
    }

    /** Return true iff row R and column C denotes a valid square. */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /** Return true iff S is a valid square number. */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /** Return the row number for square #N. */
    final int row(int n) {
        return n / size() + 1;
    }

    /** Return the column number for square #N. */
    final int col(int n) {
        return n % size() + 1;
    }

    /** Return the square number of row R, column C. */
    final int sqNum(int r, int c) {
        return (c - 1) + (r - 1) * size();
    }

    /** Return a string denoting move (ROW, COL)N. */
    String moveString(int row, int col) {
        return String.format("%d %d", row, col);
    }

    /** Return a string denoting move N. */
    String moveString(int n) {
        return String.format("%d %d", row(n), col(n));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
        to square at row R, column C. */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
     *  to square #N. */
    boolean isLegal(Side player, int n) {
        return isLegal(player) && exists(n)
                && player != get(n).getSide().opposite();
    }

    /** Returns true iff PLAYER is allowed to move at this point. */
    boolean isLegal(Side player) {
        return !(getWinner() == BLUE || getWinner() == RED);
    }

    /** Returns the possible moves for a given PLAYER. */
    ArrayList<Integer> possibleMoves(Side player) {
        ArrayList<Integer> possible = new ArrayList<Integer>();
        for (int n = 0; n < size() * size(); n += 1) {
            if (isLegal(player, n)) {
                possible.add(n);
            }
        }
        return possible;
    }

    /** Returns the winner of the current position, if the game is over,
     *  and otherwise null. */
    final Side getWinner() {
        if (numOfSide(RED) == this.size() * this.size()) {
            return RED;
        } else if (numOfSide(BLUE) == this.size() * this.size()) {
            return BLUE;
        } else {
            return null;
        }
    }

    /** Return the number of squares of given SIDE. */
    int numOfSide(Side side) {
        int total = 0;
        for (int r = 1; r <= size(); r += 1) {
            for (int c = 1; c <= size(); c += 1) {
                if (get(r, c).getSide() == side) {
                    total += 1;
                }
            }
        }
        return total;
    }

    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C). */
    void addSpot(Side player, int r, int c) {
        addSpotHelper(player, r, c);
        _curr += 1;
        _lastHistory = _curr;
        markUndo();
    }

    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C). */
    void addSpotHelper(Side player, int r, int c) {
        if (!isLegal(player)) {
            return;
        }
        simpleAdd(player, r, c, 1);
        if (overfull(r, c)) {
            jump(player, r, c);
        }
    }

    /** Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N). */
    void addSpot(Side player, int n) {
        addSpot(player, row(n), col(n));
    }

    /** Return whether the square at R, C is overfull. */
    boolean overfull(int r, int c) {
        return get(r, c).getSpots() > neighbors(r, c);
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white). */
    void set(int r, int c, int num, Side player) {
        internalSet(r, c, num, player);
        announce();
    }
    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Does not announce
     *  changes. */
    private void internalSet(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), num, player);
    }

    /** Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     *  if NUM > 0 (otherwise, white). Does not announce changes. */
    void internalSet(int n, int num, Side player) {
        assert num >= 0;
        _cells[row(n) - 1][col(n) - 1] = square(player, num);
    }

    /** Undo the effects of one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared, or the construction of this Board. */
    void undo() {
        if (_curr <= 0) {
            System.out.println("Cannot undo, at initial state");
            return;
        }
        _curr -= 1;
        deepCopy(_history.get(_curr), this._cells);
        _history.remove(_history.size() - 1);
    }

    /** Record the beginning of a move in the undo history. */
    private void markUndo() {
        Square[][] savedCells = new Square[size()][size()];
        deepCopy(this.cells(), savedCells);
        _history.add(savedCells);
    }

    /** Add DELTASPOTS spots of side PLAYER to row R, column C,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int r, int c, int deltaSpots) {
        internalSet(r, c, deltaSpots + get(r, c).getSpots(), player);
    }

    /** Add DELTASPOTS spots of color PLAYER to square #N,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int n, int deltaSpots) {
        internalSet(n, deltaSpots + get(n).getSpots(), player);
    }

    /**
     *
     * @param player the side that jumps
     * @param S the square number
     */
    private void jump(Side player, int S) {
        jump(player, row(S), col(S));
    }

    /**
     *
     * @param player the side that jumps
     * @param r the row number
     * @param c the col number
     */
    private void jump(Side player, int r, int c) {
        int[][] neighbors = {{r - 1, c}, {r + 1, c}, {r, c - 1}, {r, c + 1}};
        set(r, c, 1, player);
        for (int[] coord : neighbors) {
            if (exists(coord[0], coord[1])) {
                addSpotHelper(player, coord[0], coord[1]);
            }
        }
    }

    /** Returns my dumped representation. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===\n");
        for (Square[] r : this._cells) {
            out.format("    ");
            for (Square s : r) {
                out.format("%1d%1s ", s.getSpots(), s.getSide().repr());
            }
            out.format("\n");
        }
        out.format("===\n");
        return out.toString();
    }

    /** Returns an external rendition of me, suitable for human-readable
     *  textual display, with row and column numbers.  This is distinct
     *  from the dumped representation (returned by toString). */
    public String toDisplayString() {
        String[] lines = toString().trim().split("\\R");
        Formatter out = new Formatter();
        for (int i = 1; i + 1 < lines.length; i += 1) {
            out.format("%2d %s%n", i, lines[i].trim());
        }
        out.format("  ");
        for (int i = 1; i <= size(); i += 1) {
            out.format("%3d", i);
        }
        return out.toString();
    }

    /** Returns the number of neighbors of the square at row R, column C. */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /** Returns the number of neighbors of square #N. */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        } else {
            Board B = (Board) obj;
            for (int r = 1; r <= size(); r += 1) {
                for (int c = 1; c <= size(); c += 1) {
                    if (!(this.get(r, c).equals(B.get(r, c)))) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        return numPieces();
    }

    /** Set my notifier to NOTIFY. */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /** Take any action that has been set for a change in my state. */
    private void announce() {
        _notifier.accept(this);
    }

    /** Used in jump to keep track of squares needing processing.  Allocated
     *  here to cut down on allocations. */
    private final ArrayDeque<Integer> _workQueue = new ArrayDeque<>();

    /** A notifier that does nothing. */
    private static final Consumer<Board> NOP = (s) -> { };

    /** A read-only version of this Board. */
    private ConstantBoard _readonlyBoard;

    /** Use _notifier.accept(B) to announce changes to this board. */
    private Consumer<Board> _notifier;
}
