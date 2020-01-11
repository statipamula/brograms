package tablut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static tablut.Piece.*;
import static tablut.Square.*;


/** The state of a Tablut Game.
 *  @author Santosh Tatipamula
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** a 2-d array containing board pieces. */
    private Piece[][] _board = new Piece[SIZE][SIZE];

    /** Hashmap key piece, value square. */
    private HashMap<Piece, Square> pieceToSquare = new HashMap<>();

    /** Hashmap key piece, value square. */
    private HashMap<Square, Piece> sqtop = new HashMap<>();

    /** HashSet of board strings (previous states of board). */
    private Stack<String> boardstate = new Stack<>();

    /** Stack of board states (previous states of board). */
    private Stack<Move> pastmoves = new Stack<>();

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };



    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initial positions of defenders as list. */
    static final List<Square> INITIALDEFEND = Arrays.asList(INITIAL_DEFENDERS);

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Initializes a stack of boards.
     * @return*/
    Stack<String> boardstate() {
        return boardstate;
    }

    /** Initializes a copy of MODEL.
     * @return*/
    Piece[][] board() {
        return _board;
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        this.pieceToSquare = model.pieceToSquare;
        this.sqtop = model.sqtop;
        this._board = model._board;
        this._turn = model._turn;
        this._moveCount = model.moveCount();
        this._winner = model.winner();
    }

    /** Clears the board to the initial position. */
    void init() {
        sqtop.clear();
        boardstate.clear();
        pastmoves.clear();
        for (Piece[] arr1 : _board) {
            Arrays.fill(arr1, EMPTY);
        }
        for (Square s: INITIAL_ATTACKERS) {
            _board[s.col()][s.row()] = BLACK;
            sqtop.put(s, BLACK);
        }
        for (Square s: INITIAL_DEFENDERS) {
            _board[s.col()][s.row()] = WHITE;
            sqtop.put(s, WHITE);
        }
        _board[THRONE.col()][THRONE.row()] = KING;
        sqtop.put(THRONE, KING);
        _turn = BLACK;
        _repeated = false;
        _moveCount = 0;
        _winner = null;
        boardstate.push(encodedBoard());
    }

    /** Set the move limit to LIM.  It is an error
     * if 2*LIM <= moveCount() given N. */
    void setMoveLimit(int n) {
        if (!(2 * n <= moveCount())) {
            _moveLimit = n;
        }
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    void checkRepeated() {
        if (boardstate.contains(encodedBoard())) {
            _repeated = true;
            if (_turn == BLACK) {
                _winner = BLACK;
            }
            if (_turn == WHITE) {
                _winner = WHITE;
            }
        } else {
            boardstate.add(encodedBoard());
        }
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kpos() {
        for (Map.Entry<Square, Piece> entry : sqtop.entrySet()) {
            if (entry.getValue() == KING) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return _board[col][row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        _board[s.col()][s.row()] = p;
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        int direc = from.direction(to);
        int count = 1;
        Square next = from.rookMove(direc, count);
        if (sqtop.containsKey(next)) {
            return false;
        }
        while (next != to) {
            if (sqtop.containsKey(next)) {
                return false;
            }
            assert next != null;
            next = next.rookMove(direc, count);
        }
        if (next == to) {
            if (sqtop.containsKey(next)) {
                return false;
            }
        }
        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }



    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        if (to == THRONE && sqtop.get(from) != KING) {
            return false;
        }
        if (sqtop.get(from) == turn()) {
            if (!sqtop.containsKey(to)) {
                if (from.col() == to.col() | from.row() == to.row()) {
                    if (isUnblockedMove(from, to)) {
                        return true;
                    }
                }
            }
        } else if (sqtop.get(from) == KING && _turn == WHITE) {
            if (!sqtop.containsKey(to)) {
                if (from.col() == to.col() | from.row() == to.row()) {
                    if (isUnblockedMove(from, to)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        if (!_undo) {
            capturedbyblack.clear();
            capturedbywhite.clear();
        }
        Piece p = sqtop.get(from);
        if (p == _turn || p == KING && turn() == WHITE) {
            put(p, to);
            sqtop.remove(from);
            _board[from.col()][from.row()] = EMPTY;
            sqtop.put(to, p);
            _moveCount += 1;
            for (int i = 0; i < 4; i++) {
                Piece nextp = sqtop.get(to.rookMove(i, 1));
                if (nextp == turn().opponent()
                        || _turn == BLACK && nextp == KING) {
                    if (to.rookMove(i, 2) != null) {
                        if (isHostile(to, to.rookMove(i, 2))) {
                            capture(to, to.rookMove(i, 2));
                        }
                    }
                }
            }
            if (_turn == BLACK) {
                _turn = WHITE;
            } else if (_turn == WHITE) {
                _turn = BLACK;
            }
            if (!_undo) {
                checkRepeated();
            }
            if (!sqtop.containsValue(KING)) {
                _winner = BLACK;
            }
            if (kpos() != null && kpos().isEdge()) {
                _winner = WHITE;
            }
        }
    }


    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
        pastmoves.add(move);
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Square tocapture = sq0.between(sq2);
        Piece tocap = sqtop.get(tocapture);
        if (isHostile(sq0, sq2)) {
            put(EMPTY, tocapture);
            sqtop.remove(tocapture);
            if (_turn == BLACK && (tocap.side() == WHITE)) {
                capturedbyblack.put(tocapture, tocap);
            }
            if (_turn.side() == WHITE && (tocap.side() == BLACK)) {
                capturedbywhite.put(tocapture, tocap);
            }
        }

    }

    /** Checks if SQ0 and SQ2 are hostile.
     * @return */
    private boolean isHostile(Square sq0, Square sq2) {
        Square tocapture = sq0.between(sq2);
        Piece tocap = sqtop.get(tocapture);
        Piece enem = sqtop.get(tocapture).opponent();
        if (!(sqtop.containsKey(THRONE))) {
            if (sq2 == THRONE && sqtop.get(sq0) == enem) {
                if (tocap != KING) {
                    return true;
                } else {
                    if (sqtop.get(sq0.diag1(sq2)) == BLACK
                            && sqtop.get(sq0.diag2(sq2)) == BLACK) {
                        return true;
                    }
                }
            }
        }
        if ((sqtop.get(sq0) == KING | sqtop.get(sq2) == KING)
                && (sqtop.get(sq0) == WHITE | sqtop.get(sq2) == WHITE)) {
            return true;
        }
        if (sqtop.get(sq0) == BLACK && sqtop.get(sq2) == KING) {
            int count = 0;
            for (Square s: INITIALDEFEND) {
                if (sqtop.get(s) == BLACK) {
                    count += 1;
                }
            }
            return count == 4;
        }
        if (sqtop.get(sq0) == BLACK && sqtop.get(sq2) == BLACK) {
            if (tocap == KING && !(INITIALDEFEND.contains(kpos()))
                && !(kpos() == THRONE)) {
                return true;
            }
        }
        if (sqtop.get(sq0) == BLACK & sqtop.get(tocapture) == KING) {
            if (kpos() == THRONE || INITIALDEFEND.contains(kpos())) {
                ArrayList<Square> newlist = new ArrayList<>();
                Square diag1 = sq0.diag1(sq2);
                Square diag2 = sq0.diag2(sq2);
                if (diag1 == THRONE || sqtop.get(diag1) == BLACK) {
                    newlist.add(sq0.diag1(sq2));
                }
                if (diag2 == THRONE || sqtop.get(diag2) == BLACK) {
                    newlist.add(sq0.diag2(sq2));
                }
                if (sqtop.get(sq2) == BLACK || sq2 == THRONE) {
                    newlist.add(sq2);
                }
                return newlist.size() == 3;
            }
        }
        if (sqtop.get(sq0) == enem & sqtop.get(sq2) == enem) {
            return true;
        }
        return false;
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            if (_winner != null) {
                _winner = null;
            }
            boardstate.pop();
            undoPosition();
            if (_turn == BLACK) {
                capturedbyblack.clear();
            }
            if (_turn.side() == WHITE) {
                capturedbywhite.clear();
            }
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        Move m = pastmoves.pop();
        _turn = _turn.opponent();
        _undo = true;
        this.makeMove(m.to(), m.from());
        if (_turn == WHITE) {
            capturedbyblack.forEach((square, piece) ->
                    put(piece, square));
            capturedbyblack.forEach((square, piece) ->
                    sqtop.put(square, piece));
        }
        if (_turn == BLACK) {
            capturedbywhite.forEach((square, piece) ->
                    put(piece, square));
            capturedbywhite.forEach((square, piece) ->
                    sqtop.put(square, piece));
        }
        if (_turn.side() == WHITE) {
            capturedbyblack.clear();
        }
        if (_turn.side() == BLACK) {
            capturedbywhite.clear();
        }
        _undo = false;
        _turn = _turn.opponent();
        _moveCount -= 2;
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        List<Move> allLegal = new ArrayList<>();
        if (side == WHITE) {
            for (int i = 0; i < 4; i++) {
                for (Square t : ROOK_SQUARES[kpos().index()][i]) {
                    if (isUnblockedMove(kpos(), t)) {
                        allLegal.add(new Move(kpos(), t));
                    }
                }
            }
        }
        for (Square s : this.pieceLocations(side)) {
            for (int j = 0; j < 4; j++) {
                for (Square t : ROOK_SQUARES[s.index()][j]) {
                    if (t != THRONE) {
                        if (isUnblockedMove(s, t)) {
                            allLegal.add(new Move(s, t));
                        }
                    }
                }
            }
        }
        return allLegal;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        return this.legalMoves(side).size() > 0;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> sidesquares = new HashSet<>();
        for (Square s: sqtop.keySet()) {
            if (sqtop.get(s) == side) {
                sidesquares.add(s);
            }
        }
        return sidesquares;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;

    /** True when a move is undoing. */
    private boolean _undo;

    /** move limit. */
    private int _moveLimit;

    /** hash map meant to save the
     * captured white pieces from latest black move. */
    private HashMap<Square, Piece> capturedbyblack = new HashMap<>();

    /** hash map accessor.
     * @return  */
    HashMap<Square, Piece> capbyblack() {
        return capturedbyblack;
    }

    /** hash map meant to save the captured
     * black pieces from latest white move. */
    private HashMap<Square, Piece> capturedbywhite = new HashMap<>();

    /** hash map accessor.
     * @return  */
    HashMap<Square, Piece> capbywhite() {
        return capturedbywhite;
    }

    /** hash map accessor.
     * @return  */
    HashMap<Square, Piece> squareToPiece() {
        return sqtop;
    }


}
